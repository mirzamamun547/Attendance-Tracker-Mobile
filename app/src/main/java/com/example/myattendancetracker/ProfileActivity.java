package com.example.myattendancetracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imgProfile;
    private Button btnUpload, btnContinue;
    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // <-- your XML file

        // Initialize views
        imgProfile = findViewById(R.id.imgProfile);
        btnUpload = findViewById(R.id.btnUpload);
        tvInfo = findViewById(R.id.tvInfo);
        btnContinue = findViewById(R.id.btnContinue);

        // Get data passed from LoginActivity
        String role = getIntent().getStringExtra("role");
        String email = getIntent().getStringExtra("email");

        // Show info
        tvInfo.setText("Email: " + email + "\nRole: " + role);

        // Handle Upload button
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Handle Continue button
        btnContinue.setOnClickListener(v -> {
            Intent intent;
            if ("Teacher".equals(role)) {
                intent = new Intent(ProfileActivity.this, TeacherActivity.class);
            } else {
                intent = new Intent(ProfileActivity.this, StudentActivity.class);
            }
            startActivity(intent);
            finish(); // optional: close ProfileActivity
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                imgProfile.setImageURI(imageUri);
                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
