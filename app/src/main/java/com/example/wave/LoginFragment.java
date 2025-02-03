package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth; // Firebase Authentication instance
    private GoogleSignInClient googleSignInClient; // Google Sign-In client
    private EditText emailInput, passwordInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Use your Web Client ID
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // Bind inputs and buttons
        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        Button loginSubmitButton = view.findViewById(R.id.loginSubmitButton);
        TextView loginButton = view.findViewById(R.id.loginButton);
        TextView signupButton = view.findViewById(R.id.signupButton);
        ImageView googleSignInButton = view.findViewById(R.id.googleIcon); // Google sign-in button (ImageView)

        // Set initial active state
        setActiveButton(loginButton, signupButton);

        // Handle Login Button Click
        loginButton.setOnClickListener(v -> setActiveButton(loginButton, signupButton));

        // Handle Sign Up Button Click
        signupButton.setOnClickListener(v -> {
            if (getActivity() instanceof LoginSignUpActivity) {
                ((LoginSignUpActivity) getActivity()).showSignupFragment();
            }
            setActiveButton(signupButton, loginButton); // Update styles
        });

        // Handle Login Submit Button Click
        loginSubmitButton.setOnClickListener(v -> loginUser());

        // Handle Google Sign-In Button Click
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        return view;
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    GoogleSignInAccount account = task.getResult();
                                    if (account != null) {
                                        authenticateWithFirebase(account);
                                    } else {
                                        Log.e("LoginFragment", "GoogleSignInAccount is null.");
                                        Toast.makeText(requireContext(), "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.e("LoginFragment", "Google Sign-In task failed", task.getException());
                                    Toast.makeText(requireContext(), "Google Sign-In failed", Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Log.d("LoginFragment", "Google Sign-In canceled or failed");
                    Toast.makeText(requireContext(), "Google Sign-In cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void authenticateWithFirebase(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String displayName = account.getDisplayName();

                            // Update display name in Firebase Authentication
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            navigateToDashboard(displayName);
                                        } else {
                                            Log.e("LoginFragment", "Profile update failed", updateTask.getException());
                                            Toast.makeText(requireContext(), "Failed to update display name", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Log.e("LoginFragment", "Firebase Authentication failed", task.getException());
                        Toast.makeText(requireContext(), "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToDashboard(String displayName) {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), DashboardActivity.class);
            intent.putExtra("USER_NAME", displayName);
            startActivity(intent);
            getActivity().finish();
        } else {
            Toast.makeText(requireContext(), "Context is unavailable. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(getResources().getColor(android.R.color.white));

        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(getResources().getColor(R.color.dark_blue));
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String displayName = user.getDisplayName();
                            if (displayName == null || displayName.isEmpty()) {
                                displayName = "User"; // Fallback if no display name is set
                            }
                            navigateToDashboard(displayName);
                        }
                    } else {
                        Log.e("LoginFragment", "Login failed", task.getException());
                        Toast.makeText(requireContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
