package com.example.wave;

import android.content.Intent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class LoginFragmentInstrumentedTest {

    @Before
    public void setUp() {
        ActivityScenario.launch(LoginSignUpActivity.class);
    }

    @Test
    public void testGoogleSignIn() {
        FragmentScenario<LoginFragment> scenario = FragmentScenario.launchInContainer(LoginFragment.class);
        onView(withId(R.id.googleIcon)).perform(click());
    }

//    @Test
//    public void testEmailLoginSuccess() {
//        // Input valid email and password
//        Espresso.onView(withId(R.id.emailInput))
//                .perform(ViewActions.typeText("testuser@example.com"), ViewActions.closeSoftKeyboard());
//
//        Espresso.onView(withId(R.id.passwordInput))
//                .perform(ViewActions.typeText("password123"), ViewActions.closeSoftKeyboard());
//
//        // Simulate clicking the login button
//        Espresso.onView(withId(R.id.loginSubmitButton)).perform(ViewActions.click());
//
//        // Verify that the user is redirected to DashboardActivity
//        Espresso.onView(withText("Dashboard")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
//    }
//
//    @Test
//    public void testEmailLoginFailure() {
//        // Input invalid email or password
//        Espresso.onView(withId(R.id.emailInput))
//                .perform(ViewActions.typeText("invaliduser@example.com"), ViewActions.closeSoftKeyboard());
//
//        Espresso.onView(withId(R.id.passwordInput))
//                .perform(ViewActions.typeText("wrongpassword"), ViewActions.closeSoftKeyboard());
//
//        // Simulate clicking the login button
//        Espresso.onView(withId(R.id.loginSubmitButton)).perform(ViewActions.click());
//
//        // Verify that the error message is displayed
//        Espresso.onView(withText("Login failed"))
//                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
//    }
//
//    @Test
//    public void testLoginValidation() {
//        // Check that error is shown when email or password is empty
//        Espresso.onView(withId(R.id.loginSubmitButton)).perform(ViewActions.click());
//        Espresso.onView(withId(R.id.emailInput)).check(ViewAssertions.matches(ViewMatchers.hasErrorText("Email is required")));
//        Espresso.onView(withId(R.id.passwordInput)).check(ViewAssertions.matches(ViewMatchers.hasErrorText("Password is required")));
//    }
//
//    @Test
//    public void testNavigationToSignUpFragment() {
//        // Simulate clicking the sign-up button
//        Espresso.onView(withId(R.id.signupButton)).perform(ViewActions.click());
//
//        // Verify that the sign-up fragment is displayed
//        Espresso.onView(withText("Sign Up")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
//    }
}
