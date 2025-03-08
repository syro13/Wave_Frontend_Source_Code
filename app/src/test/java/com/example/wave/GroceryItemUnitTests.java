package com.example.wave;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GroceryItemUnitTests {

    private GroceryItem groceryItem;

    @Before
    public void setUp() {
        groceryItem = new GroceryItem("Apple", true);
    }

    @Test // Tests the constructor of the GroceryItem class
    public void testConstructor() {
        assertNotNull(groceryItem);
        assertEquals("Apple", groceryItem.text);
        assertTrue(groceryItem.checked);
    }

    @Test // Test if the text can be updated
    public void testTextChange() {
        groceryItem.text = "Banana";
        assertEquals("Banana", groceryItem.text);
    }

    @Test // Test if the checked state can be updated
    public void testCheckedChange() {
        groceryItem.checked = false;
        assertFalse(groceryItem.checked);
    }
}
