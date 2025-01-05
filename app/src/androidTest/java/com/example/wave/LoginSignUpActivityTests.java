package com.example.wave;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class LoginSignUpActivityTests {

    // Test that the default fragment is the LoginFragment
    @Test
    public void testDefaultFragmentIsLoginFragment() {
        // Launch the LoginSignUpActivity
        ActivityScenario<LoginSignUpActivity> scenario = ActivityScenario.launch(LoginSignUpActivity.class);

        // Assert that the LoginFragment is displayed
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));
        scenario.onActivity(activity -> {
            Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            assertTrue(currentFragment instanceof LoginFragment);
        });
    }

    // Test switching to the SignupFragment
    @Test
    public void testSwitchToSignupFragment() {
        // Launch the LoginSignUpActivity
        ActivityScenario<LoginSignUpActivity> scenario = ActivityScenario.launch(LoginSignUpActivity.class);

        // Switch to the SignupFragment
        scenario.onActivity(LoginSignUpActivity::showSignupFragment);

        // Assert that the SignupFragment is displayed
        scenario.onActivity(activity -> {
            Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            assertTrue(currentFragment instanceof SignUpFragment);
        });
    }

    // Test switching back to the LoginFragment from the SignupFragment
    @Test
    public void testSwitchBackToLoginFragment() {
        // Launch the LoginSignUpActivity
        ActivityScenario<LoginSignUpActivity> scenario = ActivityScenario.launch(LoginSignUpActivity.class);

        // Switch to the SignupFragment
        scenario.onActivity(LoginSignUpActivity::showSignupFragment);

        // Switch back to the LoginFragment
        scenario.onActivity(LoginSignUpActivity::showLoginFragment);

        // Assert that the LoginFragment is displayed
        scenario.onActivity(activity -> {
            Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            assertTrue(currentFragment instanceof LoginFragment);
        });
    }
}
