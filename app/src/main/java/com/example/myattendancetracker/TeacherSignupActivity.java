package com.example.myattendancetracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TeacherSignupActivity extends AppCompatActivity {

    private EditText etTeacherName, etTeacherEmail, etTeacherPassword, etTeacherConfirmPassword;
    private Button btnTeacherSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_signup); // <-- your XML file name

        // Initialize views
        etTeacherName = findViewById(R.id.etTeacherName);
        etTeacherEmail = findViewById(R.id.etTeacherEmail);
        etTeacherPassword = findViewById(R.id.etTeacherPassword);
        etTeacherConfirmPassword = findViewById(R.id.etTeacherConfirmPassword);
        btnTeacherSignup = findViewById(R.id.btnTeacherSignup);

        // Handle signup button click
        btnTeacherSignup.setOnClickListener(v -> {
            String name = etTeacherName.getText().toString().trim();
            String email = etTeacherEmail.getText().toString().trim();
            String password = etTeacherPassword.getText().toString().trim();
            String confirmPassword = etTeacherConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                // For now, just show confirmation
                Toast.makeText(this,
                        "Teacher signed up:\n" + name + " (" + email + ")",
                        Toast.LENGTH_LONG).show();

                // 👉 Later: Save to Firebase Auth + Database
            }
        });
    }
}

