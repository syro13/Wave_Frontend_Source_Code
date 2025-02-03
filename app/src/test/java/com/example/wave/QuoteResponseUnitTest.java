package com.example.wave;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class QuoteResponseUnitTest {

    private QuoteResponse quoteResponse;

    @Before
    public void setUp() {
        quoteResponse = new QuoteResponse();

        // Use reflection to set private fields
        try {
            java.lang.reflect.Field textField = QuoteResponse.class.getDeclaredField("text");
            textField.setAccessible(true);
            textField.set(quoteResponse, "Some Random quote.");

            java.lang.reflect.Field authorField = QuoteResponse.class.getDeclaredField("author");
            authorField.setAccessible(true);
            authorField.set(quoteResponse, "Test Author");
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        }
    }

    // Test the getText method of the QuoteResponse class
    @Test
    public void testGetText() {
        assertEquals("Some Random quote.", quoteResponse.getText());
    }

    // Test the getAuthor method of the QuoteResponse class
    @Test
    public void testGetAuthor() {
        assertEquals("Test Author", quoteResponse.getAuthor());
    }
}
