package com.example.myattendancetracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddStudentActivity extends AppCompatActivity {

    private EditText etStudentName, etStudentEmail, etStudentClass;
    private Button btnAddStudent;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        etStudentName = findViewById(R.id.etStudentName);
        etStudentEmail = findViewById(R.id.etStudentEmail);
        etStudentClass = findViewById(R.id.etStudentClass); // Class/Section
        btnAddStudent = findViewById(R.id.btnAddStudent);

        btnAddStudent.setOnClickListener(v -> addStudentToFirestore());
    }

    private void addStudentToFirestore() {
        String name = etStudentName.getText().toString().trim();
        String email = etStudentEmail.getText().toString().trim();
        String classSection = etStudentClass.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || classSection.isEmpty()) {
            Toast.makeText(this, "Please enter name, email, and class/section", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current teacher ID
        String teacherId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        if (teacherId.isEmpty()) {
            Toast.makeText(this, "Teacher not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare student data
        Map<String, Object> student = new HashMap<>();
        student.put("name", name);
        student.put("email", email);
        student.put("role", "Student");
        student.put("classSection", classSection);
        student.put("teacherId", teacherId); // Link student to teacher
        student.put("presentDays", 0);       // Initialize attendance
        student.put("totalDays", 0);

        // Save to Firestore under "students" collection
        db.collection("students")
                .add(student)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Student added successfully!", Toast.LENGTH_SHORT).show();
                    etStudentName.setText("");
                    etStudentEmail.setText("");
                    etStudentClass.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
