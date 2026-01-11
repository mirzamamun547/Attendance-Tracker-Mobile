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

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_attendance) {

                startActivity(new Intent(this, AttendanceActivity.class));

            } else if (id == R.id.nav_add_class) {

                startActivity(new Intent(this, AddClassActivity.class));

            } else if (id == R.id.nav_add_student) {

                startActivity(new Intent(this, AddStudentActivity.class));

            } else if (id == R.id.nav_view_students) {

                startActivity(new Intent(this, StudentListActivity.class));

            }
            // ✅ Reason of Absence (Teacher View)
            else if (id == R.id.nav_reason_absence) {

                startActivity(new Intent(this, ViewAbsenceReasonsActivity.class));

            }
            else if (id == R.id.nav_logout) {

                auth.signOut();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }
}
