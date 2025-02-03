package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Abstract method for child classes to define their current menu item
    protected abstract int getCurrentMenuItemId();

    protected void setupBottomNavigation(BottomNavigationView bottomNavigationView) {
        int currentMenuItemId = getCurrentMenuItemId();
        bottomNavigationView.setSelectedItemId(currentMenuItemId); // Highlight current tab
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
}
