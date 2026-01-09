package com.example.myattendancetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class TeacherActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        auth = FirebaseAuth.getInstance();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer toggle (hamburger icon)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation menu click handling
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_attendance) {
                // Open Attendance Activity
                Intent intent = new Intent(TeacherActivity.this, AttendanceActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_add_class) {
                // Open Add Class Activity
                Intent intent = new Intent(TeacherActivity.this, AddClassActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_add_student) {
                // Open Add Student Activity
                Intent intent = new Intent(TeacherActivity.this, AddStudentActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_view_students) {
                // Open View Students Activity
                Intent intent = new Intent(TeacherActivity.this, StudentListActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                auth.signOut();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }
}
