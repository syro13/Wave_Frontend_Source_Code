package com.example.wave;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoadingActivity extends AppCompatActivity {
    private static final int MIN_LOADING_TIME = 3000; // Minimum loading time in ms (3s)
    private LottieAnimationView waveProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading); // Create a loading layout
        waveProgressBar = findViewById(R.id.waveProgressBar);
        waveProgressBar.playAnimation();
        // Start fetching user data
        fetchUserData();
    }

    private void fetchUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // If user is not logged in, send them to login
            navigateToLogin();
            return;
        }

        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Fetch user name
        String userName = user.getDisplayName();
        if (userName != null) {
            editor.putString("user_name", userName);
        }

        String storedImageUrl = preferences.getString("profile_image_url", null);
        Uri firebasePhotoUri = user.getPhotoUrl();
        String firebasePhotoUrl = (firebasePhotoUri != null) ? firebasePhotoUri.toString() : null;

        // Only update if needed (prevents flickering from default image)
        if (storedImageUrl == null || storedImageUrl.equals(AppController.DEFAULT_PROFILE_IMAGE_URL)) {
            editor.putString("profile_image_url", (firebasePhotoUrl != null) ? firebasePhotoUrl : AppController.DEFAULT_PROFILE_IMAGE_URL);
        }

        editor.apply();

        // Preload image to make sure it's cached before UI loads
        Glide.with(this)
                .load(firebasePhotoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Ensures fast loading
                .preload();


        // Ensure minimum loading time
        new Handler().postDelayed(this::navigateToDashboard, MIN_LOADING_TIME);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(LoadingActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish(); // Close this loading screen
    }

    private void navigateToLogin() {
        Intent intent = new Intent(LoadingActivity.this, LoginSignUpActivity.class);
        startActivity(intent);
        finish();
    }
}
