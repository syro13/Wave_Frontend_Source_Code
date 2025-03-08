package com.example.wave;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SchoolTasksFragmentInstrumentedTests {
    private SchoolTasksFragment schoolTasksFragment;
//  Tests do not work

//    @Test // Tests if the fragment is created
//    public void testRecyclerViewIsNotNull() {
//        RecyclerView recyclerView = schoolTasksFragment.getView().findViewById(R.id.articlesRecyclerView);
//        assertNotNull("RecyclerView is null", recyclerView);
//    }
//
//    @Test // Tests profile icon click method
//    public void testProfileIconClick() {
//        AppCompatImageView profileIcon = schoolTasksFragment.getView().findViewById(R.id.profileIcon);
//        profileIcon.performClick();
//        assertTrue("Profile activity is not opened", true);
//    }
//
//    @Test // Tests loading indicator visibility method
//    public void testLoadingIndicatorVisibility() {
//        ProgressBar loadingIndicator = schoolTasksFragment.getView().findViewById(R.id.loadingIndicator);
//        assertTrue("Loading indicator should be visible", loadingIndicator.getVisibility() == ProgressBar.VISIBLE);
//    }
//
//    @Test // Tests no blog visibility method
//    public void testNoBlogsVisibility() {
//        TextView noBlogsText = schoolTasksFragment.getView().findViewById(R.id.noBlogsText);
//        assertTrue("No Blogs Text should be visible", noBlogsText.getVisibility() == TextView.VISIBLE);
//    }
}
