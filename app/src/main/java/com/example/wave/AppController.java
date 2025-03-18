package com.example.wave;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AppController extends Application {
    private static AppController instance;
    public static final String DEFAULT_PROFILE_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/wave-7269b.firebasestorage.app/o/profile_image.png?alt=media&token=5f6b26d9-06ec-4a22-861b-d586a40bcd97";
    private String profileImageUrl;
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        // Force Light Mode for the entire app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Load User Profile Image at App Start
        loadUserProfile();

    }
    public static AppController getInstance() {
        return instance;
}
    public void loadUserProfile() {

        // Load cached image first
        profileImageUrl = preferences.getString("profile_image_url", DEFAULT_PROFILE_IMAGE_URL);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Uri photoUri = user.getPhotoUrl();
            if (photoUri != null) {
                String latestImageUrl = photoUri.toString();

                if (!latestImageUrl.equals(profileImageUrl)) {
                    profileImageUrl = latestImageUrl;
                    saveProfileImageUrl(profileImageUrl);
                }
            }
        }

        // Ensure the profile image is preloaded
        Glide.with(this).load(profileImageUrl).preload();

    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }


    public void updateProfileImageUrl(String newUrl) {
        if (newUrl != null && !newUrl.equals(profileImageUrl)) {
            profileImageUrl = newUrl;
            saveProfileImageUrl(newUrl);
        }
    }
    private void saveProfileImageUrl(String url) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("profile_image_url", url);
        editor.apply();
    }

}
