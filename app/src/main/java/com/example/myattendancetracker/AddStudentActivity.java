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

    private EditText etStudentName, etStudentEmail, etStudentClass, etStudentRoll;
    private Button btnAddStudent;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etStudentName = findViewById(R.id.etStudentName);
        etStudentEmail = findViewById(R.id.etStudentEmail);
        etStudentClass = findViewById(R.id.etStudentClass);
        etStudentRoll = findViewById(R.id.etStudentRoll); // ✅ ADD THIS
        btnAddStudent = findViewById(R.id.btnAddStudent);

        btnAddStudent.setOnClickListener(v -> addStudent());
    }

    private void addStudent() {

        String name = etStudentName.getText().toString().trim();
        String email = etStudentEmail.getText().toString().trim();
        String className = etStudentClass.getText().toString().trim();
        String rollStr = etStudentRoll.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || className.isEmpty() || rollStr.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String teacherId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (teacherId == null) {
            Toast.makeText(this, "Teacher not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        int roll = Integer.parseInt(rollStr);

        Map<String, Object> student = new HashMap<>();
        student.put("name", name);
        student.put("email", email);
        student.put("roll", roll);
        student.put("className", className);
        student.put("teacherId", teacherId);
        student.put("presentDays", 0);
        student.put("totalDays", 0);

        db.collection("students")
                .add(student)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        etStudentName.setText("");
        etStudentEmail.setText("");
        etStudentClass.setText("");
        etStudentRoll.setText("");
    }
}
