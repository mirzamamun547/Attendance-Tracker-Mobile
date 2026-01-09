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
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        etSearch = findViewById(R.id.etSearch);
        spinnerClass = findViewById(R.id.spinnerClass);
        rvStudents = findViewById(R.id.rvStudents);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new StudentAdapter(filteredList);
        rvStudents.setAdapter(adapter);

        setupSearch();
        loadClassesForSpinner();
        loadStudents();
    }

    // 🔍 SEARCH
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudents();
            }
        });
    }

    // 📥 LOAD STUDENTS
    private void loadStudents() {

        if (auth.getCurrentUser() == null) return;

        String teacherId = auth.getCurrentUser().getUid();

        db.collection("students")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    studentList.clear();
                    filteredList.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        String id = doc.getId();
                        int roll = doc.getLong("roll") != null
                                ? doc.getLong("roll").intValue() : 0;

                        String name = doc.getString("name");
                        String className = doc.getString("className");

                        int presentDays = doc.getLong("presentDays") != null
                                ? doc.getLong("presentDays").intValue() : 0;

                        int totalDays = doc.getLong("totalDays") != null
                                ? doc.getLong("totalDays").intValue() : 0;

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
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // 🎓 LOAD CLASSES INTO SPINNER (FROM CLASSES COLLECTION)
    private void loadClassesForSpinner() {

        if (auth.getCurrentUser() == null) return;

        String teacherId = auth.getCurrentUser().getUid();

        List<String> classes = new ArrayList<>();
        classes.add("All");

        db.collection("classes")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String className = doc.getString("className");
                        if (className != null && !classes.contains(className)) {
                            classes.add(className);
                        }
                    }

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item,
                                    classes);

                    adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);

                    spinnerClass.setAdapter(adapter);

                    spinnerClass.setOnItemSelectedListener(
                            new SimpleItemSelectedListener() {
                                @Override
                                public void onItemSelected(String item) {
                                    filterStudents();
                                }
                            }
                    );
                });
    }

    // 🧠 FILTER STUDENTS
    private void filterStudents() {

        filteredList.clear();

        String searchText = etSearch.getText().toString().toLowerCase();
        String selectedClass = spinnerClass.getSelectedItem().toString();

        for (Student s : studentList) {

            boolean matchName =
                    s.getName() != null &&
                            s.getName().toLowerCase().contains(searchText);

            boolean matchClass =
                    selectedClass.equals("All") ||
                            s.getClassName().equals(selectedClass);

            if (matchName && matchClass) {
                filteredList.add(s);
            }
        }

        adapter.notifyDataSetChanged();
    }
}
