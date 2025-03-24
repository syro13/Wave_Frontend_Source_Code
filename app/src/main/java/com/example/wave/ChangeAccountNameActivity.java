package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ChangeAccountNameActivity extends AppCompatActivity {

    private EditText accountNameInput;
    private Button submitButton;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_account_name);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Bind UI Elements
        accountNameInput = findViewById(R.id.accountNameInput);
        submitButton = findViewById(R.id.submitButton);
        ImageView backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);

        // Load current name into input field
        loadCurrentUserName();


        // Handle Back Button Click
        backButton.setOnClickListener(v -> {
            finish(); // Go back to ProfileActivity
        });

        // Handle Submit Button Click
        submitButton.setOnClickListener(v -> updateAccountName());
    }
    private void loadCurrentUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getDisplayName() != null) {
            accountNameInput.setText(user.getDisplayName());
        }
    }
    /**
     * Updates the user's display name in Firebase Authentication.
     */
    private void updateAccountName() {
        String newName = accountNameInput.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(newName)) {
            accountNameInput.setError("Name cannot be empty");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            progressBar.setVisibility(View.VISIBLE);
            submitButton.setEnabled(false);

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        // Hide progress and enable button
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(ChangeAccountNameActivity.this, "Account name updated!", Toast.LENGTH_SHORT).show();
                            redirectToProfile();
                        } else {
                            Toast.makeText(ChangeAccountNameActivity.this, "Failed to update name", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    private void redirectToProfile() {
        Intent intent = new Intent(ChangeAccountNameActivity.this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
