package com.example.wave;

import org.junit.Test;

public class GroceryItemTests {

    @Test
    public void testGroceryItem() {
        GroceryItem item = new GroceryItem("Milk", false);
        assert item.text.equals("Milk");
        assert !item.checked;
    }

    @Test
    public void testGroceryItemChecked() {
        GroceryItem item = new GroceryItem("Milk", true);
        assert item.text.equals("Milk");
        assert item.checked;
    }

    @Test // This test should not pass
    public void testGroceryEmptyItemText() {
        GroceryItem item = new GroceryItem("", false);
        assert item.text.equals("");
        assert !item.checked;
    }

    @Test // This test should not pass
    public void testGrocerySpecialCharacterItemText() {
        GroceryItem item = new GroceryItem("!£$%^&*", false);
        assert item.text.equals("!£$%^&*");
        assert !item.checked;
    }

    @Test // This test should not pass
    public void testGroceryLeadingAndTrailingWhiteSpaceItemText() {
        GroceryItem item = new GroceryItem("   Milk   ", false);
        assert item.text.equals("   Milk   ");
        assert !item.checked;
    }
}
