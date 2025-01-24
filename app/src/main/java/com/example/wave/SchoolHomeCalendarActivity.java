package com.example.wave;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SchoolHomeCalendarActivity extends BaseActivity {

    private boolean isSchoolCalendarFragmentActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_school_calendar); // Updated with the correct layout file

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation(bottomNavigationView);

        // Load the SchoolCalendarFragment by default
        if (savedInstanceState == null) {
            loadFragment(new SchoolCalendarFragment());
        }
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_calendar; // The menu item ID for the Calendar tab
    }


    /**
     * Load a fragment into the container and track the current state.
     *
     * @param fragment Fragment to load
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_school_calendar_container, fragment); // Updated with correct ID
        transaction.commit();
    }

    public void showSchoolCalendarFragment() {
        if (!isSchoolCalendarFragmentActive) { // Only switch if not already active
            loadFragment(new SchoolCalendarFragment());
            isSchoolCalendarFragmentActive = true;
        }
    }

    public void showHomeCalendarFragment() {
        if (isSchoolCalendarFragmentActive) { // Only switch if not already active
            loadFragment(new HomeCalendarFragment());
            isSchoolCalendarFragmentActive = false;
        }
    }

    /**
     * Sets up bottom navigation with the given BottomNavigationView.
     *
     * @param bottomNavigationView The BottomNavigationView to configure.
     */
}
