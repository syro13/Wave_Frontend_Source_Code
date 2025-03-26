package com.example.wave.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.example.wave.AppController;
import com.example.wave.LoadingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UserUtils {

    private static final String USER_PREFS = "user_prefs";
    public static void saveUserData(Context context, FirebaseUser user, String newName) {
        if (user == null) {
            Log.e("UserUtils", "Cannot save user data: FirebaseUser is null");
            return;
        }

        SharedPreferences preferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String userId = user.getUid();
        boolean hasUserSetProfileImage = preferences.getBoolean(userId + "_profile_image_set", false);

        String profileImageUrl=null;

        if (hasUserSetProfileImage) {
            profileImageUrl = preferences.getString(userId + "_profile_image_url", null);
        } else {
            Uri firebasePhotoUri = user.getPhotoUrl();
            if (firebasePhotoUri != null) {
                String firebaseUrl = firebasePhotoUri.toString();
                boolean isOAuthDefaultImage = firebaseUrl.contains("googleusercontent") ||
                        firebaseUrl.contains("facebook.com") ||
                        firebaseUrl.contains("twimg.com");

                if (!isOAuthDefaultImage) {
                    profileImageUrl = firebaseUrl;

                    // Cache this now, because SharedPreferences is likely wiped after reinstall
                    editor.putBoolean(userId + "_profile_image_set", true);
                    editor.putString(userId + "_profile_image_url", profileImageUrl);
                    hasUserSetProfileImage = true;

                    Log.d("UserUtils", "Restoring manually set image from Firebase URI");
                }
            }
        }

        editor.putBoolean(userId + "_profile_image_set", hasUserSetProfileImage);
        editor.putString(userId + "_profile_image_url", profileImageUrl);
        editor.apply();

        AppController.getInstance().updateProfileImageUrl(profileImageUrl);

        final String finalProfileImageUrl = profileImageUrl;

        // Update Firebase User Profile with our forced image
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .setPhotoUri(finalProfileImageUrl != null ? Uri.parse(finalProfileImageUrl) : null) // Ensures Firebase does not override
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(updateTask -> {
            if (updateTask.isSuccessful()) {
                user.reload().addOnCompleteListener(reloadTask -> {
                    Log.d("UserUtils", "User data saved: " + newName + ", " + finalProfileImageUrl);
                    updateDashboardUI(context);
                    navigateToLoadingActivity(context);
                });
            } else {
                Log.e("UserUtils", "Failed to update user profile", updateTask.getException());
            }
        });
    }

    public static void markUserProfileImageAsSet(Context context, String imageUrl) {
        SharedPreferences preferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            editor.putBoolean(userId + "_profile_image_set", true); // Mark as manually set
            editor.putString(userId + "_profile_image_url", imageUrl); // Save new profile image
            editor.apply();
        }

        AppController.getInstance().updateProfileImageUrl(imageUrl);
        Log.d("UserUtils", "User manually set profile image: " + imageUrl);
    }

    public static String getSavedUserName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        return preferences.getString("user_name", "User"); // Default to "User" if missing
    }

    public static void navigateToLoadingActivity(Context context) {
        Intent intent = new Intent(context, LoadingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Log.d("UserUtils", "Navigating to LoadingActivity...");
    }

    public static void updateDashboardUI(Context context) {
        Intent intent = new Intent("UPDATE_DASHBOARD_UI");
        context.sendBroadcast(intent);
        Log.d("UserUtils", "Broadcast sent to update Dashboard UI.");
    }
}
