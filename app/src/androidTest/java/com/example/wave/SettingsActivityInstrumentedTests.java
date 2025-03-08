package com.example.wave;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SettingsActivityInstrumentedTests {

    @Rule
    public ActivityTestRule<SettingsActivity> activityTestRule = new ActivityTestRule<>(SettingsActivity.class);

    private SettingsActivity settingsActivity;

    @Before
    public void setUp() {
        // Initialize activity instance
        settingsActivity = activityTestRule.getActivity();
    }


    @Test // Test if the activity is created
    public void testGetCurrentMenuItemId_returnsNavSettings() {
        int currentMenuItemId = settingsActivity.getCurrentMenuItemId();
        assert(currentMenuItemId == R.id.nav_settings);
    }
}
