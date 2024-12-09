package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpFragment extends Fragment {

    private FirebaseAuth mAuth; // Firebase Authentication instance
    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 123; // Request code for Google Sign-In

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        configureGoogleSignIn();

        // Bind inputs and buttons
        nameInput = view.findViewById(R.id.nameInput);
        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);
        Button signupSubmitButton = view.findViewById(R.id.signupSubmitButton);
        TextView loginButton = view.findViewById(R.id.loginButton);
        TextView signupButton = view.findViewById(R.id.signupButton);
        ImageView googleIcon = view.findViewById(R.id.googleIcon);

        // Set initial active state
        setActiveButton(signupButton, loginButton);

        // Handle Login Button Click
        loginButton.setOnClickListener(v -> {
            if (getActivity() instanceof LoginSignUpActivity) {
                ((LoginSignUpActivity) getActivity()).showLoginFragment();
            }
            setActiveButton(loginButton, signupButton); // Update styles
        });

        // Handle Sign Up Button Click
        signupButton.setOnClickListener(v -> {
            // No action for signup since it's already active
            setActiveButton(signupButton, loginButton);
        });

        // Handle Sign Up Submit Button Click
        signupSubmitButton.setOnClickListener(v -> signUpUser());

        // Handle Google Sign-Up Button Click
        googleIcon.setOnClickListener(v -> signUpWithGoogle());

        return view;
    }

    /**
     * Configure Google Sign-In options.
     */
    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Web client ID from Firebase
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
    }

    /**
     * Trigger Google Sign-In.
     */
    private void signUpWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult();
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Google Sign-Up failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Authenticate the Google account with Firebase.
     *
     * @param account GoogleSignInAccount
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(getContext(), "Google Sign-Up successful!", Toast.LENGTH_SHORT).show();

                            // Get the user's display name
                            String userName = account.getDisplayName();

                            // Navigate to DashboardActivity and pass the user name
                            if (getActivity() != null) {
                                Intent intent = new Intent(getActivity(), DashboardActivity.class);
                                intent.putExtra("USER_NAME", userName); // Pass the user's name
                                getActivity().finish();
                                startActivity(intent);
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    /**
     * Handles user sign-up with email and password.
     */
    private void signUpUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInput.setError("Please confirm your password");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return;
        }

        // Create user in Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Set the display name in Firebase Authentication
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(getContext(), "Sign-up successful!", Toast.LENGTH_SHORT).show();
                                            // Navigate to Dashboard
                                            Intent intent = new Intent(getActivity(), DashboardActivity.class);
                                            intent.putExtra("USER_NAME", name);
                                            startActivity(intent);
                                            getActivity().finish();
                                        } else {
                                            Toast.makeText(getContext(), "Failed to set display name", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(getContext(), "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }



    /**
     * Updates the styles of the toggle buttons to show which one is active.
     *
     * @param activeButton   The button to mark as active
     * @param inactiveButton The button to mark as inactive
     */
    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        // Set active button style
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(getResources().getColor(android.R.color.white));

        // Set inactive button style
        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(getResources().getColor(R.color.dark_blue));
    }
}