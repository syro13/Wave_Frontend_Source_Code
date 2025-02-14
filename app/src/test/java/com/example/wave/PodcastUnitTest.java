package com.example.wave;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PodcastUnitTest {

    private Podcast podcast;

    @Before
    public void setUp() {
        podcast = new Podcast();

        // Use reflection to set private fields
        try {
            java.lang.reflect.Field titleField = Podcast.class.getDeclaredField("title");
            titleField.setAccessible(true);
            titleField.set(podcast, "Test Podcast");

            java.lang.reflect.Field lengthSecondsField = Podcast.class.getDeclaredField("lengthSeconds");
            lengthSecondsField.setAccessible(true);
            lengthSecondsField.set(podcast, 3600); // 1 hour in seconds

            java.lang.reflect.Field linkField = Podcast.class.getDeclaredField("link");
            linkField.setAccessible(true);
            linkField.set(podcast, "http://example.com/podcast");
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        }
    }

    // Test the getTitle method of the Podcast class
    @Test
    public void testGetTitle() {
        assertEquals("Test Podcast", podcast.getTitle());
    }

    // Test the getLengthSeconds method of the Podcast class
    @Test
    public void testGetLengthMinutes() {
        assertEquals(60, podcast.getLengthMinutes()); // 3600 seconds should convert to 60 minutes
    }

    // Test the getLink method of the Podcast class
    @Test
    public void testGetLink() {
        assertEquals("http://example.com/podcast", podcast.getLink());
    }
}
