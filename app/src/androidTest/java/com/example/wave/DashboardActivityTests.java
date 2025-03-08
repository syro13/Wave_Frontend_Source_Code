package com.example.wave;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DashboardActivityTests {

    @Before
    public void setUp() {
        ActivityScenario.launch(DashboardActivity.class);
    }

    @Test  // Test if the activity is created
    public void testUIElementsDisplayed() {
        // Verify key UI elements are displayed
        onView(withId(R.id.taskRecyclerView)).check(matches(isDisplayed()));
        onView(withId(R.id.bottomNavigationView)).check(matches(isDisplayed()));
        onView(withId(R.id.greetingText)).check(matches(isDisplayed()));
        onView(withId(R.id.currentDate)).check(matches(isDisplayed()));
        onView(withId(R.id.weatherIcon)).check(matches(isDisplayed()));

        // Check navigation cards
        onView(withId(R.id.homeTasksCard)).check(matches(isDisplayed()));
        onView(withId(R.id.schoolTasksCard)).check(matches(isDisplayed()));
        onView(withId(R.id.wellnessTasksCard)).check(matches(isDisplayed()));
        onView(withId(R.id.budgetTasksCard)).check(matches(isDisplayed()));
    }

//    @Test
//    public void testNavigationCardClicks() {
//        onView(withId(R.id.homeTasksCard)).perform(click());
//        onView(withId(R.id.taskRecyclerView)).check(matches(isDisplayed()));
//
//    }

    @Test // Test if the profile icon is clickable
    public void testProfileIconClick() {
        onView(withId(R.id.profileIcon)).perform(click());
    }
}
