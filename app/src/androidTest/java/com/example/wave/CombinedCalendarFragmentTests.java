package com.example.wave;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CombinedCalendarFragmentTests {

    @Before
    public void setUp() {
        FragmentScenario.launchInContainer(CombinedCalendarFragment.class);
    }

    @Test //  Test if the CombinedCalendarFragment is displayed
    public void testToggleButtonsFunctionality() {
        // Click "House" toggle button and check UI update
        onView(withId(R.id.houseToggleButton)).perform(click());
        onView(withId(R.id.houseToggleButton)).check(matches(isDisplayed()));

        // Click "School" toggle button and check UI update
        onView(withId(R.id.schoolToggleButton)).perform(click());
        onView(withId(R.id.schoolToggleButton)).check(matches(isDisplayed()));

        // Click "Both" toggle button and check UI update
        onView(withId(R.id.bothToggleButton)).perform(click());
        onView(withId(R.id.bothToggleButton)).check(matches(isDisplayed()));
    }

    @Test // Test month navigation
    public void testMonthNavigation() {
        // Click next month button and verify UI response
        onView(withId(R.id.nextMonth)).perform(click());
        onView(withId(R.id.calendarRecyclerView)).check(matches(isDisplayed()));

        // Click previous month button and verify UI response
        onView(withId(R.id.previousMonth)).perform(click());
        onView(withId(R.id.calendarRecyclerView)).check(matches(isDisplayed()));
    }
}
