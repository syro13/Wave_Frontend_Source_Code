package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ForgotPasswordActivity extends BaseActivity {

    private EditText emailInput;
    private Button submitButton;
    private ImageView backButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind UI elements
        emailInput = findViewById(R.id.emailInput);
        submitButton = findViewById(R.id.submitButton);
        backButton = findViewById(R.id.backButton);

        // Handle Reset Password Button Click
        submitButton.setOnClickListener(v -> resetPassword());

        setNoInternetOverlay(findViewById(R.id.noInternetOverlay));
        configureNoInternetOverlay();

        // Handle Back Button Click
        backButton.setOnClickListener(v -> finish()); // Go back to Login screen
    }
    @Override
    protected int getCurrentMenuItemId() {
        return -1; // No selection
    }
    private void resetPassword() {
        String email = emailInput.getText().toString().trim();

        // Validate email input
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        // Attempt sign-in with a fake password to check if email exists
        mAuth.signInWithEmailAndPassword(email, "fakePassword")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // User signed in (this shouldn't happen with a fake password)
                        Toast.makeText(ForgotPasswordActivity.this, "Unexpected sign-in. Try resetting your password.", Toast.LENGTH_LONG).show();
                    } else {
                        // Check if the error is due to a non-existent email
                        String errorMessage = task.getException().getMessage();
                        if (errorMessage.contains("no user record")) {
                            Toast.makeText(ForgotPasswordActivity.this, "No account found with this email.", Toast.LENGTH_LONG).show();
                        } else {
                            // Email exists → Send reset link
                            sendResetEmail(email);
                        }
                    }
                });
    }

    private void sendResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent. Check your inbox!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ForgotPasswordActivity.this, LoginSignUpActivity.class));
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Error sending reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

}
