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

    /**
     * Saves the user's profile image and name to SharedPreferences and updates AppController.
     * Ensures OAuth users (Google, Facebook, etc.) start with the default image until they manually change it.
     *
     * @param context Application context
     * @param user    FirebaseUser object containing user details
     * @param newName The name entered by the user during sign-up
     */
    public static void saveUserData(Context context, FirebaseUser user, String newName) {
        if (user == null) {
            Log.e("UserUtils", "Cannot save user data: FirebaseUser is null");
            return;
        }

        SharedPreferences preferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String userId = user.getUid();
        boolean hasUserSetProfileImage = preferences.getBoolean(userId + "_profile_image_set", false);
        boolean isSocialLoginUser = false;

        for (UserInfo userInfo : user.getProviderData()) {
            String providerId = userInfo.getProviderId();
            if (providerId.equals("google.com") || providerId.equals("twitter.com") || providerId.equals("facebook.com")) {
                isSocialLoginUser = true;
                break;
            }
        }

        String profileImageUrl;

        if (isSocialLoginUser && !hasUserSetProfileImage) {
            profileImageUrl = null; // Use the default XML image
        } else {
            profileImageUrl = preferences.getString(userId + "_profile_image_url", null);
        }

        editor.putString(userId + "_profile_image_url", profileImageUrl);
        editor.putBoolean(userId + "_profile_image_set", hasUserSetProfileImage);
        editor.apply();

        AppController.getInstance().updateProfileImageUrl(profileImageUrl);

        // 🛠 Update Firebase User Profile with our forced image
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .setPhotoUri(profileImageUrl != null ? Uri.parse(profileImageUrl) : null) // Ensures Firebase does not override
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(updateTask -> {
            if (updateTask.isSuccessful()) {
                user.reload().addOnCompleteListener(reloadTask -> {
                    Log.d("UserUtils", "User data saved: " + newName + ", " + profileImageUrl);
                    updateDashboardUI(context);
                    navigateToLoadingActivity(context);
                });
            } else {
                Log.e("UserUtils", "Failed to update user profile", updateTask.getException());
            }
        });
    }

    /**
     * Marks that the user has set a custom profile image in ProfileActivity.
     *
     * @param context  Application context
     * @param imageUrl The new profile image URL
     */
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
