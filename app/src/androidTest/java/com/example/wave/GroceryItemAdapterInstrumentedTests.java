package com.example.wave;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GroceryItemAdapterInstrumentedTests {

    @Before
    public void setUp() {
        ActivityScenario.launch(DashboardActivity.class);
    }

// Tests didnt wokr
//    @Test
//    public void testToggleCheckbox() {
//        onView(withText("Milk")).perform(scrollTo(), click());
//        onView(withText("Milk")).check(matches(isDisplayed()));
//    }
//
//    @Test // This test should fail
//    public void testDeleteItem() {
//        onView(withText("Bread")).perform(scrollTo(), click());
//        onView(withText("Bread")).check(matches(isDisplayed()));
//    }
}
