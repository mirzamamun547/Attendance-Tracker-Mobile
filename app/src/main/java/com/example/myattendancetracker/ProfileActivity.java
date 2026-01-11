package com.example.myattendancetracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;

    private ImageView imgProfile;
    private Button btnUpload, btnContinue;
    private TextView tvInfo;

    private String role, email;
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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

        // Get role/email from LoginActivity
        role = getIntent().getStringExtra("role");
        email = getIntent().getStringExtra("email");

        tvInfo.setText("Email: " + email + "\nRole: " + role);

        // Load existing profile image from Firestore (Base64)
        loadProfileImage();

        btnUpload.setOnClickListener(v -> openImagePicker());

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

    // ---------------- Open Gallery ----------------
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
                imgProfile.setImageURI(imageUri);
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

            // Save to Firestore
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;

            String collection = role.equalsIgnoreCase("Teacher") ? "teachers" : "students";
            Map<String, Object> update = new HashMap<>();
            update.put("profileImageBase64", base64Image);

            db.collection(collection).document(user.getUid())
                    .update(update)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------- Load image from Base64 ----------------
    private void loadProfileImage() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String collection = role.equalsIgnoreCase("Teacher") ? "teachers" : "students";

        db.collection(collection).document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String base64 = doc.getString("profileImageBase64");
                        if (base64 != null && !base64.isEmpty()) {
                            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            imgProfile.setImageBitmap(bitmap);
                        }
                    }
                });
    }
}
