package com.example.myattendancetracker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private final List<Student> students;

    public StudentAdapter(List<Student> students) {
        this.students = students;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_student_attendance, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student s = students.get(position);

        holder.tvRoll.setText(String.valueOf(s.getRoll()));
        holder.tvName.setText(s.getName());

        int percent = s.getPercentage();
        holder.tvPercentage.setText("Attendance: " + percent + "%");

        if (percent < 70) {
            holder.tvPercentage.setTextColor(Color.RED);
        } else {
            holder.tvPercentage.setTextColor(Color.parseColor("#16A34A"));
        }

        // Avoid recycling issue for CheckBox
        holder.cbPresent.setOnCheckedChangeListener(null);
        holder.cbPresent.setChecked(s.isPresent());
        holder.cbPresent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            s.setPresent(isChecked);
        });

        // ✅ Fix delete
        holder.btnDelete.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition(); // always get current position
            if (adapterPos != RecyclerView.NO_POSITION) {
                students.remove(adapterPos);
                notifyItemRemoved(adapterPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvRoll, tvName, tvPercentage;
        CheckBox cbPresent;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvRoll = itemView.findViewById(R.id.tvRoll);
            tvName = itemView.findViewById(R.id.tvName);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            cbPresent = itemView.findViewById(R.id.cbPresent);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
