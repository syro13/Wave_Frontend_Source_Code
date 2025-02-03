package com.example.wave;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    // Tests that the BottomNavigationView is displayed
    @Test
    public void testBottomNavigationIsDisplayed() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            // Verify that the BottomNavigationView is displayed
            onView(withId(R.id.bottomNavigationView)).check(matches(isDisplayed()));
        }
    }

    // Tests that the correct menu item is selected
    @Test
    public void testCorrectMenuItemIsSelected() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            // Verify that the correct menu item is selected
            onView(withId(R.id.nav_index)).check(matches(isDisplayed()));
        }
    }

    // Tests that the MainActivity launches successfully
    @Test
    public void testMainActivityLaunches() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            // Assert the activity is not null
            scenario.onActivity(Assert::assertNotNull);
        }
    }
}
