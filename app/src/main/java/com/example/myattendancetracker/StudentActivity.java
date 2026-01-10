package com.example.myattendancetracker;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class StudentActivity extends AppCompatActivity {

    private TextView tvRoll, tvName, tvCourse;
    private Spinner spinnerClass;
    private RecyclerView rvAttendance;
    private EditText etReason;
    private Button btnSubmitReason;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String studentEmail;
    private String studentUid;

    private List<String> classList = new ArrayList<>();
    private ArrayAdapter<String> classAdapter;

    private List<AttendanceRecord> attendanceList = new ArrayList<>();
    private AttendanceAdapter attendanceAdapter;

    // Map spinner item -> teacherId (to handle multiple teachers with same class)
    private Map<String, String> spinnerClassToTeacher = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Initialize views
        tvRoll = findViewById(R.id.tvRoll);
        tvName = findViewById(R.id.tvName);

        spinnerClass = findViewById(R.id.spinnerClass);
        rvAttendance = findViewById(R.id.rvAttendance);
        etReason = findViewById(R.id.etReason);
        btnSubmitReason = findViewById(R.id.btnSubmitReason);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            studentEmail = user.getEmail();
            studentUid = user.getUid();
        }

        // RecyclerView setup
        rvAttendance.setLayoutManager(new LinearLayoutManager(this));
        attendanceAdapter = new AttendanceAdapter(attendanceList);
        rvAttendance.setAdapter(attendanceAdapter);

        // Spinner setup
        classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(classAdapter);

        // Load student info (Roll, Name, Course)
        loadStudentInfo();

        // Load all classes for this student
        loadStudentClasses();

        // Spinner selection
        spinnerClass.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(String selectedClass) {
                loadAttendance(selectedClass);
            }
        });

        // Submit absence reason
        btnSubmitReason.setOnClickListener(v -> submitReason());
    }

    // ---------------- LOAD STUDENT INFO ----------------
    private void loadStudentInfo() {
        if (studentEmail == null) return;

        db.collection("students")
                .whereEqualTo("email", studentEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) return;

                    // Take first valid document for profile info
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        if (doc.contains("name") && doc.contains("roll")) {
                            tvRoll.setText(String.valueOf(doc.getLong("roll")));
                            tvName.setText(doc.getString("name"));

                            break;
                        }
                    }
                });
    }

    // ---------------- LOAD STUDENT CLASSES ----------------
    private void loadStudentClasses() {
        if (studentEmail == null) return;

        db.collection("students")
                .whereEqualTo("email", studentEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    classList.clear();
                    spinnerClassToTeacher.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String className = doc.getString("className");
                        String teacherId = doc.getString("teacherId");

                        if (className != null && teacherId != null) {
                            String uniqueClass = className + " (" + teacherId.substring(0, 5) + ")";
                            if (!classList.contains(uniqueClass)) {
                                classList.add(uniqueClass);
                                spinnerClassToTeacher.put(uniqueClass, teacherId); // store actual teacherId
                            }
                        }
                    }

                    if (!classList.isEmpty()) {
                        classAdapter.notifyDataSetChanged();
                        spinnerClass.setSelection(0);

                        // Load attendance for first class automatically
                        loadAttendance(classList.get(0));
                    } else {
                        Toast.makeText(this, "No classes found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ---------------- LOAD ATTENDANCE ----------------
    private void loadAttendance(String spinnerItem) {
        if (studentEmail == null || spinnerItem == null) return;

        // Extract className from spinner (removes teacher hint)
        String className = spinnerItem.split(" \\(")[0];
        String teacherId = spinnerClassToTeacher.get(spinnerItem);
        if (teacherId == null) return;

        db.collection("attendance")
                .whereEqualTo("className", className)
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(attSnap -> {
                    attendanceList.clear();

                    for (QueryDocumentSnapshot attDoc : attSnap) {

                        Map<String, Boolean> attendanceMap =
                                (Map<String, Boolean>) attDoc.get("attendance");

                        Map<String, String> emailMap =
                                (Map<String, String>) attDoc.get("attendanceEmails");

                        boolean present = false;

                        if (attendanceMap != null && emailMap != null) {
                            // Find UID corresponding to this student's email
                            for (Map.Entry<String, String> entry : emailMap.entrySet()) {
                                String uid = entry.getKey();
                                String email = entry.getValue();

                                if (studentEmail.equals(email)) {
                                    // Check if this UID is marked present
                                    present = Boolean.TRUE.equals(attendanceMap.get(uid));
                                    break;
                                }
                            }
                        }

                        long dateMillis = attDoc.getLong("dateMillis") != null
                                ? attDoc.getLong("dateMillis") : 0;

                        String date = new SimpleDateFormat(
                                "yyyy-MM-dd", Locale.getDefault()
                        ).format(new Date(dateMillis));

                        attendanceList.add(new AttendanceRecord(date, present));
                    }

                    attendanceAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load attendance: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }


    // ---------------- SUBMIT ABSENCE REASON ----------------
    private void submitReason() {
        String reason = etReason.getText().toString().trim();
        if (reason.isEmpty()) {
            Toast.makeText(this, "Please enter a reason", Toast.LENGTH_SHORT).show();
            return;
        }

        if (studentEmail == null) return;

        String selectedClass = spinnerClass.getSelectedItem() != null
                ? spinnerClass.getSelectedItem().toString() : "";

        Map<String, Object> data = new HashMap<>();
        data.put("studentEmail", studentEmail);
        data.put("className", selectedClass);
        data.put("reason", reason);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("absence_reasons")
                .add(data)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Reason submitted!", Toast.LENGTH_SHORT).show();
                    etReason.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to submit reason: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
