package com.example.myattendancetracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private static final int REQ_ONE_TAP = 2;

    private RadioGroup roleGroup;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleLogin, btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ===== INIT FIREBASE =====
        mAuth = FirebaseAuth.getInstance();
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

        // ===== INIT UI =====
        roleGroup = findViewById(R.id.radioGroupRole);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        // ===== BUTTON LISTENERS =====
        btnLogin.setOnClickListener(v -> loginWithEmail());
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
        btnSignUp.setOnClickListener(v -> handleSignUp());

        // Optional: disable SignUp button for students
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbStudent) {
                btnSignUp.setEnabled(false);
            } else {
                btnSignUp.setEnabled(true);
            }
        });
    }

    // ---------------- EMAIL LOGIN ----------------
    private void loginWithEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) fetchUserRole(user.getUid());
                    } else {
                        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ---------------- EMAIL SIGN-UP ----------------
    private void handleSignUp() {
        int selectedId = roleGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedId == R.id.rbStudent) {
            // Student cannot sign up
            Toast.makeText(this,
                    "Students cannot sign up. Ask teacher to add you.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Teacher selected → go to Teacher Signup Activity
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        Intent intent = new Intent(LoginActivity.this, TeacherSignupActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    // ---------------- GOOGLE LOGIN ----------------
    private void signInWithGoogle() {
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

        if (requestCode == REQ_ONE_TAP && resultCode == RESULT_OK && data != null) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) firebaseAuthWithGoogle(idToken);
            } catch (ApiException e) {
                Log.e("OneTap", "Sign-in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) fetchUserRole(user.getUid());
                    } else {
                        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ---------------- FIRESTORE ROLE MANAGEMENT ----------------
    private void fetchUserRole(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("teachers").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                goToProfile(doc.getString("role"), doc.getString("email"));
            } else {
                db.collection("students").document(uid).get().addOnSuccessListener(studentDoc -> {
                    if (studentDoc.exists()) {
                        goToProfile(studentDoc.getString("role"), studentDoc.getString("email"));
                    } else {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.getEmail() != null) {
                            Toast.makeText(this, "Account not found. Please sign up as Teacher.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).addOnFailureListener(e -> Log.e("Login", "Error fetching role", e));
    }

    private void goToProfile(String role, String email) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("role", role);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }
}
