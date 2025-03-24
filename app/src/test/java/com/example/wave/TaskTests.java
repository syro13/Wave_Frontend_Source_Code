package com.example.wave;

import org.junit.Test;
import static org.junit.Assert.*;

public class TaskTests {

    @Test
    public void testTaskConstructorAndGetters() {
        // Arrange
        Task task = new Task("Complete Assignment", "10:00 AM", "18", "January", "High", "School", true, 2024);

        // Assert
        assertEquals("Complete Assignment", task.getTitle());
        assertEquals("10:00 AM", task.getTime());
        assertEquals("18", task.getDate());
        assertEquals("January", task.getMonth());
        assertEquals("High", task.getPriority());
        assertEquals("School", task.getCategory());
        assertTrue(task.isRemind());
        assertEquals(2024, task.getYear());
    }

    @Test
    public void testBooleanRemind() {
        // Arrange
        Task task = new Task("Doctor Appointment", "3:30 PM", "5", "June", "High", "Health", false, 2023);
        Task taskWithReminder = new Task("Doctor Appointment", "3:30 PM", "5", "June", "High", "Health", true, 2023);

        // Assert
        assertFalse(task.isRemind());
        assertTrue(taskWithReminder.isRemind());
    }

    @Test
    public void testGetTitle() {
        // Arrange
        Task task = new Task("Buy Groceries", "12:00 PM", "20", "March", "Medium", "Personal", false, 2024);

        // Act
        String result = task.getTitle();

        // Assert
        assertEquals("Buy Groceries", result);
    }

    @Test
    public void testGetTime() {
        // Arrange
        Task task = new Task("Buy Groceries", "12:00 PM", "20", "March", "Medium", "Personal", false, 2024);

        // Act
        String result = task.getTime();

        // Assert
        assertEquals("12:00 PM", result);
    }

    @Test
    public void testGetCategory() {
        // Arrange
        Task task = new Task("Clean Kitchen", "8:00 PM", "15", "April", "Low", "Home", false, 2023);

        // Act
        String result = task.getCategory();

        // Assert
        assertEquals("Home", result);
    }

    @Test
    public void testIsRemind() {
        // Arrange
        Task task = new Task("Submit Report", "5:00 PM", "10", "May", "High", "Work", true, 2025);

        // Act
        boolean result = task.isRemind();

        // Assert
        assertTrue(result);
    }

    @Test
    public void testEmptyTitle() {
        // Arrange
        Task task = new Task("", "10:00 AM", "1", "January", "High", "Work", false, 2023);

        // Assert
        assertEquals("", task.getTitle());
    }
}
