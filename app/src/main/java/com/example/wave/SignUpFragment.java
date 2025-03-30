package com.example.wave;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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

import com.airbnb.lottie.LottieAnimationView;
import com.example.wave.utils.UserUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;


import java.util.Arrays;

public class SignUpFragment extends Fragment implements TwitterAuthManager.Callback {

    private FirebaseAuth mAuth; // Firebase Authentication instance
    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 123; // Request code for Google Sign-In
    private CallbackManager callbackManager;
    private TwitterAuthManager twitterAuthManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
// Inject dynamic Lottie animation for theme
        LottieAnimationView lottieView = view.findViewById(R.id.lottieAnimation);
        int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            lottieView.setAnimation(R.raw.login_signup_animation_dark); // dark theme animation
        } else {
            lottieView.setAnimation(R.raw.login_signup_animation); // default light theme animation
        }
        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize TwitterAuthManager with the Activity
        twitterAuthManager = TwitterAuthManager.getInstance(requireActivity(), this);

        // Initialize Facebook Callback Manager
        callbackManager = CallbackManager.Factory.create();

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
        ImageView facebookIcon = view.findViewById(R.id.facebookIcon);
        ImageView twitterIcon = view.findViewById(R.id.twitterIcon);

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

        // Handle Facebook Sign-Up Button Click
        facebookIcon.setOnClickListener(v -> signUpWithFacebook());

        // Handle Twitter Sign-Up Button Click
        twitterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                twitterAuthManager.signIn();
            }
        });

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
        googleSignInClient.signOut();
    }

    /**
     * Trigger Google Sign-In.
     */
    private void signUpWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Handle Facebook Sign-In.
     */
    private void signUpWithFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(SignUpFragment.this, Arrays.asList("email","public_profile"));

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(getContext(), "Facebook Sign-In Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getContext(), "Facebook Sign-In Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handle Facebook Access Token and authenticate with Firebase.
     */
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserUtils.saveUserData(requireContext(), user, user.getDisplayName());
                        }
                    } else {
                        Toast.makeText(getContext(), "Facebook Sign-In Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void updateUI(FirebaseUser user) {
        if (user != null) {
            UserUtils.saveUserData(requireContext(), user, user.getDisplayName());
        } else {
            Toast.makeText(getContext(), "Twitter Sign-In Failed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        // Handle Facebook Login Result
        callbackManager.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);

        // Handle Google Sign-In Result
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult();
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Google Sign-In Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        String email = account.getEmail(); // Get the email from the account

        // Check if the email is already registered
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isExistingUser = !task.getResult().getSignInMethods().isEmpty();

                        if (isExistingUser) {
                            // User exists, treat it as a login
                            Toast.makeText(getContext(), "Account already exists. Logging in...", Toast.LENGTH_SHORT).show();
                            authenticateWithGoogle(account, false); // Sign-in flow
                        } else {
                            // User doesn't exist, treat it as a sign-up
                            authenticateWithGoogle(account, true); // Sign-up flow
                        }
                    } else {
                        Toast.makeText(getContext(), "Error checking user status: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Authenticate the Google account with Firebase and differentiate between sign-up and login.
     *
     * @param account GoogleSignInAccount
     * @param isSignUp Indicates whether this is a sign-up or sign-in
     */
    private void authenticateWithGoogle(GoogleSignInAccount account, boolean isSignUp) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (isSignUp) {
                                UserUtils.saveUserData(requireContext(), user, user.getDisplayName());
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Google Sign-In Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    private boolean isValidPassword(String password) {
        // Minimum 12 characters, at least 1 uppercase, 1 lowercase, 1 digit, 1 special char
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,100}$";
        return password.matches(passwordRegex);
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
        if (!isValidPassword(password)) {
            passwordInput.setError("Password must be at least 12 characters, include uppercase, lowercase, number and special character");
            return;
        }
         mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserUtils.saveUserData(requireContext(), user, name);
                        }
                    } else {
                        Toast.makeText(getContext(), "Sign-Up Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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