package com.example.wave;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BudgetPlannerActivityInstrumentedTest {

    // Launch BudgetPlannerActivity before each test
    @Rule
    public ActivityScenarioRule<BudgetPlannerActivity> activityScenarioRule =
            new ActivityScenarioRule<>(BudgetPlannerActivity.class);

    // Test that the Add Expense button opens the Add Expense Bottom Sheet
    @Test
    public void testAddExpenseButtonOpensFragment() {
        Intents.init();

        // Click Add Expense button
        Espresso.onView(ViewMatchers.withId(R.id.addExpenseButton)).perform(ViewActions.click());

        // Verify that the Add Expense Bottom Sheet is displayed
        Espresso.onView(ViewMatchers.withText("Add Expense")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Intents.release();
    }
}
