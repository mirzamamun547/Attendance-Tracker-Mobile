package com.example.myattendancetracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StudentActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;

    private TextView tvRoll, tvName;
    private Spinner spinnerClass;
    private RecyclerView rvAttendance;
    private ImageView ivProfile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String studentEmail;
    private Uri imageUri;

    private List<String> classList = new ArrayList<>();
    private ArrayAdapter<String> classAdapter;

    private List<AttendanceRecord> attendanceList = new ArrayList<>();
    private AttendanceAdapter attendanceAdapter;

    private Map<String, String> spinnerClassToTeacher = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // --- Initialize Views ---
        tvRoll = findViewById(R.id.tvRoll);
        tvName = findViewById(R.id.tvName);
        spinnerClass = findViewById(R.id.spinnerClass);
        rvAttendance = findViewById(R.id.rvAttendance);
        ivProfile = findViewById(R.id.ivProfile);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) studentEmail = user.getEmail();

        // Load profile image from Firestore by email
        loadStudentProfileImage();

        // Click profile image to upload new photo
        ivProfile.setOnClickListener(v -> openImagePicker());

        // RecyclerView setup
        rvAttendance.setLayoutManager(new LinearLayoutManager(this));
        attendanceAdapter = new AttendanceAdapter(attendanceList, this::submitReasonForRecord);
        rvAttendance.setAdapter(attendanceAdapter);

        // Spinner setup
        classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(classAdapter);

        // Load student info & classes
        loadStudentInfo();
        loadStudentClasses();

        // Spinner listener
        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!classList.isEmpty()) {
                    loadAttendance(classList.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ---------------- Load Student Info ----------------
    private void loadStudentInfo() {
        if (studentEmail == null) return;

        db.collection("students")
                .whereEqualTo("email", studentEmail)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        if (doc.contains("name") && doc.contains("roll")) {
                            tvRoll.setText(String.valueOf(doc.getLong("roll")));
                            tvName.setText(doc.getString("name"));
                            break;
                        }
                    }
                });
    }

    // ---------------- Load Classes ----------------
    private void loadStudentClasses() {
        if (studentEmail == null) return;

        db.collection("students")
                .whereEqualTo("email", studentEmail)
                .get()
                .addOnSuccessListener(query -> {
                    classList.clear();
                    spinnerClassToTeacher.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        String className = doc.getString("className");
                        String teacherId = doc.getString("teacherId");
                        if (className != null && teacherId != null) {
                            String uniqueClass = className + " (" + teacherId.substring(0, 5) + ")";
                            if (!classList.contains(uniqueClass)) {
                                classList.add(uniqueClass);
                                spinnerClassToTeacher.put(uniqueClass, teacherId);
                            }
                        }
                    }

                    if (!classList.isEmpty()) {
                        classAdapter.notifyDataSetChanged();
                        spinnerClass.setSelection(0);
                        loadAttendance(classList.get(0));
                    }
                });
    }

    // ---------------- Load Attendance ----------------
    private void loadAttendance(String spinnerItem) {
        if (studentEmail == null || spinnerItem == null) return;

        String className = spinnerItem.split(" \\(")[0];
        String teacherId = spinnerClassToTeacher.get(spinnerItem);
        if (teacherId == null) return;

        db.collection("attendance")
                .whereEqualTo("className", className)
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(attSnap -> {
                    attendanceList.clear();
                    Set<String> datesAdded = new HashSet<>();

                    for (QueryDocumentSnapshot attDoc : attSnap) {
                        long dateMillis = attDoc.getLong("dateMillis") != null ? attDoc.getLong("dateMillis") : 0;
                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(new Date(dateMillis));

                        boolean present = false;
                        Map<String, Boolean> attendanceMap = (Map<String, Boolean>) attDoc.get("attendance");
                        Map<String, String> emailMap = (Map<String, String>) attDoc.get("attendanceEmails");

                        if (attendanceMap != null && emailMap != null) {
                            for (Map.Entry<String, String> entry : emailMap.entrySet()) {
                                if (studentEmail.equals(entry.getValue())) {
                                    present |= Boolean.TRUE.equals(attendanceMap.get(entry.getKey()));
                                }
                            }
                        }

                        if (!datesAdded.contains(date)) {
                            attendanceList.add(new AttendanceRecord(date, present));
                            datesAdded.add(date);
                        }
                    }

                    attendanceAdapter.notifyDataSetChanged();
                });
    }

    // ---------------- Submit Reason ----------------
    private void submitReasonForRecord(AttendanceRecord record, int position) {
        if (record.isPresent() || record.hasReason()) return;

        String selectedClass = spinnerClass.getSelectedItem().toString();
        String className = selectedClass.split(" \\(")[0];

        ReasonDialog dialog = new ReasonDialog(this, reasonText -> {
            record.setReason(reasonText);
            record.setHasReason(true);
            attendanceAdapter.notifyItemChanged(position);

            Map<String, Object> data = new HashMap<>();
            data.put("studentEmail", studentEmail);
            data.put("className", className);
            data.put("reason", reasonText);
            data.put("timestamp", System.currentTimeMillis());

            db.collection("absence_reasons")
                    .add(data)
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
        dialog.show();
    }

    // ---------------- Load profile image by email ----------------
    private void loadStudentProfileImage() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String studentUid = user.getUid(); // ✅ use UID

        db.collection("students").document(studentUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String base64 = doc.getString("profileImageBase64");
                        if (base64 != null && !base64.isEmpty()) {
                            try {
                                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                ivProfile.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }

    // ---------------- Open Gallery to pick image ----------------
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // ---------------- Handle selected image ----------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                ivProfile.setImageURI(imageUri);
                uploadImageToFirestore(imageUri);
            }
        }
    }

    private void uploadImageToFirestore(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            // Update Firestore by email
            db.collection("students")
                    .whereEqualTo("email", studentEmail)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            String docId = query.getDocuments().get(0).getId();
                            Map<String, Object> update = new HashMap<>();
                            update.put("profileImageBase64", base64Image);
                            db.collection("students").document(docId)
                                    .update(update)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}