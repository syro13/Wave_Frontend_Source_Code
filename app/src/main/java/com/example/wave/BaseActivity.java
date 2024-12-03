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

    protected void setupBottomNavigation(BottomNavigationView bottomNavigationView, int currentMenuItemId) {
        bottomNavigationView.setSelectedItemId(currentMenuItemId); // Highlight current tab
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Intent intent = null;

        if (itemId == R.id.nav_index && !(this instanceof MainActivity)) {
            intent = new Intent(this, MainActivity.class);
      //  } else if (itemId == R.id.nav_calendar && !(this instanceof CalendarActivity)) {
       //     intent = new Intent(this, CalendarActivity.class);
      //  } else if (itemId == R.id.nav_settings && !(this instanceof SettingsActivity)) {
      //      intent = new Intent(this, SettingsActivity.class);
      //  } else if (itemId == R.id.nav_profile && !(this instanceof ProfileActivity)) {
      //      intent = new Intent(this, ProfileActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
            overridePendingTransition(0, 0); // Optional: Smooth transition
            finish(); // Close the current activity
        }

        return true; // Mark the item as selected
    }
}
