package com.example.wave;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SetBudgetBottomSheetFragmentInstrumentedTest {

    // Test if the bottom sheet UI elements are displayed
    @Test
    public void testBottomSheetUIElements() {
        FragmentScenario<SetBudgetBottomSheetFragment> scenario =
                FragmentScenario.launchInContainer(SetBudgetBottomSheetFragment.class);

        // Check if the TextView for displaying the amount is displayed
        Espresso.onView(ViewMatchers.withId(R.id.amountValue))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Check if all keypad buttons are displayed
        int[] buttonIds = {
                R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4,
                R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9,
                R.id.buttonDot, R.id.buttonBackspace, R.id.submitButton
        };

        for (int id : buttonIds) {
            Espresso.onView(ViewMatchers.withId(id))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    // Test keypad interaction
    @Test
    public void testKeypadInteraction() {
        FragmentScenario<SetBudgetBottomSheetFragment> scenario =
                FragmentScenario.launchInContainer(SetBudgetBottomSheetFragment.class);

        // Simulate clicking on 1 and 2
        Espresso.onView(ViewMatchers.withId(R.id.button1)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.button2)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.buttonDot)).perform(ViewActions.click());

        // Check if the amount is displayed correctly should be 12
        Espresso.onView(ViewMatchers.withId(R.id.amountValue))
                .check(ViewAssertions.matches(ViewMatchers.withText("12.")));
    }
}
