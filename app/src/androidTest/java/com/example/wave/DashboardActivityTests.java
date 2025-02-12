package com.example.wave;

import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@RunWith(AndroidJUnit4.class)
public class DashboardActivityTests {

    // Tests that the greeting text is displayed
//    @Test
//    public void testGreetingText() {
//        ActivityScenario<DashboardActivity> scenario = ActivityScenario.launch(DashboardActivity.class);
//
//        scenario.onActivity(activity -> {
//            TextView greetingText = activity.findViewById(R.id.greetingText);
//
//            // If no one is logged should display hello user
//            assertEquals("Hello User!", greetingText.getText().toString());
//        });
//    }

    // Tests that the current date is displayed
    @Test
    public void testCurrentDateDisplayed() {
        // Launch the activity
        ActivityScenario<DashboardActivity> scenario = ActivityScenario.launch(DashboardActivity.class);

        scenario.onActivity(activity -> {
            TextView currentDate = activity.findViewById(R.id.currentDate);

            // Verify the current date is displayed correctly
            String expectedDate = new SimpleDateFormat("EEE dd MMM", Locale.getDefault()).format(new Date());
            assertEquals(expectedDate, currentDate.getText().toString());
        });
    }
}
