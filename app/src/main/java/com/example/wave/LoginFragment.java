package com.example.wave;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth; // Firebase Authentication instance
    private EditText emailInput, passwordInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Bind inputs and buttons
        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        Button loginSubmitButton = view.findViewById(R.id.loginSubmitButton);
        TextView loginButton = view.findViewById(R.id.loginButton);
        TextView signupButton = view.findViewById(R.id.signupButton);

        // Set initial active state
        setActiveButton(loginButton, signupButton);

        // Handle Login Button Click
        loginButton.setOnClickListener(v -> {
            // No action for login since it's already active, but update styles
            setActiveButton(loginButton, signupButton);
        });

        // Handle Sign Up Button Click
        signupButton.setOnClickListener(v -> {
            if (getActivity() instanceof LoginSignUpActivity) {
                ((LoginSignUpActivity) getActivity()).showSignupFragment();
            }
            setActiveButton(signupButton, loginButton); // Update styles
        });

        // Handle Login Submit Button Click
        loginSubmitButton.setOnClickListener(v -> loginUser());

        return view;
    }

    /**
     * Logs in the user with Firebase Authentication.
     */
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
                        Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                        // Navigate to MainActivity or other dashboard activity
                        if (getActivity() != null) {
                            getActivity().finish(); // Close LoginSignUpActivity
                            startActivity(new android.content.Intent(getActivity(), MainActivity.class));
                        }
                    } else {
                        Toast.makeText(getContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
