package com.example.myattendancetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ViewHolder> {

    private final String[] classes;

    public ClassAdapter(String[] classes) {
        this.classes = classes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvClassName.setText(classes[position]);
    }

    @Override
    public int getItemCount() {
        return classes.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName;
        ViewHolder(View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(android.R.id.text1);
        }
    }
}
