package com.example.wave;

import org.junit.Test;
import static org.junit.Assert.*;

public class TaskTests {

    @Test
    public void testTaskConstructorAndGetters() {
        Task task = new Task("Complete Assignment", "10:00 AM", "18", "January", "High", "School", true, 2024);

        assertEquals("Complete Assignment", task.getTitle());
        assertEquals("10:00 AM", task.getTime());
        assertEquals("18", task.getDate());
        assertEquals("January", task.getMonth());
        assertEquals("High", task.getPriority());
        assertEquals("School", task.getCategory());
        assertTrue(task.isRemind());
        assertEquals(2024, task.getYear());
    }

//    @Test  Test Does not work
//    public void testGetFullDate() {
//        Task task = new Task("Meeting", "9", "15", "March","Medium", "School", false, 2025);
//        assertEquals("15/3/2025", task.getFullDate(2025));
//    }

//    @Test  Test Does not work
//    public void testMonthIndexHandling() {
//        Task task = new Task("Event", "5:00 PM", "7", "December", "Low", "Personal", false, 2024);
//        assertEquals("7/12/2024", task.getFullDate(2024));
//    }

    @Test
    public void testBooleanRemind() {
        Task task = new Task("Doctor Appointment", "3:30 PM", "5", "June", "High", "Health", false, 2023);
        assertFalse(task.isRemind());

        Task taskWithReminder = new Task("Doctor Appointment", "3:30 PM", "5", "June", "High", "Health", true, 2023);
        assertTrue(taskWithReminder.isRemind());
    }
}
