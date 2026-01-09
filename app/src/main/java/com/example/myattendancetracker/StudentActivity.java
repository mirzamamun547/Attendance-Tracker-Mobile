package com.example.myattendancetracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StudentActivity extends AppCompatActivity {

    private RecyclerView rvClasses;
    private EditText etReason;
    private Button btnSubmitReason;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String studentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Initialize views
        rvClasses = findViewById(R.id.rvClasses);
        etReason = findViewById(R.id.etReason);
        btnSubmitReason = findViewById(R.id.btnSubmitReason);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) studentUid = user.getUid();

        // Setup RecyclerView (just dummy data for now)
        rvClasses.setLayoutManager(new LinearLayoutManager(this));
        String[] classes = {"Math", "Science", "History"}; // Replace with actual classes from Firestore
        ClassAdapter adapter = new ClassAdapter(classes);
        rvClasses.setAdapter(adapter);

        // Submit absence reason
        btnSubmitReason.setOnClickListener(v -> submitReason());
    }

    private void submitReason() {
        String reason = etReason.getText().toString().trim();

        if (reason.isEmpty()) {
            Toast.makeText(this, "Please enter a reason", Toast.LENGTH_SHORT).show();
            return;
        }

        if (studentUid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save reason to Firestore under "absence_reasons" collection
        Map<String, Object> data = new HashMap<>();
        data.put("studentUid", studentUid);
        data.put("reason", reason);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("absence_reasons")
                .add(data)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Reason submitted!", Toast.LENGTH_SHORT).show();
                    etReason.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
