package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class LaunchActivityInstrumentedTest {

    @Before
    public void setUp() {
        // Initialize the scenario
        ActivityScenario.launch(LaunchActivity.class);
    }

    @Test // Test if the activity is created
    public void testLottieAnimationIsPlaying() {
        onView(withId(R.id.lottieAnimation)).check(matches(isDisplayed()));
    }
}
