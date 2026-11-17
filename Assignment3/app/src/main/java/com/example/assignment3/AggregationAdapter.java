package com.example.assignment3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AggregationAdapter extends RecyclerView.Adapter<AggregationAdapter.ViewHolder> {

    private List<Map.Entry<List<String>, Double>> aggregations;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(List<String> tags);
    }

    public AggregationAdapter(Map<List<String>, Double> data, OnItemClickListener listener) {
        this.aggregations = new ArrayList<>(data.entrySet());
        this.listener = listener;
    }

    public void updateData(Map<List<String>, Double> data) {
        this.aggregations = new ArrayList<>(data.entrySet());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aggregation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<List<String>, Double> entry = aggregations.get(position);
        List<String> tags = entry.getKey();
        
        // Show tags as a single string
        String tagText = String.join(" + ", tags);
        holder.tvTagName.setText(tagText);
        
        holder.tvTagTotal.setText(String.format("R%.2f", entry.getValue()));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(tags));
    }

    @Override
    public int getItemCount() {
        return aggregations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTagName, tvTagTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTagName = itemView.findViewById(R.id.tv_tag_name);
            tvTagTotal = itemView.findViewById(R.id.tv_tag_total);
        }
    }
}
