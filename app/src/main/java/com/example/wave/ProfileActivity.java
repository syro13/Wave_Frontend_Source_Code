package com.example.wave;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wave.utils.UserUtils;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.auth.UserInfo;

public class ProfileActivity extends BaseActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImage;
    private Uri imageUri;
    private FirebaseUser user;
    private StorageReference storageReference;
    private TextView profileName;

    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setNoInternetOverlay(findViewById(R.id.noInternetOverlay));
        configureNoInternetOverlay();

        // Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // UI Elements
        profileImage = findViewById(R.id.profileIcon);
        profileName = findViewById(R.id.userNameTextView);
        progressBar = findViewById(R.id.progressBar);
        LinearLayout changeAccountImage = findViewById(R.id.changeAccountImage);
        LinearLayout changeAccountName = findViewById(R.id.changeAccountName);
        LinearLayout changeAccountPassword = findViewById(R.id.changePassword);
        LinearLayout aboutUs = findViewById(R.id.aboutUs);
        LinearLayout logoutButton = findViewById(R.id.logout);
        LinearLayout faq= findViewById(R.id.faq);
        LinearLayout feedback= findViewById(R.id.feedback);
        LinearLayout privacyPolicy= findViewById(R.id.privacyPolicy);

        // Load Current Profile Image
        loadUserProfile();

        // Open Gallery When Clicking "Change Account Image"
        changeAccountImage.setOnClickListener(v -> openGallery());

        // Open Change Name Screen
        changeAccountName.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangeAccountNameActivity.class);
            startActivity(intent);
        });

        changeAccountPassword.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        aboutUs.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AboutUsActivity.class);
            startActivity(intent);
        });
        faq.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FAQsActivity.class);
            startActivity(intent);
        });
        feedback.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });
        privacyPolicy.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });
        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation(bottomNavigationView);

        logoutButton.setOnClickListener(v -> logoutUser());
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadUserProfile();
    }
    private void logoutUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            for (com.google.firebase.auth.UserInfo profile : user.getProviderData()) {
                String providerId = profile.getProviderId();

                if (providerId.equals("google.com")) {
                    logoutGoogle();
                } else if (providerId.equals("facebook.com")) {
                    logoutFacebook();
                } else if (providerId.equals("twitter.com")) {
                    TwitterAuthManager.getInstance(this, null).signOut();
                }
            }
        }

        // Sign out from Firebase
        auth.signOut();

        // Clear shared preferences (user data)
        getSharedPreferences("user_prefs", MODE_PRIVATE).edit().clear().apply();

        // Clear Glide image cache
        Glide.get(this).clearMemory();
        new Thread(() -> Glide.get(this).clearDiskCache()).start();

        // Show logout message
        Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();

        // Redirect to login screen
        Intent intent = new Intent(ProfileActivity.this, LoginSignUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(intent);
        finish(); // Close current activity
    }
    private void logoutGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInClient.revokeAccess()
                .addOnCompleteListener(task -> Toast.makeText(this, "Google Logout Successful", Toast.LENGTH_SHORT).show());
    }
    private void logoutFacebook() {
        LoginManager.getInstance().logOut();
        Toast.makeText(this, "Facebook Logout Successful", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_profile;
    }
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null && user != null) {
            // Unique file name for each user
            StorageReference fileReference = storageReference.child(user.getUid() + ".jpg");

            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateProfileImage(uri.toString());
                    })
            ).addOnFailureListener(e ->
                    Toast.makeText(ProfileActivity.this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void updateProfileImage(String imageUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            progressBar.setVisibility(View.VISIBLE);
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(imageUrl))
                    .build();

            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    user.reload().addOnCompleteListener(reloadTask -> {
                        if (reloadTask.isSuccessful()) {
                            UserUtils.markUserProfileImageAsSet(getApplicationContext(), imageUrl);
                            // Force Glide to load the new image immediately
                            Glide.with(ProfileActivity.this)
                                    .load(imageUrl)
                                    .placeholder(Drawable.createFromPath(imageUrl)) // Use the new image as placeholder to prevent flickering
                                    .error(R.drawable.profile_image)
                                    .circleCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Avoids loading old cached image
                                    .skipMemoryCache(true)
                                    .into(profileImage);
                            Toast.makeText(ProfileActivity.this, "Profile Image Updated!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ProfileActivity.this, "Update Failed!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}