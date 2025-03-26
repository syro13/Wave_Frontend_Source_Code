package com.example.wave;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;


import java.util.List;

public class ChangePasswordActivity extends BaseActivity {

    private EditText currentPasswordInput, newPasswordInput;
    private Button updatePasswordButton, sendResetEmailButton;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ProgressBar progressBar;
    private ImageView toggleCurrentPassword, toggleNewPassword;
    private boolean isCurrentPasswordVisible = false, isNewPasswordVisible = false;
    private boolean isOAuthUserWithoutPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        setNoInternetOverlay(findViewById(R.id.noInternetOverlay));
        configureNoInternetOverlay();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // Bind UI elements
        currentPasswordInput = findViewById(R.id.currentPasswordInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        updatePasswordButton = findViewById(R.id.submitButton);
        sendResetEmailButton = findViewById(R.id.sendResetEmailButton);
        progressBar = findViewById(R.id.progressBar);
        ImageView backButton = findViewById(R.id.backButton);
        toggleCurrentPassword = findViewById(R.id.toggleCurrentPassword);
        toggleNewPassword = findViewById(R.id.toggleNewPassword);


        // Back Button Click
        backButton.setOnClickListener(v -> finish());

        // Check if user needs password reset email or can change password normally
        checkUserAuthMethod();

        // Handle Reset Email for OAuth users
        sendResetEmailButton.setOnClickListener(v -> sendPasswordResetEmail());

        // Handle Password Update
        updatePasswordButton.setOnClickListener(v -> updatePassword());

        // Initially disable the eye icons
        toggleCurrentPassword.setEnabled(false);
        toggleCurrentPassword.setAlpha(0.5f); // Reduce opacity to show it's disabled
        toggleNewPassword.setEnabled(false);
        toggleNewPassword.setAlpha(0.5f);

        // Enable the eye icon when user types
        currentPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    toggleCurrentPassword.setEnabled(true);
                    toggleCurrentPassword.setAlpha(1.0f); // Full opacity when enabled
                } else {
                    toggleCurrentPassword.setEnabled(false);
                    toggleCurrentPassword.setAlpha(0.5f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        newPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    toggleNewPassword.setEnabled(true);
                    toggleNewPassword.setAlpha(1.0f);
                } else {
                    toggleNewPassword.setEnabled(false);
                    toggleNewPassword.setAlpha(0.5f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Toggle Current Password Visibility
        toggleCurrentPassword.setOnClickListener(v -> {
            if (toggleCurrentPassword.isEnabled()) { // Check if enabled before toggling
                isCurrentPasswordVisible = !isCurrentPasswordVisible;
                if (isCurrentPasswordVisible) {
                    currentPasswordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    toggleCurrentPassword.setImageResource(R.drawable.ic_eye_opened);
                } else {
                    currentPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    toggleCurrentPassword.setImageResource(R.drawable.ic_eye_closed);
                }
                currentPasswordInput.setSelection(currentPasswordInput.length()); // Keep cursor at end
            }
        });

        // Toggle New Password Visibility
        toggleNewPassword.setOnClickListener(v -> {
            if (toggleNewPassword.isEnabled()) { // Check if enabled before toggling
                isNewPasswordVisible = !isNewPasswordVisible;
                if (isNewPasswordVisible) {
                    newPasswordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    toggleNewPassword.setImageResource(R.drawable.ic_eye_opened);
                } else {
                    newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    toggleNewPassword.setImageResource(R.drawable.ic_eye_closed);
                }
                newPasswordInput.setSelection(newPasswordInput.length());
            }
        });
    }
    @Override
    protected int getCurrentMenuItemId() {
        return -1; // No selection
    }
    /**
     * Check if user has a password or is an OAuth user without one.
     */
    private void checkUserAuthMethod() {
        if (user != null) {
            boolean isOAuthUser = false;

            for (UserInfo userInfo : user.getProviderData()) {
                String providerId = userInfo.getProviderId();

                if (providerId.equals("google.com") || providerId.equals("facebook.com") || providerId.equals("twitter.com")) {
                    isOAuthUser = true;
                    break; // No need to check further
                }
            }

            if (isOAuthUser) {
                // User signed in with Google/Facebook/Twitter → Show reset email option
                isOAuthUserWithoutPassword = true;
                findViewById(R.id.passwordFields).setVisibility(View.GONE);
                sendResetEmailButton.setVisibility(View.VISIBLE);
            } else {
                // User has email/password authentication → Show password change fields
                findViewById(R.id.passwordFields).setVisibility(View.VISIBLE);
                sendResetEmailButton.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Sends a password reset email for OAuth users without a password.
     */
    private void sendPasswordResetEmail() {
        if (user != null && user.getEmail() != null) {
            progressBar.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(user.getEmail())
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Show an alert dialog informing the user
                            new AlertDialog.Builder(ChangePasswordActivity.this)
                                    .setTitle("Password Reset Sent")
                                    .setMessage("A password reset email has been sent to your email. After resetting your password, please log in again.")
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        // Log out user and navigate to login screen
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(ChangePasswordActivity.this, LoginSignUpActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .setCancelable(false)
                                    .show();
                        } else {
                            showToast("Failed to send reset email: " + task.getException().getMessage());
                        }
                    });
        }
    }
    private void showToast(String message) {
        Toast.makeText(ChangePasswordActivity.this, message, Toast.LENGTH_SHORT).show();
    }
    private void updatePassword() {
        if (isOAuthUserWithoutPassword) {
            showToast("Please reset your password via email.");
            return;
        }

        String currentPassword = currentPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();

        // Validate input
        if (!isPasswordValid(newPassword)) {
            return;
        }
        if (newPassword.equals(currentPassword)) {
            newPasswordInput.setError("New password cannot be the same as the current password");
            return;
        }

        if (user != null && user.getEmail() != null) {
                progressBar.setVisibility(View.VISIBLE);
                updatePasswordButton.setEnabled(false);

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
                user.reauthenticate(credential).addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                            progressBar.setVisibility(View.GONE);
                            updatePasswordButton.setEnabled(true);

                            if (updateTask.isSuccessful()) {
                                Toast.makeText(ChangePasswordActivity.this, "Password updated successfully! Please log in again.", Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                                Intent intent = new Intent(ChangePasswordActivity.this, LoginSignUpActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Exception exception = updateTask.getException();
                                if (exception != null) {
                                    String errorMessage = exception.getMessage();
                                    Toast.makeText(this, "Failed to update password: " + errorMessage, Toast.LENGTH_LONG).show();
                                    exception.printStackTrace();  // Logs full error in Logcat
                                } else {
                                    Toast.makeText(this, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        updatePasswordButton.setEnabled(true);
                        Toast.makeText(ChangePasswordActivity.this, "Current password incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
            }
         else {
            Toast.makeText(this, "User not logged in. Please log in again.", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean isPasswordValid(String password) {
        if (TextUtils.isEmpty(password)) {
            newPasswordInput.setError("Password cannot be empty");
            return false;
        }
        if (password.length() < 12) {
            newPasswordInput.setError("Password must be at least 12 characters");
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            newPasswordInput.setError("Password must contain at least one uppercase letter");
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            newPasswordInput.setError("Password must contain at least one lowercase letter");
            return false;
        }
        if (!password.matches(".*\\d.*")) {
            newPasswordInput.setError("Password must contain at least one numeric character");
            return false;
        }
        if (!password.matches(".*[@#$%^&+=!].*")) {
            newPasswordInput.setError("Password must contain at least one special character (@#$%^&+=!)");
            return false;
        }
        return true;
    }

}
