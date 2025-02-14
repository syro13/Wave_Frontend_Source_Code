package com.example.wave;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class PodcastSearchResponseInstrumentedTest {

    private static final String JSON_RESPONSE =
            "[{\"title_original\":\"Podcast 1\", \"audio_length_sec\":1200, \"link\":\"http://podcast1.com\"}, " +
                    "{\"title_original\":\"Podcast 2\", \"audio_length_sec\":1500, \"link\":\"http://podcast2.com\"}]";

    private static final String INVALID_JSON_RESPONSE = "[{\"title_original\":\"Podcast 1\", \"audio_length_sec\":1200}]";

    private List<Podcast> podcasts;

    @Before
    public void setUp() {
        // Prepare the JSON string and convert to list for testing
        podcasts = PodcastSearchResponse.fromJsonList(JSON_RESPONSE);
    }

    // Testing from JSON list
    @Test
    public void testFromJsonList() {
        // Verify that the JSON string is correctly parsed into a list of Podcast objects
        assertNotNull(podcasts);
        assertEquals(2, podcasts.size());

        Podcast firstPodcast = podcasts.get(0);
        assertEquals("Podcast 1", firstPodcast.getTitle());
        assertEquals(1200, firstPodcast.getLengthMinutes() * 60); // Validate length in seconds
        assertEquals("http://podcast1.com", firstPodcast.getLink());

        Podcast secondPodcast = podcasts.get(1);
        assertEquals("Podcast 2", secondPodcast.getTitle());
        assertEquals(1500, secondPodcast.getLengthMinutes() * 60); // Validate length in seconds
        assertEquals("http://podcast2.com", secondPodcast.getLink());
    }

    // Testing to Json list
    @Test
    public void testToJsonList() {
        // Convert the list back to JSON and verify the output
        String json = PodcastSearchResponse.toJsonList(podcasts);
        assertNotNull(json);
        assertTrue(json.contains("Podcast 1"));
        assertTrue(json.contains("http://podcast1.com"));
        assertTrue(json.contains("Podcast 2"));
    }

    // Test with empty list
    @Test
    public void testToJsonList_emptyList() {
        // Test with an empty list
        List<Podcast> emptyList = List.of();
        String json = PodcastSearchResponse.toJsonList(emptyList);
        assertNotNull(json);
        assertEquals("[]", json);
    }
}
