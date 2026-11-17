package com.example.assignment3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;
    private TransactionManager transactionManager;
    private TransactionAdapter transactionAdapter;
    private AggregationAdapter aggregationAdapter;
    private TextView tvSummary;
    private Spinner spinnerPeriod;
    private Date startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        transactionManager = new TransactionManager();
        setupDefaultRules();

        tvSummary = findViewById(R.id.tv_summary);
        spinnerPeriod = findViewById(R.id.spinner_period);
        
        // Simple list for aggregated totals
        RecyclerView rvAggregations = findViewById(R.id.rv_aggregations);
        rvAggregations.setLayoutManager(new LinearLayoutManager(this));
        aggregationAdapter = new AggregationAdapter(transactionManager.getAggregatedTotals(startDate, endDate), tags -> {
            // Requirement: "dig deeper into the aggregation to see the exact transactions"
            List<Transaction> filtered = transactionManager.getFilteredTransactions(startDate, endDate, tags);
            transactionAdapter.setTransactions(filtered);
            Toast.makeText(this, "Showing details for: " + String.join(", ", tags), Toast.LENGTH_SHORT).show();
        });
        rvAggregations.setAdapter(aggregationAdapter);

        // Simple list for all (or filtered) transactions
        RecyclerView rvTransactions = findViewById(R.id.rv_transactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(new ArrayList<>());
        rvTransactions.setAdapter(transactionAdapter);

        // Task 2: Initiate process
        findViewById(R.id.btn_process_sms).setOnClickListener(v -> {
            if (checkPermission()) {
                processSms();
            } else {
                requestPermission();
            }
        });

        // Task 1: Open Generator
        findViewById(R.id.btn_open_generator).setOnClickListener(v -> {
            startActivity(new Intent(this, SmsGeneratorActivity.class));
        });

        // Requirement: Add new rules/filters at runtime
        findViewById(R.id.btn_add_rule).setOnClickListener(v -> showAddEntryDialog());

        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Requirement: Choose per week, month, year
                updateDateRange(position);
                refreshUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupDefaultRules() {
        // Default regex and mapping for ABSA and WFS
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("account", 1);
        mapping.put("type", 2);
        mapping.put("date", 3);
        mapping.put("description", 4);
        mapping.put("amount", 5);

        transactionManager.addBankPattern(new BankPattern("ABSA", "Absa: (\\w+), ([^,]+), (\\d{2}/\\d{2}/\\d{2}) .*? - ([^,]+), (R-?[\\d,.]+), Available", mapping));
        transactionManager.addBankPattern(new BankPattern("WFS", "WFS : (\\w+), ([^,.]+)[,.] (\\d{2}/\\d{2}/\\d{2}) .*?, ([^,]+), (R[\\d,.]+), Total Avail", mapping));

        // Default tagging rules
        transactionManager.addTaggingRule(new TaggingRule("Airtime", new ArrayList<>(Arrays.asList("Expense", "Utility"))));
        transactionManager.addTaggingRule(new TaggingRule("Rental", new ArrayList<>(Arrays.asList("Expense", "Accommodation"))));
        transactionManager.addTaggingRule(new TaggingRule("Work", new ArrayList<>(Arrays.asList("Income", "Salary"))));
        transactionManager.addTaggingRule(new TaggingRule("Steers", new ArrayList<>(Arrays.asList("Expense", "Food"))));
        transactionManager.addTaggingRule(new TaggingRule("SeattleCoff", new ArrayList<>(Arrays.asList("Expense", "Food"))));
    }

    private void processSms() {
        transactionManager.clearTransactions();
        new SmsProcessor(this, transactionManager);
        refreshUI();
        Toast.makeText(this, "Inbox Processed", Toast.LENGTH_SHORT).show();
    }

    private void refreshUI() {
        // Show all transactions by default in the main list
        List<Transaction> all = transactionManager.getFilteredTransactions(startDate, endDate, null);
        transactionAdapter.setTransactions(all);
        
        // Update the category totals
        Map<List<String>, Double> aggregates = transactionManager.getAggregatedTotals(startDate, endDate);
        aggregationAdapter.updateData(aggregates);

        double total = transactionManager.calculateTotal(all);
        tvSummary.setText(String.format("Total: R%.2f", total));
    }

    private void updateDateRange(int position) {
        Calendar cal = Calendar.getInstance();
        endDate = cal.getTime();
        switch (position) {
            case 0: startDate = new Date(0); break; // All Time
            case 1: cal.add(Calendar.DAY_OF_YEAR, -7); startDate = cal.getTime(); break; // Week
            case 2: cal.add(Calendar.MONTH, -1); startDate = cal.getTime(); break; // Month
            case 3: cal.add(Calendar.YEAR, -1); startDate = cal.getTime(); break; // Year
        }
    }

    private void showAddEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_entry, null);
        
        RadioGroup rgType = view.findViewById(R.id.rg_type);
        View layoutTag = view.findViewById(R.id.layout_tag_rule);
        View layoutBank = view.findViewById(R.id.layout_bank_pattern);
        View layoutAgg = view.findViewById(R.id.layout_agg_filter);
        
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            layoutTag.setVisibility(checkedId == R.id.rb_tag_rule ? View.VISIBLE : View.GONE);
            layoutBank.setVisibility(checkedId == R.id.rb_bank_pattern ? View.VISIBLE : View.GONE);
            layoutAgg.setVisibility(checkedId == R.id.rb_agg_filter ? View.VISIBLE : View.GONE);
        });

        builder.setView(view);
        builder.setTitle("Add Rule / Pattern / Filter");
        builder.setPositiveButton("Add", (dialog, which) -> {
            int checked = rgType.getCheckedRadioButtonId();
            if (checked == R.id.rb_tag_rule) {
                String kw = ((EditText)view.findViewById(R.id.et_keyword)).getText().toString();
                String tags = ((EditText)view.findViewById(R.id.et_tags)).getText().toString();
                if (!kw.isEmpty()) {
                    List<String> tagList = Arrays.stream(tags.split(",")).map(String::trim).collect(Collectors.toList());
                    transactionManager.addTaggingRule(new TaggingRule(kw.trim(), new ArrayList<>(tagList)));
                }
            } else if (checked == R.id.rb_bank_pattern) {
                String name = ((EditText)view.findViewById(R.id.et_bank_name)).getText().toString();
                String regex = ((EditText)view.findViewById(R.id.et_regex)).getText().toString();
                Map<String, Integer> map = new HashMap<>();
                map.put("account", getInt(view, R.id.et_group_acc));
                map.put("type", getInt(view, R.id.et_group_type));
                map.put("date", getInt(view, R.id.et_group_date));
                map.put("description", getInt(view, R.id.et_group_desc));
                map.put("amount", getInt(view, R.id.et_group_amount));
                transactionManager.addBankPattern(new BankPattern(name, regex, map));
            } else if (checked == R.id.rb_agg_filter) {
                String tags = ((EditText)view.findViewById(R.id.et_agg_tags)).getText().toString();
                if (!tags.isEmpty()) {
                    List<String> tagList = Arrays.stream(tags.split(",")).map(String::trim).collect(Collectors.toList());
                    transactionManager.addAggregateFilter(new ArrayList<>(tagList));
                    refreshUI();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private int getInt(View v, int id) {
        try { return Integer.parseInt(((EditText)v.findViewById(id)).getText().toString()); } catch (Exception e) { return 0; }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            processSms();
        }
    }
}
