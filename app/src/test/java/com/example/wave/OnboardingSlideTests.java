package com.example.wave;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;


public class OnboardingSlideTests {

    // Tests the OnboardingSlide constructor
    @Test
    public void testOnboardingSlideConstructor() {
        // Arrange
        int imageRes = R.drawable.image1;
        String title = "Test Title";
        String description = "Test Description";

        // Act
        OnboardingSlide slide = new OnboardingSlide(imageRes, title, description);

        // Assert
        assertEquals(imageRes, slide.getImageRes());
        assertEquals(title, slide.getTitle());
        assertEquals(description, slide.getDescription());
    }

    // Tests the getImageRes method
    @Test
    public void testGetImageRes() {
        // Arrange
        int imageRes = R.drawable.image1;
        OnboardingSlide slide = new OnboardingSlide(imageRes, "Test Title", "Test Description");

        // Act
        int result = slide.getImageRes();

        // Assert
        assertEquals(imageRes, result);
    }

    // Tests the getTitle method
    @Test
    public void testGetTitle() {
        // Arrange
        String title = "Test Title";
        OnboardingSlide slide = new OnboardingSlide(R.drawable.image1, title, "Test Description");

        // Act
        String result = slide.getTitle();

        // Assert
        assertEquals(title, result);
    }

    // Tests the getTitle method with null
    @Test
    public void testGetTitleWithNull() {
        // Arrange
        String title = null;
        OnboardingSlide slide = new OnboardingSlide(R.drawable.image1, title, "Test Description");

        // Act
        String result = slide.getTitle();

        // Assert
        assertNull(result);
    }


    // Tests the getTitle method with a long string
    @Test
    public void testGetTitleWithLongString() {
        // Arrange
        String title = "A".repeat(10000);
        OnboardingSlide slide = new OnboardingSlide(R.drawable.image1, title, "Test Description");

        // Act
        String result = slide.getTitle();

        // Assert
        assertEquals(title, result);
    }

    // Tests the getTitle method with special characters
    @Test
    public void testGetTitleWithSpecialCharacters() {
        // Arrange
        String title = "!£$%^&**&^%$";
        OnboardingSlide slide = new OnboardingSlide(R.drawable.image1, title, "Test Description");

        // Act
        String result = slide.getTitle();

        // Assert
        assertEquals(title, result);
    }

    // Tests the getTitle method with whitespace only
    @Test
    public void testGetTitleWithWhitespaceOnly() {
        // Arrange
        String title = "   ";
        OnboardingSlide slide = new OnboardingSlide(R.drawable.image1, title, "Test Description");

        // Act
        String result = slide.getTitle();

        // Assert
        assertEquals(title, result);
    }


    // Tests the getTitle method with leading and trailing whitespace
    @Test
    public void testGetTitleWithLeadingTrailingWhitespace() {
        // Arrange
        String title = "  Test Title  ";
        OnboardingSlide slide = new OnboardingSlide(R.drawable.image1, title, "Test Description");

        // Act
        String result = slide.getTitle();

        // Assert
        assertEquals(title, result);
    }

    // Tests the getTitle method with an empty string
    @Test
    public void testGetTitleWithEmptyString() {
        // Arrange
        String title = "";
        OnboardingSlide slide = new OnboardingSlide(R.drawable.image1, title, "Test Description");

        // Act
        String result = slide.getTitle();

        // Assert
        assertEquals(title, result);
    }





    // Tests the getDescription method
    @Test
    public void testGetDescription() {
        // Arrange
        String description = "Test Description";
        OnboardingSlide slide = new OnboardingSlide(R.drawable.image1, "Test Title", description);

        // Act
        String result = slide.getDescription();

        // Assert
        assertEquals(description, result);
    }
}
