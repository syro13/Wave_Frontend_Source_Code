package com.example.wave;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public abstract class BaseActivity extends AppCompatActivity {
    private ImageView profileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Abstract method for child classes to define their current menu item
    protected abstract int getCurrentMenuItemId();

    protected void setupBottomNavigation(BottomNavigationView bottomNavigationView) {
        int currentMenuItemId = getCurrentMenuItemId();

        if (currentMenuItemId != -1) {
            bottomNavigationView.setSelectedItemId(currentMenuItemId);
        }

        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }


    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == getCurrentMenuItemId()) {
            // If the current activity is already selected, do nothing
            return true;
        }

        Intent intent = null;

        if (itemId == R.id.nav_index) {
            intent = new Intent(this, DashboardActivity.class);
        } else if (itemId == R.id.nav_calendar) {
            intent = new Intent(this, SchoolHomeCalendarActivity.class);
        } else if (itemId == R.id.nav_settings) {
            intent = new Intent(this, SettingsActivity.class);
        } else if (itemId == R.id.nav_profile) {
            intent = new Intent(this, ProfileActivity.class);
        }



        if (intent != null) {
            startActivity(intent);
            overridePendingTransition(0, 0); // Optional: Smooth transition
            finish(); // Close the current activity
        }

        return true; // Mark the item as selected
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (findViewById(R.id.profileIcon) != null) {
            loadUserProfile();
        }
    }
    private void loadUserProfile() {
        if (profileIcon == null) {
            profileIcon = findViewById(R.id.profileIcon);
        }
        if (profileIcon != null) {
            String cachedProfileImage = AppController.getInstance().getProfileImageUrl();
            Glide.with(this)
                    .load(cachedProfileImage)
                    .placeholder(R.drawable.profile_image)
                    .error(R.drawable.profile_image)
                    .circleCrop()
                    .into(profileIcon);
            // Fetch latest image in the background
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Uri photoUri = user.getPhotoUrl();
                if (photoUri != null) {
                    String latestImageUrl = photoUri.toString();
                    if (!latestImageUrl.equals(cachedProfileImage)) {
                        // Update the cache and UI
                        AppController.getInstance().updateProfileImageUrl(latestImageUrl);

                        Glide.with(this)
                                .load(latestImageUrl)
                                .placeholder(R.drawable.profile_image)
                                .error(R.drawable.profile_image)
                                .circleCrop()
                                .into(profileIcon);
                    }
                }
            }
        }else {
                profileIcon.setImageResource(R.drawable.profile_image);
            }
        }
}
