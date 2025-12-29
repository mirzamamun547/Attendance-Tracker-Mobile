package com.example.myattendancetracker;



import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics firebaseAnalytics;
    private FirebaseAuth auth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize Realtime Database reference
        database = FirebaseDatabase.getInstance().getReference();

        // Test Realtime Database connection
        Map<String, Object> testData = new HashMap<>();
        testData.put("message", "Firebase connected");

        database.child("test").child("hello")
                .setValue(testData)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Realtime Database write success");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error: " + e.getMessage());
                });
    }
}
