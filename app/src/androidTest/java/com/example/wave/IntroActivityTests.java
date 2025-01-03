package com.example.wave;

import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class IntroActivityTests {

    @Test
    public void testButtonExists() {
        try (ActivityScenario<IntroActivity> scenario = ActivityScenario.launch(IntroActivity.class)) {
            // Checks to see if get started button is displayed
            onView(withId(R.id.getStartedButton)).check(matches(isDisplayed()));
        }
    }
}
