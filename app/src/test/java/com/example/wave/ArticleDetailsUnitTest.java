package com.example.wave;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ArticleDetailsUnitTest {

    // Test the ArticleDetails class constructor
    @Test
    public void testGetId() {
        // Create an ArticleDetails object with mock data
        ArticleDetails article = createMockArticle();

        // Assert the ID getter
        assertEquals("001", article.getId());
    }

    // Test the getTitle method of the ArticleDetails class
    @Test
    public void testGetTitle() {
        // Create an ArticleDetails object with mock data
        ArticleDetails article = createMockArticle();

        // Assert the Title getter
        assertEquals("Test Article", article.getTitle());
    }

    // Test the getAuthor method of the ArticleDetails class
    @Test
    public void testGetAuthor() {
        // Create an ArticleDetails object with mock data
        ArticleDetails article = createMockArticle();

        // Assert the Author getter
        assertEquals("John Doe", article.getAuthor());
    }

    // Test the getUrl method of the ArticleDetails class
    @Test
    public void testGetUrl() {
        // Create an ArticleDetails object with mock data
        ArticleDetails article = createMockArticle();

        // Assert the URL getter
        assertEquals("https://example.com/article", article.getUrl());
    }

    // Test the getImageUrl method of the ArticleDetails class
    @Test
    public void testGetImageUrl() {
        // Create an ArticleDetails object with mock data
        ArticleDetails article = createMockArticle();

        // Assert the Image URL getter
        assertEquals("https://example.com/image.jpg", article.getImageUrl());
    }

    // Helper method to create a mock ArticleDetails object
    private ArticleDetails createMockArticle() {
        ArticleDetails article = new ArticleDetails();
        try {
            // Get the fields of the ArticleDetails class and st them as accessible
            java.lang.reflect.Field idField = ArticleDetails.class.getDeclaredField("id");
            idField.setAccessible(true);

            java.lang.reflect.Field titleField = ArticleDetails.class.getDeclaredField("title");
            titleField.setAccessible(true);

            java.lang.reflect.Field authorField = ArticleDetails.class.getDeclaredField("author");
            authorField.setAccessible(true);

            java.lang.reflect.Field urlField = ArticleDetails.class.getDeclaredField("url");
            urlField.setAccessible(true);

            java.lang.reflect.Field imageUrlField = ArticleDetails.class.getDeclaredField("image_url");
            imageUrlField.setAccessible(true);

            // Set the values of the fields
            idField.set(article, "001");
            titleField.set(article, "Test Article");
            authorField.set(article, "John Doe");
            urlField.set(article, "https://example.com/article");
            imageUrlField.set(article, "https://example.com/image.jpg");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set mock data", e);
        }
        // Return the mock ArticleDetails object
        return article;
    }
}
