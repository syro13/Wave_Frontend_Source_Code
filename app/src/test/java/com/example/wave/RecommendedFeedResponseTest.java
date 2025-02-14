package com.example.wave;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

public class RecommendedFeedResponseTest {

    // Tests the getters of the RecommendedFeedResponse class
    @Test
    public void testGetters() {
        // Create a RecommendedFeedResponse object
        RecommendedFeedResponse response = new RecommendedFeedResponse();

        // Use reflection to set private fields
        List<String> mockFeed = Arrays.asList("Feed1", "Feed2", "Feed3");
        int mockCount = 3;
        String mockTag = "TestTag";
        int mockPage = 1;

        // Setting values using reflection
        try {
            java.lang.reflect.Field recommendedFeedField = RecommendedFeedResponse.class.getDeclaredField("recommended_feed");
            recommendedFeedField.setAccessible(true);
            recommendedFeedField.set(response, mockFeed);

            java.lang.reflect.Field countField = RecommendedFeedResponse.class.getDeclaredField("count");
            countField.setAccessible(true);
            countField.set(response, mockCount);

            java.lang.reflect.Field tagField = RecommendedFeedResponse.class.getDeclaredField("tag");
            tagField.setAccessible(true);
            tagField.set(response, mockTag);

            java.lang.reflect.Field pageField = RecommendedFeedResponse.class.getDeclaredField("page");
            pageField.setAccessible(true);
            pageField.set(response, mockPage);
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        }

        // Validate getters
        assertEquals(mockFeed, response.getRecommendedFeed());
        assertEquals(mockCount, response.getCount());
        assertEquals(mockTag, response.getTag());
        assertEquals(mockPage, response.getPage());
    }
}
