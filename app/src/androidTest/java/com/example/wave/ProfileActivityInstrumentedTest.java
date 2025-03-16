package com.example.wave;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.*;

@RunWith(AndroidJUnit4.class)
public class ProfileActivityInstrumentedTest {

    @Before
    public void setUp() {
    }


//    @Test // Test does not wokr
//    public void testBottomNavigationSelection() {
//        onView(withId(R.id.bottomNavigationView)).perform(click());
//        onView(withId(R.id.nav_settings)).check(matches(isSelected()));
//    }
}
