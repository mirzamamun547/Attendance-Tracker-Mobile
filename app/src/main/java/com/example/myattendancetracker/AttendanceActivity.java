package com.example.myattendancetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AttendanceActivity extends AppCompatActivity {

    private ListView lvClasses;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private List<String> classNames = new ArrayList<>();
    private List<String> classIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        lvClasses = findViewById(R.id.lvClasses);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, classNames);
        lvClasses.setAdapter(adapter);

        loadTeacherClasses();

        lvClasses.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < classIds.size() && position < classNames.size()) {
                String classId = classIds.get(position);
                String classDisplayName = classNames.get(position);

                // Split into className and section for TakeAttendanceActivity
                String[] parts = classDisplayName.split(" - ");
                String className = parts[0];
                String section = parts.length > 1 ? parts[1] : "";

                Intent intent = new Intent(AttendanceActivity.this, TakeAttendanceActivity.class);
                intent.putExtra("classId", classId);
                intent.putExtra("className", className);
                intent.putExtra("section", section);
                startActivity(intent);
            }
        });
    }

    private void loadTeacherClasses() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Teacher not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String teacherId = auth.getCurrentUser().getUid();

        db.collection("classes")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    classNames.clear();
                    classIds.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String className = doc.getString("className");
                        String section = doc.getString("section");

                        if (className == null) className = "Unnamed";
                        if (section == null) section = "";

                        String displayName = className + " - " + section;

                        classNames.add(displayName);
                        classIds.add(doc.getId());
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load classes: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
