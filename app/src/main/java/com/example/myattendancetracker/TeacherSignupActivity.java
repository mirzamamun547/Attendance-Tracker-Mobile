package com.example.myattendancetracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TeacherSignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    private EditText etTeacherName, etTeacherEmail, etTeacherPassword, etTeacherConfirmPassword;
    private Button btnTeacherSignup, btnGoogleSignup;

    private static final int REQ_ONE_TAP = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_signup);

        etTeacherName = findViewById(R.id.etTeacherName);
        etTeacherEmail = findViewById(R.id.etTeacherEmail);
        etTeacherPassword = findViewById(R.id.etTeacherPassword);
        etTeacherConfirmPassword = findViewById(R.id.etTeacherConfirmPassword);
        btnTeacherSignup = findViewById(R.id.btnTeacherSignup);
        btnGoogleSignup = findViewById(R.id.btnGoogleSignup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // One Tap Sign-In setup
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(getString(R.string.default_web_client_id))
                                .setFilterByAuthorizedAccounts(false)
                                .build())
                .setAutoSelectEnabled(true)
                .build();

        btnTeacherSignup.setOnClickListener(v -> teacherSignup());
        btnGoogleSignup.setOnClickListener(v -> googleSignIn());
    }

    // ---------------- EMAIL SIGN-UP ----------------
    private void teacherSignup() {
        String name = etTeacherName.getText().toString().trim();
        String email = etTeacherEmail.getText().toString().trim();
        String password = etTeacherPassword.getText().toString().trim();
        String confirmPassword = etTeacherConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    saveTeacherData(name, email, user.getUid());
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // ---------------- GOOGLE SIGN-UP ----------------
    private void googleSignIn() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(result -> {
                    try {
                        startIntentSenderForResult(
                                result.getPendingIntent().getIntentSender(),
                                REQ_ONE_TAP,
                                null, 0, 0, 0, null);
                    } catch (Exception e) {
                        Log.e("OneTap", "Couldn't start One Tap UI", e);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OneTap", "Sign-in failed", e);
                    Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP && resultCode == Activity.RESULT_OK) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();

                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken, credential.getDisplayName(), credential.getId());
                }
            } catch (ApiException e) {
                Log.e("OneTap", "Sign-in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken, String name, String email) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                checkAndSaveUser(user, name, email);
            } else {
                Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------- FIRESTORE ROLE MANAGEMENT ----------------
    private void checkAndSaveUser(FirebaseUser user, String name, String email) {
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
            if (!doc.exists() || !doc.contains("role")) {
                saveTeacherData(name, email, user.getUid());
            } else {
                startActivity(new Intent(this, TeacherActivity.class));
                finish();
            }
        });
    }

    private void saveTeacherData(String name, String email, String uid) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("role", "teacher");

        db.collection("users").document(uid)
                .set(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, TeacherActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show());
    }
}
