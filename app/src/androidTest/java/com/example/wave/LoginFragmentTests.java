package com.example.wave;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginFragmentTests {

    // This is not yet implemented !!!
    // Tests the login with empty fields
//    @Test
//    public void testLoginWithEmptyFields() {
//        FragmentScenario<LoginFragment> scenario = FragmentScenario.launchInContainer(LoginFragment.class);
//
//        // Click the Login Submit button
//        onView(withId(R.id.loginSubmitButton)).perform(click());
//
//        // Verify the error messages
//        onView(withId(R.id.emailInput)).check(matches(withText("Email is required")));
//        onView(withId(R.id.passwordInput)).check(matches(withText("Password is required")));
//    }

    // Tests the login with invalid email
    @Test
    public void testGoogleSignIn() {
        FragmentScenario<LoginFragment> scenario = FragmentScenario.launchInContainer(LoginFragment.class);
        onView(withId(R.id.googleIcon)).perform(click());
    }
}
