package com.example.wave;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OnboardingActivityTests {

    @Before
    public void setUp() {
        // Initialize Espresso Intents for intent verification
        init();
    }

    @After
    public void tearDown() {
        // Release Espresso Intents
        release();
    }

    // Test that the OnboardingActivity launches successfully
    @Test
    public void testActivityLaunches() {
        try (ActivityScenario<OnboardingActivity> scenario = ActivityScenario.launch(OnboardingActivity.class)) {
            onView(withId(R.id.viewPager)).check(matches(isDisplayed()));
            onView(withId(R.id.nextButton)).check(matches(isDisplayed()));
            onView(withId(R.id.skipButton)).check(matches(isDisplayed()));
            onView(withId(R.id.backButton)).check(matches(isDisplayed()));
        }
    }

    // Test that the Skip button navigates to the LoginSignUpActivity
    @Test
    public void testSkipButtonNavigatesToLoginSignUpActivity() {
        try (ActivityScenario<OnboardingActivity> scenario = ActivityScenario.launch(OnboardingActivity.class)) {
            onView(withId(R.id.skipButton)).perform(click());
            intended(hasComponent(LoginSignUpActivity.class.getName()));
        }
    }

    // Test that the Next button navigates to the next slide
    @Test
    public void testNextButtonNavigatesToNextSlide() {
        try (ActivityScenario<OnboardingActivity> scenario = ActivityScenario.launch(OnboardingActivity.class)) {
            onView(withId(R.id.nextButton)).perform(click());
            onView(withId(R.id.viewPager)).check(matches(isDisplayed()));
        }
    }

    // Test that the Back button navigates to the previous slide
    @Test
    public void testBackButtonNavigatesToIntroActivity() {
        try (ActivityScenario<OnboardingActivity> scenario = ActivityScenario.launch(OnboardingActivity.class)) {
            onView(withId(R.id.backButton)).perform(click());
            intended(hasComponent(IntroActivity.class.getName()));
        }
    }

    // Test that the indicators update correctly when swiping
    @Test
    public void testIndicatorsUpdateCorrectly() {
        try (ActivityScenario<OnboardingActivity> scenario = ActivityScenario.launch(OnboardingActivity.class)) {
            onView(withId(R.id.viewPager)).perform(swipeLeft());
            onView(withId(R.id.dot2)).check(matches(isDisplayed()));
        }
    }
}
