package com.example.wave;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;

public class LoginSignUpActivity extends AppCompatActivity {

    private boolean isLoginFragmentActive = true; // To track the active fragment
    private FirebaseAuth mAuth; // Firebase Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sign_up); // Assuming a layout exists for this Activity

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Load the LoginFragment by default
        if (savedInstanceState == null) {
            loadFragment(new LoginFragment());
        }
    }

    /**
     * Load a fragment into the container and track the current state.
     *
     * @param fragment Fragment to load
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * Switch to the LoginFragment.
     */
    public void showLoginFragment() {
        if (!isLoginFragmentActive) { // Only switch if not already active
            loadFragment(new LoginFragment());
            isLoginFragmentActive = true;
        }
    }

    /**
     * Switch to the SignupFragment.
     */
    public void showSignupFragment() {
        if (isLoginFragmentActive) { // Only switch if not already active
            loadFragment(new SignUpFragment());
            isLoginFragmentActive = false;
        }
    }
}
