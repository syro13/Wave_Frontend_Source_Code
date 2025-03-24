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
        Task task = new Task("Doctor Appointment", "3:30 PM", "5", "June", "High", "Health", false, 2023);
        Task taskWithReminder = new Task("Doctor Appointment", "3:30 PM", "5", "June", "High", "Health", true, 2023);
        assertFalse(task.isRemind());
        assertTrue(taskWithReminder.isRemind());
    }

    @Test
    public void testGetTitle() {
        Task task = new Task("Buy Groceries", "12:00 PM", "20", "March", "Medium", "Personal", false, 2024);
        String result = task.getTitle();
        assertEquals("Buy Groceries", result);
    }

    @Test
    public void testGetTime() {
        Task task = new Task("Buy Groceries", "12:00 PM", "20", "March", "Medium", "Personal", false, 2024);
        String result = task.getTime();
        assertEquals("12:00 PM", result);
    }

    @Test
    public void testGetCategory() {
        Task task = new Task("Clean Kitchen", "8:00 PM", "15", "April", "Low", "Home", false, 2023);
        String result = task.getCategory();
        assertEquals("Home", result);
    }

    @Test
    public void testIsRemind() {
        Task task = new Task("Submit Report", "5:00 PM", "10", "May", "High", "Work", true, 2025);
        boolean result = task.isRemind();
        assertTrue(result);
    }

    @Test
    public void testEmptyTitle() {
        Task task = new Task("", "10:00 AM", "1", "January", "High", "Work", false, 2023);
        assertEquals("", task.getTitle());
    }
}
