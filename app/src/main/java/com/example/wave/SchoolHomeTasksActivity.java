package com.example.wave;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class SchoolHomeTasksActivity extends AppCompatActivity {

    private boolean isSchoolTasksActive = true; // Track active fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_school_tasks);

        // Load the SchoolTasksFragment by default
        if (savedInstanceState == null) {
            loadFragment(new SchoolTasksFragment());
        }
    }

    /**
     * Load a fragment into the container.
     *
     * @param fragment Fragment to load
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_school_tasks_container, fragment);
        transaction.commit();
    }

    /**
     * Switch to SchoolTasksFragment.
     */
    public void showSchoolTasksFragment() {
        if (!isSchoolTasksActive) {
            loadFragment(new SchoolTasksFragment());
            isSchoolTasksActive = true;
        }
    }

    /**
     * Switch to HomeTasksFragment.
     */
    public void showHomeTasksFragment() {
        if (isSchoolTasksActive) {
            loadFragment(new HomeTasksFragment());
            isSchoolTasksActive = false;
        }
    }
}
