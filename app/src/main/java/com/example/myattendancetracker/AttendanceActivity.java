package com.example.myattendancetracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AttendanceActivity extends AppCompatActivity {

    private ListView lvClasses;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private List<String> classNames = new ArrayList<>();
    private List<String> classIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private Button btnSelectDate;
    private TextView tvSelectedDate;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        lvClasses = findViewById(R.id.lvClasses);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, classNames);
        lvClasses.setAdapter(adapter);

        loadTeacherClasses();

        // Date picker
        btnSelectDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(
                    AttendanceActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        String dateStr = dayOfMonth + "/" + (month + 1) + "/" + year;
                        tvSelectedDate.setText("Selected Date: " + dateStr);
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            dp.show();
        });

        lvClasses.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < classIds.size()) {
                String classId = classIds.get(position);
                String classDisplayName = classNames.get(position);

                String[] parts = classDisplayName.split(" - ");
                String className = parts.length > 0 ? parts[0] : "Unnamed";
                String section = parts.length > 1 ? parts[1] : "";

                Intent intent = new Intent(AttendanceActivity.this, TakeAttendanceActivity.class);
                intent.putExtra("classId", classId);
                intent.putExtra("className", className);
                intent.putExtra("section", section);

                long dateMillis = (selectedDate != null) ? selectedDate.getTimeInMillis() : System.currentTimeMillis();
                intent.putExtra("dateMillis", dateMillis);

                startActivity(intent);
            }
        });
    }

    private void loadTeacherClasses() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Teacher not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String teacherId = auth.getCurrentUser().getUid();

        db.collection("classes")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    classNames.clear();
                    classIds.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String className = doc.getString("className");
                        String section = doc.getString("section");

                        if (className == null) className = "Unnamed";
                        if (section == null) section = "";

                        classNames.add(className + " - " + section);
                        classIds.add(doc.getId());
                    }

                    if (classNames.isEmpty()) {
                        Toast.makeText(this, "No classes found for this teacher", Toast.LENGTH_SHORT).show();
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load classes: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
