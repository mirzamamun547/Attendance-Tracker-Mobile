package com.example.myattendancetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnStudent, btnTeacherLogin, btnTeacherSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStudent = findViewById(R.id.btnStudent);
        btnTeacherLogin = findViewById(R.id.btnTeacherLogin);
        btnTeacherSignup = findViewById(R.id.btnTeacherSignup);

        // Student → LoginActivity
        btnStudent.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Teacher Login → LoginActivity
        btnTeacherLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Teacher Signup → TeacherSignupActivity
        btnTeacherSignup.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TeacherSignupActivity.class);
            startActivity(intent);
        });
    }
}
