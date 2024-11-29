package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.example.wave.R.id;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        // Placeholder for navigation logic
        // Uncomment this when the activities are created
        int itemId = item.getItemId();
        Intent intent = null;

        if (itemId == R.id.nav_index) {
            intent = new Intent(this, OnboardingActivity.class);
       // } else if (itemId == R.id.nav_calendar) {
         //   intent = new Intent(this, CalendarActivity.class);
      //  } else if (itemId == R.id.nav_settings) {
         //   intent = new Intent(this, SettingsActivity.class);
      //  } else if (itemId == R.id.nav_profile) {
      //      intent = new Intent(this, ProfileActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
        return false;
    }
}
