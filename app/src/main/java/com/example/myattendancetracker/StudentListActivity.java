package com.example.myattendancetracker;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
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

    private final List<Student> studentList = new ArrayList<>();
    private final List<Student> filteredList = new ArrayList<>();

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
        loadTeacherStudents();
    }

    // ---------------- SEARCH ----------------
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudents(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ---------------- CLASS FILTER ----------------
    private void setupClassFilter() {
        spinnerClass.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(String selectedClass) {
                filterStudents(etSearch.getText().toString());
            }
        });
    }

    // ---------------- LOAD STUDENTS ----------------
    private void loadTeacherStudents() {

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Teacher not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String teacherId = mAuth.getCurrentUser().getUid();

        db.collection("students")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    studentList.clear();
                    filteredList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {

                        // 🔑 Firestore document ID
                        String id = doc.getId();

                        int roll = doc.getLong("roll") != null
                                ? doc.getLong("roll").intValue() : 0;

                        String name = doc.getString("name");

                        // 🔴 MUST MATCH AddStudentActivity FIELD NAME
                        String className = doc.getString("classSection");

                        int presentDays = doc.getLong("presentDays") != null
                                ? doc.getLong("presentDays").intValue() : 0;

                        int totalDays = doc.getLong("totalDays") != null
                                ? doc.getLong("totalDays").intValue() : 0;

                        // ✅ CORRECT CONSTRUCTOR
                        Student student = new Student(
                                id,
                                roll,
                                name,
                                className,
                                presentDays,
                                totalDays
                        );

                        studentList.add(student);
                    }

                    filteredList.addAll(studentList);
                    adapter.notifyDataSetChanged();
                    populateClassSpinner();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load students: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // ---------------- FILTER LOGIC ----------------
    private void filterStudents(String query) {
        filteredList.clear();

        String selectedClass = spinnerClass.getSelectedItem() != null
                ? spinnerClass.getSelectedItem().toString()
                : "All";

        for (Student s : studentList) {

            boolean matchName = s.getName() != null &&
                    s.getName().toLowerCase().contains(query.toLowerCase());

            boolean matchClass = selectedClass.equals("All") ||
                    (s.getClassName() != null && s.getClassName().equals(selectedClass));

            if (matchName && matchClass) {
                filteredList.add(s);
            }
        }

        adapter.notifyDataSetChanged();
    }

    // ---------------- CLASS SPINNER ----------------
    private void populateClassSpinner() {

        List<String> classes = new ArrayList<>();
        classes.add("All");

        for (Student s : studentList) {
            if (s.getClassName() != null && !classes.contains(s.getClassName())) {
                classes.add(s.getClassName());
            }
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        classes);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(adapter);
    }
}
