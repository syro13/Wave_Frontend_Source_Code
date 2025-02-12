package com.example.wave;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class TaskTests {

    // Tests the Task constructor and getters
    @Test
    public void testTaskConstructorAndGetters() {
        // Arrange
        String title = "Complete Assignment";
        String time = "10:00 AM";
        String category = "School";
        boolean isOverdue = false;
        boolean isHighPriority = true;

        // Act
        Task task = new Task(title, time, category, isOverdue, isHighPriority);

        // Assert
        assertEquals(title, task.getTitle());
        assertEquals(time, task.getTime());
        assertEquals(category, task.getCategory());
        assertFalse(task.isOverdue());
        assertTrue(task.isHighPriority());
    }

    // Tests the getTitle method
    @Test
    public void testGetTitle() {
        // Arrange
        String title = "Buy Groceries";
        Task task = new Task(title, "12:00 PM", "Personal", false, false);

        // Act
        String result = task.getTitle();

        // Assert
        assertEquals(title, result);
    }

    // Tests the getTime method
    @Test
    public void testGetTime() {
        // Arrange
        String time = "12:00 PM";
        Task task = new Task("Buy Groceries", time, "Personal", false, false);

        // Act
        String result = task.getTime();

        // Assert
        assertEquals(time, result);
    }

    // Tests the getCategory method
    @Test
    public void testGetCategory() {
        // Arrange
        String category = "Home";
        Task task = new Task("Clean Kitchen", "8:00 PM", category, false, false);

        // Act
        String result = task.getCategory();

        // Assert
        assertEquals(category, result);
    }

    // Tests the isOverdue method
    @Test
    public void testIsOverdue() {
        // Arrange
        boolean isOverdue = true;
        Task task = new Task("Submit Report", "5:00 PM", "Work", isOverdue, false);

        // Act
        boolean result = task.isOverdue();

        // Assert
        assertTrue(result);
    }

    // Tests the isHighPriority method
    @Test
    public void testIsHighPriority() {
        // Arrange
        boolean isHighPriority = true;
        Task task = new Task("Pay Bills", "3:00 PM", "Finance", false, isHighPriority);

        // Act
        boolean result = task.isHighPriority();

        // Assert
        assertTrue(result);
    }
}
