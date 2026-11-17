package com.example.assignment3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = transactions.get(position);
        
        holder.tvDescription.setText("Desc: " + t.getTransactionDescription());
        holder.tvDate.setText("Date: " + (t.getTransactionDate() != null ? dateFormat.format(t.getTransactionDate()) : "N/A"));
        holder.tvAmount.setText("Amount: R" + String.format("%.2f", t.getTransactionAmount()));
        holder.tvTags.setText("Tags: " + String.join(", ", t.getTags()));
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvDate, tvTags, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTags = itemView.findViewById(R.id.tv_tags);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}
