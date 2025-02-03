package com.example.wave;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

@RunWith(AndroidJUnit4.class)
public class SignUpFragmentTests {

    // Test sign up button click with empty input
    @Test
    public void testSignUpUserInputValidation() {
        // Launch the SignUpFragment
        FragmentScenario<SignUpFragment> scenario = FragmentScenario.launchInContainer(SignUpFragment.class);

        // Test empty name input
        onView(withId(R.id.signupSubmitButton)).perform(click());
        onView(withId(R.id.nameInput)).check(matches(withText("")));

        // Enter a name and leave other fields empty
        onView(withId(R.id.nameInput)).perform(replaceText("John Doe"));
        onView(withId(R.id.signupSubmitButton)).perform(click());
        onView(withId(R.id.emailInput)).check(matches(withText("")));
    }


    // Test Sign-Up button click with valid input
    @Test
    public void testSignUpButtonClickWithValidInput() {
        // Launch the SignUpFragment
        FragmentScenario<SignUpFragment> scenario = FragmentScenario.launchInContainer(SignUpFragment.class);

        // Enter valid input
        onView(withId(R.id.nameInput)).perform(replaceText("John Doe"));
        onView(withId(R.id.emailInput)).perform(replaceText("john.doe@example.com"));
        onView(withId(R.id.passwordInput)).perform(replaceText("password123"));
        onView(withId(R.id.confirmPasswordInput)).perform(replaceText("password123"));

        // Click the Sign-Up button
        onView(withId(R.id.signupSubmitButton)).perform(click());
    }

    // Test Sign-Up button click with invalid input
    // WILL NOT WORK YET SINCE WE HAVE NOT IMPLIMENTED THE VALIDATION
//    @Test
//    public void testSignUpButtonClickWithInvalidInput() {
//        // Launch the SignUpFragment
//        FragmentScenario<SignUpFragment> scenario = FragmentScenario.launchInContainer(SignUpFragment.class);
//
//        // Enter a valid name and email, but mismatched passwords
//        onView(withId(R.id.nameInput)).perform(replaceText("John Doe"));
//        onView(withId(R.id.emailInput)).perform(replaceText("john.doe@example.com"));
//        onView(withId(R.id.passwordInput)).perform(replaceText("password123"));
//        onView(withId(R.id.confirmPasswordInput)).perform(replaceText("password456"));
//
//        // Click the Sign-Up button
//        onView(withId(R.id.signupSubmitButton)).perform(click());
//
//        // Verify that the confirmPasswordInput field displays an error message
//        onView(withId(R.id.confirmPasswordInput)).check(matches(hasErrorText("Passwords do not match")));
//    }


    // Test Google Sign-Up button click
    @Test
    public void testGoogleSignUpButtonClick() {
        // Launch the SignUpFragment
        FragmentScenario<SignUpFragment> scenario = FragmentScenario.launchInContainer(SignUpFragment.class);

        // Click the Google Sign up button
        onView(withId(R.id.googleIcon)).perform(click());
    }
}
