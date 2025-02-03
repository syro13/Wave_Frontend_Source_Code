package com.example.wave;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import android.media.MediaPlayer;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LaunchActivityTests {

    // Test if the activity launches successfully and displays the animation
    @Test
    public void testActivityLaunchesSuccessfully() {
        try (ActivityScenario<LaunchActivity> scenario = ActivityScenario.launch(LaunchActivity.class)) {
            onView(withId(R.id.lottieAnimation)).check(matches(isDisplayed()));
        }
    }

    // Test if MediaPlayer is initialized and started
    @Test
    public void testMediaPlayerInitialization() {
        try (ActivityScenario<LaunchActivity> scenario = ActivityScenario.launch(LaunchActivity.class)) {
            scenario.onActivity(activity -> {
                MediaPlayer mediaPlayer = activity.mediaPlayer;
                assertNotNull(mediaPlayer);
                // Verify MediaPlayer is playing
                assertTrue(mediaPlayer.isPlaying());
            });
        }
    }
}
