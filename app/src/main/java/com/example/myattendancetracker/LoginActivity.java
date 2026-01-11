package com.example.myattendancetracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

    private EditText etEmail, etPassword;
    private RadioGroup radioGroupRole;
    private RadioButton rbTeacher, rbStudent;
    private Button btnLogin, btnGoogleLogin, btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        // UI references
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        rbTeacher = findViewById(R.id.rbTeacher);
        rbStudent = findViewById(R.id.rbStudent);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        // Button listeners
        btnLogin.setOnClickListener(v -> loginWithEmail());
        btnSignUp.setOnClickListener(v -> signUpWithEmail());
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
    }


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
                        if (user != null && user.getEmail() != null) {
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            fetchUserRole(user.getUid());
                        }
                    } else {
                        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void signUpWithEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = radioGroupRole.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = (selectedId == R.id.rbTeacher) ? "Teacher" : "Student";

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) saveRoleToFirestore(user.getUid(), email, role);
                    } else {
                        Toast.makeText(this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


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

        if (requestCode == REQ_ONE_TAP && resultCode == Activity.RESULT_OK && data != null) {
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
                        if (user != null && user.getEmail() != null) {
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            fetchUserRole(user.getUid());
                        }
                    } else {
                        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


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
                        // First-time user → ask role
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.getEmail() != null) {
                            showRoleSelectionDialog(uid, user.getEmail());
                        }
                    }
                });
            }
        }).addOnFailureListener(e -> Log.e("Login", "Error fetching role", e));
    }


    private void showRoleSelectionDialog(String uid, String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Role")
                .setMessage("Please choose your role")
                .setPositiveButton("Teacher", (dialog, which) -> saveRoleToFirestore(uid, email, "Teacher"))
                .setNegativeButton("Student", (dialog, which) -> saveRoleToFirestore(uid, email, "Student"))
                .setCancelable(false)
                .show();
    }


    private void saveRoleToFirestore(String uid, String email, String role) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("role", role);

        String collection = role.equals("Teacher") ? "teachers" : "students";

        db.collection(collection).document(uid).set(userData)
                .addOnSuccessListener(aVoid -> goToProfile(role, email))
                .addOnFailureListener(e -> Toast.makeText(this, "Error saving role: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void goToProfile(String role, String email) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("role", role);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }
}
