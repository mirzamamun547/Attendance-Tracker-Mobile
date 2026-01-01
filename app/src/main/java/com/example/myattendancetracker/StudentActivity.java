package com.example.myattendancetracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StudentActivity extends AppCompatActivity {

    private EditText etStudentName, etStudentEmail;
    private Button btnAddStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student); // <-- your XML file name

        // Initialize views
        etStudentName = findViewById(R.id.etStudentName);
        etStudentEmail = findViewById(R.id.etStudentEmail);
        btnAddStudent = findViewById(R.id.btnAddStudent);

        // Handle button click
        btnAddStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etStudentName.getText().toString().trim();
                String email = etStudentEmail.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty()) {
                    Toast.makeText(StudentActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // For now, just show a confirmation
                    Toast.makeText(StudentActivity.this,
                            "Student Added:\nName: " + name + "\nEmail: " + email,
                            Toast.LENGTH_LONG).show();

                    // 👉 Later: Save to Firebase Realtime Database or SQLite
                }
            }
        });
    }
}
