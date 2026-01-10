package com.example.myattendancetracker;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

public class ClassSpinnerAdapter extends ArrayAdapter<String> {
    public ClassSpinnerAdapter(Context context, List<String> classes) {
        super(context, android.R.layout.simple_spinner_item, classes);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
}
