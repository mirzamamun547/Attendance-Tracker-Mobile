package com.example.myattendancetracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import android.util.Base64;

public class TeacherActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ImageView imgProfile;
    private TextView tvWelcome, tvEmailPreview;
    private TextView btnAddClass, btnViewStudents;
    private RecyclerView recyclerClasses;
    private ClassAdapter classAdapter;
    private List<String> classList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Drawer setup
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_attendance) startActivity(new Intent(this, AttendanceActivity.class));
            else if (id == R.id.nav_add_class) startActivity(new Intent(this, AddClassActivity.class));
            else if (id == R.id.nav_add_student) startActivity(new Intent(this, AddStudentActivity.class));
            else if (id == R.id.nav_view_students) startActivity(new Intent(this, StudentListActivity.class));
            else if (id == R.id.nav_reason_absence) startActivity(new Intent(this, ViewAbsenceReasonsActivity.class));
            else if (id == R.id.nav_logout) {
                auth.signOut();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // UI elements
        imgProfile = findViewById(R.id.imgProfileTeacher);
        tvWelcome = findViewById(R.id.tvWelcomeTeacher);
        tvEmailPreview = findViewById(R.id.tvEmailPreview);
        btnAddClass = findViewById(R.id.btnAddClass);
        btnViewStudents = findViewById(R.id.btnViewStudents);
        recyclerClasses = findViewById(R.id.recyclerClasses);

        setupTeacherInfo();
        setupButtons();
        setupRecyclerView();
        loadClassesFromFirestore();
        loadProfileFromFirestore(); // Load image from ProfileActivity
    }

    private void setupTeacherInfo() {
        if (auth.getCurrentUser() == null || auth.getCurrentUser().getEmail() == null) return;

        String email = auth.getCurrentUser().getEmail();
        tvEmailPreview.setText(email);

        String displayName = email.split("@")[0].replace("_", " ").replace(".", " ");
        displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
        tvWelcome.setText("Welcome, " + displayName + "!");
    }

    private void setupButtons() {
        btnAddClass.setOnClickListener(v -> startActivity(new Intent(this, AddClassActivity.class)));
        btnViewStudents.setOnClickListener(v -> startActivity(new Intent(this, StudentListActivity.class)));
    }

    private void setupRecyclerView() {
        classList = new ArrayList<>();
        classAdapter = new ClassAdapter(classList, className ->
                Toast.makeText(this, "Clicked: " + className, Toast.LENGTH_SHORT).show()
        );
        recyclerClasses.setLayoutManager(new LinearLayoutManager(this));
        recyclerClasses.setAdapter(classAdapter);
    }

    private void loadClassesFromFirestore() {
        if (auth.getCurrentUser() == null) return;

        db.collection("classes")
                .whereEqualTo("teacherId", auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    classList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String className = doc.getString("className");
                        if (className != null) classList.add(className);
                    }
                    classAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load classes", Toast.LENGTH_SHORT).show());
    }

    private void loadProfileFromFirestore() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        // Load the same Base64 field from ProfileActivity
        db.collection("teachers").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String base64 = doc.getString("profileImageBase64"); // same field
                        if (base64 != null && !base64.isEmpty()) {
                            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            imgProfile.setImageBitmap(bitmap);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
