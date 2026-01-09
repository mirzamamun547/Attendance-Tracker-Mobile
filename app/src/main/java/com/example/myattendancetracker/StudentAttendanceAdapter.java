package com.example.myattendancetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class StudentAttendanceAdapter extends BaseAdapter {

    private final Context context;
    private final List<Student> students;

    public StudentAttendanceAdapter(Context context, List<Student> students) {
        this.context = context;
        this.students = students;
    }

    @Override
    public int getCount() {
        return students.size();
    }

    @Override
    public Object getItem(int position) {
        return students.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.row_student_attendance_checkbox, parent, false);
        }

        Student student = students.get(position);

        TextView tvName = convertView.findViewById(R.id.tvName);
        CheckBox cbPresent = convertView.findViewById(R.id.cbPresent);

        // Avoid null pointer if name is null
        tvName.setText(student.getName() != null ? student.getName() : "No Name");

        cbPresent.setOnCheckedChangeListener(null);
        cbPresent.setChecked(student.isPresent());
        cbPresent.setOnCheckedChangeListener((buttonView, isChecked) -> student.setPresent(isChecked));

        return convertView;
    }
}
