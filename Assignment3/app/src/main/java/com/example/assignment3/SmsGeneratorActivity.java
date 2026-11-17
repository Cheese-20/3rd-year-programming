package com.example.assignment3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SmsGeneratorActivity extends AppCompatActivity {

    private EditText etTargetPort, etBatchCount, etDaysBack, etCustomMessage;
    private Random random = new Random();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

    private String[] absaTemplates = {
        "Absa: CHEQ1234, Pur, %s SETTLEMENT/C - AIRTIME DEBIT, MTN: 0264851994, R-%.2f, Available R1,000.00. Help 0860008600; ACCID 001",
        "Absa: CHEQ1234, Wthdr, %s RASC PE - ATM WITHDRAWAL, R-%.2f, Available R500.00. Help 0860008600; ACCID 001",
        "Absa: CHEQ1234, Dep, %s SETTLEMENT/C - MY PLACE OF WORK, R%.2f, Available R21,000.00. Help 0860008600; ACCID 001",
        "Absa: CHEQ1234, Pmnt, %s SETTLEMENT/C - DIGITAL PAYMENT DT, MY RENTAL ACCOMMODATION, R-%.2f, Available R11,000.00. Help 0860008600; ACCID 001",
        "Absa: CHEQ1234, Sch t, %s SETTLEMENT/C - ACB DEBIT:EXTERNAL, ATLAS SEC 12345 NETCASH, R-%.2f, Available R8,500.00. Help 0860008600; ACCID 001"
    };

    private String[] wfsTemplates = {
        "WFS : CCRD1013, Pur, %s AUTHORIZATION, SeattleCoff Walmer Eastern, R%.2f, Total Avail Bal R1,000.00. Help 0861502005; CCID 004",
        "WFS : CCRD1000, Transf. %s INTERNAL FUNDS TRANSFER, ***1018, R%.2f, Total Avail Bal R21,000.00. Help 0861502005; CCID 004"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_generator);

        etTargetPort = findViewById(R.id.et_target_port);
        etBatchCount = findViewById(R.id.et_batch_count);
        etDaysBack = findViewById(R.id.et_days_back);
        etCustomMessage = findViewById(R.id.et_custom_message);

        findViewById(R.id.btn_generate_random).setOnClickListener(v -> sendRandomBatch());
        findViewById(R.id.btn_send_custom).setOnClickListener(v -> sendCustomSms());
        findViewById(R.id.btn_back_to_watcher).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void sendRandomBatch() {
        if (!checkSmsPermission()) return;

        int count = getIntFromEt(etBatchCount, 5);
        String target = etTargetPort.getText().toString();

        for (int i = 0; i < count; i++) {
            String msg = generateRandomMessage();
            sendSms(target, msg);
        }
        Toast.makeText(this, "Sent " + count + " messages to " + target, Toast.LENGTH_SHORT).show();
    }

    private String generateRandomMessage() {
        int bankType = random.nextInt(2);
        String date = sdf.format(getRandomDate());
        double amount = 50 + (5000 - 50) * random.nextDouble();

        if (bankType == 0) {
            String template = absaTemplates[random.nextInt(absaTemplates.length)];
            return String.format(Locale.US, template, date, amount);
        } else {
            String template = wfsTemplates[random.nextInt(wfsTemplates.length)];
            return String.format(Locale.US, template, date, amount);
        }
    }

    private Date getRandomDate() {
        int daysBack = getIntFromEt(etDaysBack, 30);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -random.nextInt(daysBack + 1));
        return cal.getTime();
    }

    private void sendCustomSms() {
        if (!checkSmsPermission()) return;

        String target = etTargetPort.getText().toString();
        String msg = etCustomMessage.getText().toString();

        if (!msg.isEmpty()) {
            sendSms(target, msg);
            Toast.makeText(this, "Custom SMS sent", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Message is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSms(String target, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(target, null, message, null, null);
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private int getIntFromEt(EditText et, int defaultValue) {
        try {
            return Integer.parseInt(et.getText().toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 101);
            return false;
        }
        return true;
    }
}
