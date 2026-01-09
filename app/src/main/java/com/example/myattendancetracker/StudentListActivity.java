package com.example.myattendancetracker;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentListActivity extends AppCompatActivity {

    private RecyclerView rvStudents;
    private StudentAdapter adapter;
    private EditText etSearch;
    private Spinner spinnerClass;

    private List<Student> studentList = new ArrayList<>();
    private List<Student> filteredList = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        etSearch = findViewById(R.id.etSearch);
        spinnerClass = findViewById(R.id.spinnerClass);
        rvStudents = findViewById(R.id.rvStudents);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        adapter = new StudentAdapter(filteredList);
        rvStudents.setAdapter(adapter);

        setupSearch();
        setupClassFilter();
        loadTeacherStudents(); // ✅ Load students for the logged-in teacher
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudents(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClassFilter() {
        spinnerClass.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(String selectedClass) {
                filterStudents(etSearch.getText().toString());
            }
        });
    }

    private void loadTeacherStudents() {
        String teacherId = mAuth.getCurrentUser().getUid();

        db.collection("students")
                .whereEqualTo("teacherId", teacherId) // 🔑 Only this teacher's students
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    studentList.clear();
                    filteredList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        int roll = doc.getLong("roll") != null ? doc.getLong("roll").intValue() : 0;
                        String name = doc.getString("name");
                        String className = doc.getString("className");
                        int presentDays = doc.getLong("presentDays") != null ? doc.getLong("presentDays").intValue() : 0;
                        int totalDays = doc.getLong("totalDays") != null ? doc.getLong("totalDays").intValue() : 0;

                        Student s = new Student(roll, name, className, presentDays, totalDays);
                        studentList.add(s);
                    }

                    filteredList.addAll(studentList);
                    adapter.notifyDataSetChanged();

                    // Optionally populate spinnerClass with classes
                    populateClassSpinner();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load students: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void filterStudents(String query) {
        filteredList.clear();
        String selectedClass = spinnerClass.getSelectedItem() != null ? spinnerClass.getSelectedItem().toString() : "All";

        for (Student s : studentList) {
            boolean matchName = s.getName().toLowerCase().contains(query.toLowerCase());
            boolean matchClass = selectedClass.equals("All") || s.getClassName().equals(selectedClass);

            if (matchName && matchClass) {
                filteredList.add(s);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void populateClassSpinner() {
        // Optional: dynamically fill spinner with teacher's classes
        List<String> classes = new ArrayList<>();
        classes.add("All");
        for (Student s : studentList) {
            if (!classes.contains(s.getClassName())) {
                classes.add(s.getClassName());
            }
        }
        // Assuming you have an ArrayAdapter<String> for spinnerClass
        // spinnerClass.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classes));
    }
}
