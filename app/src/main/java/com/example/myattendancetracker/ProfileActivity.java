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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imgProfile;
    private Button btnUpload, btnContinue;
    private TextView tvInfo;

    private String role;
    private String email;
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgProfile = findViewById(R.id.imgProfile);
        btnUpload = findViewById(R.id.btnUpload);
        btnContinue = findViewById(R.id.btnContinue);
        tvInfo = findViewById(R.id.tvInfo);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Get role/email from LoginActivity
        role = getIntent().getStringExtra("role");
        email = getIntent().getStringExtra("email");

        tvInfo.setText("Email: " + email + "\nRole: " + role);

        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnContinue.setOnClickListener(v -> {
            Intent intent;
            if ("Teacher".equalsIgnoreCase(role)) {
                intent = new Intent(ProfileActivity.this, TeacherActivity.class);
            } else {
                intent = new Intent(ProfileActivity.this, StudentActivity.class);
            }
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                imgProfile.setImageURI(imageUri);
                uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri uri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference fileRef = storageRef.child("profile_images/" + user.getUid() + ".jpg");
        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    saveImageUrlToFirestore(user.getUid(), downloadUri.toString());
                    Toast.makeText(this, "Profile picture uploaded!", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveImageUrlToFirestore(String uid, String imageUrl) {
        Map<String, Object> update = new HashMap<>();
        update.put("profileImage", imageUrl);

        // Correct collection based on role
        String collection = "Teacher".equalsIgnoreCase(role) ? "teachers" : "students";

        db.collection(collection).document(uid)
                .update(update)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile updated in Firestore!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
