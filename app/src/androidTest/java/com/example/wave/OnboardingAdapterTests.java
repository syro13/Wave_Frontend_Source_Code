package com.example.wave;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class OnboardingAdapterTests {

    // Test that the getItemCount method returns the correct number of items
    @Test
    public void testGetItemCount() {
        // Arrange
        List<OnboardingSlide> slides = new ArrayList<>();
        slides.add(new OnboardingSlide(R.drawable.image1, "Slide 1", "Description 1"));
        slides.add(new OnboardingSlide(R.drawable.image2, "Slide 2", "Description 2"));
        OnboardingAdapter adapter = new OnboardingAdapter(slides);

        // Act
        int itemCount = adapter.getItemCount();

        // Assert
        assertEquals(2, itemCount);
    }
}
