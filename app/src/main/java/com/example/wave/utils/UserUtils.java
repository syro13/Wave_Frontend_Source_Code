package com.example.wave.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.example.wave.AppController;
import com.example.wave.LoadingActivity;
import com.google.firebase.auth.FirebaseUser;

public class UserUtils {

    private static final String USER_PREFS = "user_prefs";

    /**
     * Saves the user's profile image and name to SharedPreferences and updates AppController.
     * Also redirects the user to LoadingActivity after saving.
     *
     * @param context Application context
     * @param user FirebaseUser object containing user details
     */
    public static void saveUserData(Context context, FirebaseUser user) {
        if (user == null) {
            Log.e("UserUtils", "Cannot save user data: FirebaseUser is null");
            return;
        }

        // Force reload user data to get the latest name
        user.reload().addOnCompleteListener(task -> {
            SharedPreferences preferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            // Save user name (use latest updated name from Firebase)
            String displayName = (user.getDisplayName() != null && !user.getDisplayName().isEmpty())
                    ? user.getDisplayName()
                    : "User"; // Fallback name
            editor.putString("user_name", displayName);

            // Save profile image URL
            Uri photoUri = user.getPhotoUrl();
            String profileImageUrl = (photoUri != null) ? photoUri.toString() : AppController.DEFAULT_PROFILE_IMAGE_URL;
            editor.putString("profile_image_url", profileImageUrl);

            editor.apply(); // Save the data

            // Update AppController for instant in-memory access
            AppController.getInstance().updateProfileImageUrl(profileImageUrl);

            Log.d("UserUtils", "User data saved: " + displayName + ", " + profileImageUrl);

            // Navigate to LoadingActivity after saving user data
            navigateToLoadingActivity(context);
            updateDashboardUI(context);
        });
    }

    /**
     * Retrieves the stored user name from SharedPreferences.
     *
     * @param context Application context
     * @return The stored user name or "User" if not found.
     */
    public static String getSavedUserName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        return preferences.getString("user_name", "User"); // Default to "User" if missing
    }

    /**
     * Navigates to LoadingActivity and clears the back stack.
     *
     * @param context Application context
     */
    public static void navigateToLoadingActivity(Context context) {
        Intent intent = new Intent(context, LoadingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Log.d("UserUtils", "Navigating to LoadingActivity...");
    }
    /**
     * Updates the UI in DashboardActivity with the latest user name.
     * @param context Application context
     */
    public static void updateDashboardUI(Context context) {
        Intent intent = new Intent("UPDATE_DASHBOARD_UI");
        context.sendBroadcast(intent);
        Log.d("UserUtils", "Broadcast sent to update Dashboard UI.");
    }

}