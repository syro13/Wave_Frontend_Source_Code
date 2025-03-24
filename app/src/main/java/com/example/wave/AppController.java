package com.example.wave;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AppController extends Application {
    private static AppController instance;
    public static final String DEFAULT_PROFILE_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/wave-7269b.firebasestorage.app/o/profile_image.png?alt=media&token=5f6b26d9-06ec-4a22-861b-d586a40bcd97";
    private String profileImageUrl;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // Force Light Mode for the entire app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Load User Profile Image at App Start
        loadUserProfile();

    }
    public static AppController getInstance() {
        return instance;
}
    public void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            profileImageUrl = user.getPhotoUrl().toString();
            // Store the latest profile image in AppController
            AppController.getInstance().updateProfileImageUrl(profileImageUrl);
            // Preload the image to speed up UI updates
            Glide.with(this).load(profileImageUrl).preload();
        } else {
            profileImageUrl = DEFAULT_PROFILE_IMAGE_URL;
        }
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }


    public void updateProfileImageUrl(String newUrl) {
        profileImageUrl = newUrl;
    }
}
