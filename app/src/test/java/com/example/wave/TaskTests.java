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
        String title = "Study";
        String time = "10:00 AM";
        String date = "18";
        String month = "January";
        String priority = "High";
        String category = "School";
        boolean remind = true;
        int year = 2025;

        // Act
        Task task = new Task(title, time, date, month, priority, category, remind, year);

        // Assert
        assertEquals(title, task.getTitle());
        assertEquals(time, task.getTime());
        assertEquals(date, task.getDate());
        assertEquals(month, task.getMonth());
        assertEquals(priority, task.getPriority());
        assertEquals(category, task.getCategory());
        assertTrue(task.isRemind());
        assertEquals(year, task.getYear());
    }

    // Tests the getTitle method
    @Test
    public void testGetTitle() {
        // Arrange
        Task task = new Task("Buy Groceries", "12:00 PM", "20", "March", "Medium", "Personal", false, 2024);

        // Act
        String result = task.getTitle();

        // Assert
        assertEquals("Buy Groceries", result);
    }

    // Tests the getTime method
    @Test
    public void testGetTime() {
        // Arrange
        Task task = new Task("Buy Groceries", "12:00 PM", "20", "March", "Medium", "Personal", false, 2024);

        // Act
        String result = task.getTime();

        // Assert
        assertEquals("12:00 PM", result);
    }

    // Tests the getCategory method
    @Test
    public void testGetCategory() {
        // Arrange
        Task task = new Task("Clean Kitchen", "8:00 PM", "15", "April", "Low", "Home", false, 2023);

        // Act
        String result = task.getCategory();

        // Assert
        assertEquals("Home", result);
    }

    // Tests the isRemind method
    @Test
    public void testIsRemind() {
        // Arrange
        Task task = new Task("Submit Report", "5:00 PM", "10", "May", "High", "Work", true, 2025);

        // Act
        boolean result = task.isRemind();

        // Assert
        assertTrue(result);
    }

    // Tests the getFullDate method
    @Test
    public void testGetFullDate() {
        // Arrange
        Task task = new Task("Doctor Appointment", "3:00 PM", "22", "June", "Medium", "Health", false, 2024);

        // Act
        String result = task.getFullDate(2024);

        // Assert
        assertEquals("22/6/2024", result);
    }

    // Tests empty title
    @Test // Possibly a issue should be reviewed
    public void testEmptyTitle() {
        Task task = new Task("", "10:00 AM", "1", "January", "High", "Work", false, 2023);
        assertEquals("", task.getTitle());
    }

    // Tests invalid month name
    @Test // This should Fail
    public void testInvalidMonth() {
        Task task = new Task("Meeting", "2:00 PM", "10", "InvalidMonth", "Medium", "Office", true, 2023);
        assertEquals("10/-1/2023", task.getFullDate(2023)); // Expecting -1 as an invalid index
    }

    // Tests invalid date
    @Test // This should fail
    public void testInvalidDate() {
        Task task = new Task("Event", "4:00 PM", "32", "July", "Low", "Personal", false, 2024);
        assertEquals("32/7/2024", task.getFullDate(2024)); // Date 32 is invalid
    }

    // Tests extra incorrect year
    @Test // This should fail 
    public void testYearBoundary() {
        Task task = new Task("New Year", "12:00 AM", "31", "December", "High", "Holiday", true, 9999);
        assertEquals("31/12/9999", task.getFullDate(9999));
    }
}
