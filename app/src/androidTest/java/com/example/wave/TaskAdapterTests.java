package com.example.wave;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class TaskAdapterTests {

    // Test that the getItemCount method returns the correct number of items
    @Test
    public void testGetItemCount() {
        // Arrange
        List<Task> taskList = new ArrayList<>();
        taskList.add(new Task("Task 1", "10:00 AM", "Work", false, false));
        taskList.add(new Task("Task 2", "11:00 AM", "Personal", false, true));
        TaskAdapter adapter = new TaskAdapter(taskList, null);

        // Act
        int itemCount = adapter.getItemCount();

        // Assert
        assertEquals(2, itemCount);
    }
}
