package com.example.myattendancetracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddClassActivity extends AppCompatActivity {

    private EditText etClassName, etSectionName;
    private Button btnAddClass;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_classes);


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        etClassName = findViewById(R.id.etClassName);
        etSectionName = findViewById(R.id.etSectionName);
        btnAddClass = findViewById(R.id.btnAddClass);

        btnAddClass.setOnClickListener(v -> addClassToFirestore());
    }

    private void addClassToFirestore() {
        String className = etClassName.getText().toString().trim();
        String section = etSectionName.getText().toString().trim();

        if (className.isEmpty() || section.isEmpty()) {
            Toast.makeText(this, "Please enter class name and section", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show();
            return;
        }


        Map<String, Object> classData = new HashMap<>();
        classData.put("className", className);
        classData.put("section", section);
        classData.put("teacherId", user.getUid());


        db.collection("classes")
                .add(classData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Class added successfully!", Toast.LENGTH_SHORT).show();
                    etClassName.setText("");
                    etSectionName.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding class: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
