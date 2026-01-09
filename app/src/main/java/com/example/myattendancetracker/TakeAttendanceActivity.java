package com.example.myattendancetracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TakeAttendanceActivity extends AppCompatActivity {

    private ListView lvStudents;
    private Button btnSaveAttendance;

    private final List<Student> studentList = new ArrayList<>();
    private StudentAttendanceAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String className;
    private String section; // 🔹 NEW

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_attendance);

        lvStudents = findViewById(R.id.lvStudents);
        btnSaveAttendance = findViewById(R.id.btnSaveAttendance);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get both className and section from intent
        className = getIntent().getStringExtra("className");
        section = getIntent().getStringExtra("section");

        adapter = new StudentAttendanceAdapter(this, studentList);
        lvStudents.setAdapter(adapter);

        loadStudents();

        btnSaveAttendance.setOnClickListener(v -> saveAttendance());
    }

    // ---------------- LOAD STUDENTS ----------------
    private void loadStudents() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Teacher not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String teacherId = auth.getCurrentUser().getUid();

        db.collection("students")
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("className", className)  // 🔹 Filter by className
                .whereEqualTo("section", section)      // 🔹 Filter by section
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    studentList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {

                        String id = doc.getId();

                        int roll = doc.getLong("roll") != null
                                ? doc.getLong("roll").intValue() : 0;

                        String name = doc.getString("name") != null ? doc.getString("name") : "Unnamed";

                        int presentDays = doc.getLong("presentDays") != null
                                ? doc.getLong("presentDays").intValue() : 0;

                        int totalDays = doc.getLong("totalDays") != null
                                ? doc.getLong("totalDays").intValue() : 0;

                        // Create Student object
                        Student student = new Student(
                                id,
                                roll,
                                name,
                                className,
                                presentDays,
                                totalDays
                        );

                        studentList.add(student);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load students: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // ---------------- SAVE ATTENDANCE ----------------
    private void saveAttendance() {

        if (auth.getCurrentUser() == null) return;

        String teacherId = auth.getCurrentUser().getUid();

        Map<String, Boolean> attendanceMap = new HashMap<>();

        for (Student s : studentList) {
            attendanceMap.put(s.getId(), s.isPresent());
        }

        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("teacherId", teacherId);
        attendanceData.put("className", className);
        attendanceData.put("section", section); // 🔹 NEW
        attendanceData.put("date", System.currentTimeMillis());
        attendanceData.put("attendance", attendanceMap);

        db.collection("attendance")
                .add(attendanceData)
                .addOnSuccessListener(docRef -> {

                    // Update each student's attendance count
                    for (Student s : studentList) {
                        s.markAttendance();
                        db.collection("students")
                                .document(s.getId())
                                .update(
                                        "presentDays", s.getPresentDays(),
                                        "totalDays", s.getTotalDays()
                                );
                    }

                    Toast.makeText(this, "Attendance saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to save attendance: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}
