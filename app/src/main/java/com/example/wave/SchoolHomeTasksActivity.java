package com.example.wave;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SchoolHomeTasksActivity extends BaseActivity implements TaskCompletionListener  {

    private boolean isSchoolTasksActive = true; // Track active fragment
    private HomeTasksFragment homeTasksFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_school_tasks);
        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation(bottomNavigationView);
        // Check if activity was opened with a flag to load HomeTasksFragment
        boolean showHomeTasks = getIntent().getBooleanExtra("showHomeTasks", false);

        // Load the correct fragment
        if (savedInstanceState == null) {
            if (showHomeTasks) {
                loadFragment(new HomeTasksFragment());
                isSchoolTasksActive = false;
            } else {
                loadFragment(new SchoolTasksFragment());
                isSchoolTasksActive = true;
            }
        }
    }

    @Override
    protected int getCurrentMenuItemId() {
        return -1; // No selection
    }


    /**
     * Load a fragment into the container.
     *
     * @param fragment Fragment to load
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_school_tasks_container, fragment);
        transaction.addToBackStack(null); // ✅ Preserve state when switching
        transaction.commit();
    }


    /**
     * Switch to SchoolTasksFragment.
     */
    public void showSchoolTasksFragment() {
        Fragment existingFragment = getSupportFragmentManager().findFragmentById(R.id.home_school_tasks_container);
        if (!(existingFragment instanceof SchoolTasksFragment)) { // ✅ Only replace if it's not already showing
            loadFragment(new SchoolTasksFragment());
            isSchoolTasksActive = true;
        }
    }

    public void showHomeTasksFragment() {
        Fragment existingFragment = getSupportFragmentManager().findFragmentById(R.id.home_school_tasks_container);
        if (!(existingFragment instanceof HomeTasksFragment)) { // ✅ Prevent unnecessary reloads
            loadFragment(new HomeTasksFragment());
            isSchoolTasksActive = false;
        }
    }


    @Override
    public void onTaskCompletedUpdate() {
        HomeTasksFragment homeTasksFragment = (HomeTasksFragment) getSupportFragmentManager()
                .findFragmentById(R.id.home_school_tasks_container); // 🔹 Ensure this ID matches your layout

        if (homeTasksFragment instanceof HomeTasksFragment) {
            homeTasksFragment.onTaskCompletedUpdate();
        } else {
            Log.e("SchoolHomeTasksActivity", "HomeTasksFragment not found or incorrect type!");
        }
    }


}
