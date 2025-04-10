package com.example.wave;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Arrays;

public class LoginFragment extends Fragment implements TwitterAuthManager.Callback {

    private FirebaseAuth mAuth; // Firebase Authentication instance
    private GoogleSignInClient googleSignInClient; // Google Sign-In client
    private EditText emailInput, passwordInput;

    private CallbackManager callbackManager;
    private TwitterAuthManager twitterAuthManager;
    private ImageView passwordToggle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
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
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Use your Web Client ID
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // Bind inputs and buttons
        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        passwordToggle = view.findViewById(R.id.passwordToggle);
        Button loginSubmitButton = view.findViewById(R.id.loginSubmitButton);
        TextView loginButton = view.findViewById(R.id.loginButton);
        TextView signupButton = view.findViewById(R.id.signupButton);
        ImageView googleSignInButton = view.findViewById(R.id.googleIcon); // Google sign-in button (ImageView)
        ImageView facebookIcon = view.findViewById(R.id.facebookIcon);
        ImageView twitterIcon = view.findViewById(R.id.twitterIcon);
        TextView forgotPasswordText = view.findViewById(R.id.forgotPassword);

        passwordToggle.setClickable(false);
        passwordToggle.setFocusable(false);
        passwordToggle.setEnabled(false);

        ImageView passwordToggle = view.findViewById(R.id.passwordToggle);
        passwordToggle.setOnClickListener(v -> {
            int inputType = passwordInput.getInputType();
            boolean isPasswordVisible = (inputType & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;

            if (isPasswordVisible) {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye_closed);
            } else {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye_opened);
            }
            passwordInput.setSelection(passwordInput.getText().length());
        });

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    passwordToggle.setClickable(true);
                    passwordToggle.setFocusable(true);
                    passwordToggle.setEnabled(true);
                    passwordToggle.setAlpha(1.0f); // Full opacity when enabled
                } else {
                    passwordToggle.setClickable(false);
                    passwordToggle.setFocusable(false);
                    passwordToggle.setEnabled(false);
                    passwordToggle.setAlpha(0.5f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
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

        // Handle Facebook Sign-Up Button Click
        facebookIcon.setOnClickListener(v -> loginWithFacebook());

        // Handle Twitter Sign-Up Button Click
        twitterIcon.setOnClickListener(v -> loginWithTwitter());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            refreshAuthToken(user);
        }

        // Navigate to Forgot Password Screen when clicked
        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ForgotPasswordActivity.class);
            startActivity(intent);
        });

        return view;
    }
    /**
     * Refresh Firebase Auth Token.
     */
    private void refreshAuthToken(FirebaseUser user) {
        user.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String newToken = task.getResult().getToken();
                Log.d("SESSION", "New Token: " + newToken);
            } else {
                Log.e("SESSION", "Failed to refresh token", task.getException());
            }
        });
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
                            UserUtils.saveUserData(requireContext(), user, user.getDisplayName());
                        }
                    } else {
                       Log.e("LoginFragment", "Firebase Authentication failed", task.getException());
                        Toast.makeText(requireContext(), "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void loginWithTwitter() {
        twitterAuthManager.signIn();
    }

    @Override
    public void updateUI(FirebaseUser user) {
        if (user != null) {
            UserUtils.saveUserData(requireContext(), user, user.getDisplayName());
        } else {
            Toast.makeText(getContext(), "Twitter Login Failed", Toast.LENGTH_SHORT).show();
        }
    }
    private void loginWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(LoginFragment.this, Arrays.asList("email", "public_profile"));

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(getContext(), "Facebook login Canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getContext(), "Facebook login Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

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
                        Toast.makeText(getContext(), "Facebook Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    /**
     * Navigate to Dashboard after successful login.
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
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
                            UserUtils.saveUserData(requireContext(), user, user.getDisplayName());
                        }
                    }
                    else {
                        Log.e("LoginFragment", "Login failed", task.getException());
                        Toast.makeText(requireContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
