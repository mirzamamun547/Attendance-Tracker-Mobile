package com.example.myattendancetracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TakeAttendanceActivity extends AppCompatActivity {

    private TextView tvClassInfo, tvDateInfo;
    private ListView lvStudents;
    private Button btnSaveAttendance;

    private final List<Student> studentList = new ArrayList<>();
    private StudentAttendanceAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String classId, className, section;
    private long dateMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_attendance);

        tvClassInfo = findViewById(R.id.tvClassInfo);
        tvDateInfo = findViewById(R.id.tvDateInfo);
        lvStudents = findViewById(R.id.lvStudents);
        btnSaveAttendance = findViewById(R.id.btnSaveAttendance);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 🔹 Get data from AttendanceActivity
        classId = getIntent().getStringExtra("classId");
        className = getIntent().getStringExtra("className");
        section = getIntent().getStringExtra("section");
        dateMillis = getIntent().getLongExtra("dateMillis", System.currentTimeMillis());

        // Show selected class & date
        tvClassInfo.setText("Class: " + className + " - " + section);

        String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(dateMillis));
        tvDateInfo.setText("Date: " + dateStr);

        adapter = new StudentAttendanceAdapter(this, studentList);
        lvStudents.setAdapter(adapter);

        loadStudents();

        btnSaveAttendance.setOnClickListener(v -> saveAttendance());
    }

    // ---------------- LOAD STUDENTS ----------------
    private void loadStudents() {

        if (auth.getCurrentUser() == null) return;

        String teacherId = auth.getCurrentUser().getUid();

        db.collection("students")
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("className", className)

                .get()
                .addOnSuccessListener(querySnapshot -> {

                    studentList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {

                        String id = doc.getId();
                        int roll = doc.getLong("roll") != null ? doc.getLong("roll").intValue() : 0;
                        String name = doc.getString("name");

                        int presentDays = doc.getLong("presentDays") != null ? doc.getLong("presentDays").intValue() : 0;
                        int totalDays = doc.getLong("totalDays") != null ? doc.getLong("totalDays").intValue() : 0;

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
                        Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show()
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
        attendanceData.put("classId", classId);
        attendanceData.put("className", className);
        attendanceData.put("section", section);
        attendanceData.put("dateMillis", dateMillis);
        attendanceData.put("attendance", attendanceMap);

        db.collection("attendance")
                .add(attendanceData)
                .addOnSuccessListener(docRef -> {

                    // update student stats
                    for (Student s : studentList) {
                        s.markAttendance();
                        db.collection("students")
                                .document(s.getId())
                                .update(
                                        "presentDays", s.getPresentDays(),
                                        "totalDays", s.getTotalDays()
                                );
                    }

                    Toast.makeText(this, "Attendance saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save attendance", Toast.LENGTH_SHORT).show()
                );
    }
}
