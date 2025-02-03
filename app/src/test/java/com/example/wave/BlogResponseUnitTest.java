package com.example.wave;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class BlogResponseUnitTest {

    // Test the BlogResponse class getters
    @Test
    public void testGetters() {
        BlogResponse blog = new BlogResponse(
                "Test Title",
                "Test Author",
                "Test Tag",
                "https://example.com",
                "https://example.com/image.jpg"
        );

        // Assert getter methods
        assertEquals("Test Title", blog.getTitle());
        assertEquals("Test Author", blog.getAuthor());
        assertEquals("Test Tag", blog.getTag());
        assertEquals("https://example.com", blog.getLink());
        assertEquals("https://example.com/image.jpg", blog.getImageUrl());
    }

    // Test the toJsonList and fromJsonList methods
    @Test
    public void testToJsonList() {
        // Create a list of BlogResponse objects
        List<BlogResponse> blogs = new ArrayList<>();
        blogs.add(new BlogResponse("Title1", "Author1", "Tag1", "https://example.com/1", "https://example.com/img1.jpg"));
        blogs.add(new BlogResponse("Title2", "Author2", "Tag2", "https://example.com/2", "https://example.com/img2.jpg"));

        // Convert the list to JSON
        String json = BlogResponse.toJsonList(blogs);
        String expectedJson = "[{\"title\":\"Title1\",\"author\":\"Author1\",\"tag\":\"Tag1\",\"link\":\"https://example.com/1\",\"imageUrl\":\"https://example.com/img1.jpg\"}," +
                "{\"title\":\"Title2\",\"author\":\"Author2\",\"tag\":\"Tag2\",\"link\":\"https://example.com/2\",\"imageUrl\":\"https://example.com/img2.jpg\"}]";

        // Assert the JSON output
        assertEquals(expectedJson, json);
    }

    // Test the fromJsonList method
    @Test
    public void testFromJsonList() {
        String json = "[{\"title\":\"Title1\",\"author\":\"Author1\",\"tag\":\"Tag1\",\"link\":\"https://example.com/1\",\"imageUrl\":\"https://example.com/img1.jpg\"}," +
                "{\"title\":\"Title2\",\"author\":\"Author2\",\"tag\":\"Tag2\",\"link\":\"https://example.com/2\",\"imageUrl\":\"https://example.com/img2.jpg\"}]";

        // Convert JSON to List<BlogResponse>
        List<BlogResponse> blogs = BlogResponse.fromJsonList(json);

        // Assert the list size
        assertEquals(2, blogs.size());

        // Assert individual elements
        assertEquals("Title1", blogs.get(0).getTitle());
        assertEquals("Author1", blogs.get(0).getAuthor());
        assertEquals("Tag1", blogs.get(0).getTag());
        assertEquals("https://example.com/1", blogs.get(0).getLink());
        assertEquals("https://example.com/img1.jpg", blogs.get(0).getImageUrl());

        assertEquals("Title2", blogs.get(1).getTitle());
        assertEquals("Author2", blogs.get(1).getAuthor());
        assertEquals("Tag2", blogs.get(1).getTag());
        assertEquals("https://example.com/2", blogs.get(1).getLink());
        assertEquals("https://example.com/img2.jpg", blogs.get(1).getImageUrl());
    }
}
