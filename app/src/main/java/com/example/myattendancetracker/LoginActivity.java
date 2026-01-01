package com.example.myattendancetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private RadioGroup radioGroupRole;
    private RadioButton rbTeacher, rbStudent;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // <-- your XML file name

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        rbTeacher = findViewById(R.id.rbTeacher);
        rbStudent = findViewById(R.id.rbStudent);
        btnLogin = findViewById(R.id.btnLogin);

        // 👉 Add your login click listener here
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                int selectedId = radioGroupRole.getCheckedRadioButtonId();

                String role = "";
                if (selectedId == R.id.rbTeacher) {
                    role = "Teacher";
                } else if (selectedId == R.id.rbStudent) {
                    role = "Student";
                }

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Login Successful\nEmail: " + email + "\nRole: " + role,
                            Toast.LENGTH_LONG).show();

                    // 👉 Navigate to ProfileActivity for both Teacher and Student
                    Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}

