package com.example.wave;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public abstract class BaseActivity extends AppCompatActivity implements NetworkReceiver.NetworkChangeListener {
    private ImageView profileIcon;
    private NetworkReceiver networkReceiver;
    private View noInternetOverlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterNetworkReceiver();
    }
    private void registerNetworkReceiver() {
        if (networkReceiver == null) {
            networkReceiver = new NetworkReceiver(this);
        }
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }
    private void unregisterNetworkReceiver() {
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (findViewById(R.id.profileIcon) != null) {
            loadUserProfile();
        }
        // Fallback check if overlay view is present but no broadcast received yet
        if (noInternetOverlay != null) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

            if (!isConnected) {
                showNoInternetUI();
            } else {
                hideNoInternetUI();
            }
        }
    }
    @Override
    public void onNetworkConnected() {
        hideNoInternetUI();
    }

    @Override
    public void onNetworkDisconnected() {
        showNoInternetUI();
    }

    protected void showNoInternetUI() {
        if (noInternetOverlay != null) {
            noInternetOverlay.setVisibility(View.VISIBLE);
        }
    }

    protected void hideNoInternetUI() {
        if (noInternetOverlay != null) {
            noInternetOverlay.setVisibility(View.GONE);
        }
    }

    protected void setNoInternetOverlay(View noInternetOverlay) {
        this.noInternetOverlay = noInternetOverlay;
    }
    protected void configureNoInternetOverlay() {
        View retryButton = findViewById(R.id.retryButton);
        if (retryButton != null) {
            retryButton.setOnClickListener(v -> {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

                if (isConnected) {
                    hideNoInternetUI();
                    onNetworkConnected(); // Manually call in case overlay was stuck
                } else {
                    Toast.makeText(this, "Still no internet connection", Toast.LENGTH_SHORT).show();
                }
            });
        }
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

    void loadUserProfile() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.e("BaseActivity", "User is null, cannot load profile.");
            return;
        }

        // Fetch user name
        String userName = user.getDisplayName();
        if (userName == null || userName.trim().isEmpty()) {
            userName = preferences.getString(user.getUid() + "_user_name", "User"); // Fallback to stored or default name
        }

        Log.d("BaseActivity", "Loading user name: " + userName);

        // Ensure `userNameTextView` exists and update it
        TextView userNameTextView = findViewById(R.id.userNameTextView);
        if (userNameTextView != null) {
            userNameTextView.setText(userName);
        } else {
            Log.w("BaseActivity", "Skipping user name update: TextView not found.");
        }

        // Ensure `profileIcon` exists
        if (profileIcon == null) {
            profileIcon = findViewById(R.id.profileIcon);
        }

        if (profileIcon == null) {
            Log.w("BaseActivity", "Skipping profile image loading: profileIcon is null.");
            return;
        }

        // Fetch cached image URL from SharedPreferences
        String cachedProfileImage = preferences.getString(user.getUid() + "_profile_image_url", null);
        Uri firebasePhotoUri = user.getPhotoUrl();
        String latestImageUrl = (firebasePhotoUri != null) ? firebasePhotoUri.toString() : null;

        // Determine which image to use
        String imageUrlToLoad;
        if (cachedProfileImage != null && !cachedProfileImage.equals(AppController.DEFAULT_PROFILE_IMAGE_URL)) {
            imageUrlToLoad = cachedProfileImage; // Use cached image if set
        } else if (latestImageUrl != null) {
            imageUrlToLoad = latestImageUrl; // Use Firebase image if available
        } else {
            imageUrlToLoad = AppController.DEFAULT_PROFILE_IMAGE_URL; // Use default image
        }

        Log.d("BaseActivity", "Loading profile image: " + imageUrlToLoad);

        // Load the profile image with Glide (Prevent Flickering)
        Glide.with(this)
                .load(imageUrlToLoad)
                .placeholder(Drawable.createFromPath(imageUrlToLoad)) // Prevents default image flicker
                .error(R.drawable.profile_image)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // Caches correct image
                .into(profileIcon);

        // Update SharedPreferences with the correct image
        if (!imageUrlToLoad.equals(cachedProfileImage)) {
            preferences.edit().putString(user.getUid() + "_profile_image_url", imageUrlToLoad).apply();
        }
    }

}
