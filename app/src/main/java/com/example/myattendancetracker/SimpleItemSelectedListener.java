package com.example.myattendancetracker;

import android.view.View;
import android.widget.AdapterView;

public abstract class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        onItemSelected(parent.getItemAtPosition(position).toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    public abstract void onItemSelected(String selectedClass);
}
