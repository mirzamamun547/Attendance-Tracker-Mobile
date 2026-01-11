package com.example.myattendancetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    public interface OnReasonClickListener {
        void onReasonClick(AttendanceRecord record, int position);
    }

    private List<AttendanceRecord> list;
    private OnReasonClickListener listener;

    public AttendanceAdapter(List<AttendanceRecord> list, OnReasonClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord record = list.get(position);
        holder.tvDate.setText(record.getDate());
        holder.tvStatus.setText(record.isPresent() ? "Present" : "Absent");

        if (!record.isPresent() && !record.hasReason()) {
            holder.btnReason.setVisibility(View.VISIBLE);
            holder.btnReason.setOnClickListener(v -> {
                if (listener != null) listener.onReasonClick(record, position);
            });
        } else {
            holder.btnReason.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStatus;
        Button btnReason;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnReason = itemView.findViewById(R.id.btnReason);
        }
    }
}
