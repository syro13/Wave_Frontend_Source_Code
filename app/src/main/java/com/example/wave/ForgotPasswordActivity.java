package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ForgotPasswordActivity extends AppCompatActivity {

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

        // Handle Back Button Click
        backButton.setOnClickListener(v -> finish()); // Go back to Login screen
    }

    private void resetPassword() {
        String email = emailInput.getText().toString().trim();

        // Validate email input
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        // Check if the email is registered in Firebase
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();

                        if (signInMethods != null && !signInMethods.isEmpty()) {
                            // Email exists → Send reset link
                            sendResetEmail(email);
                        } else {
                            // Email not registered → Show error
                            Toast.makeText(ForgotPasswordActivity.this, "No account found with this email.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Error occurred while checking email
                        Toast.makeText(ForgotPasswordActivity.this, "Error checking email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Separate method to send password reset email
    private void sendResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent. Check your inbox!", Toast.LENGTH_LONG).show();

                        // Redirect user back to Login screen
                        startActivity(new Intent(ForgotPasswordActivity.this, LoginSignUpActivity.class));
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Error sending reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}