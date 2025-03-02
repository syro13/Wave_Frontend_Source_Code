package com.example.wave;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class TaskAdapterTests {

    private TaskAdapter adapter;
    private List<Task> taskList;

    @Before
    public void setUp() {
        // Arrange: Create a mock list of tasks
        taskList = new ArrayList<>();
        taskList.add(new Task("Task 1", "10:00 AM", "5", "March", "High", "School", true, 2025, true));
        taskList.add(new Task("Task 2", "11:30 AM", "10", "April", "Medium", "Home", false, 2025 ));

        // Mock the required listeners
        TaskAdapter.OnTaskDeletedListener mockDeleteListener = mock(TaskAdapter.OnTaskDeletedListener.class);
        TaskAdapter.OnTaskEditedListener mockEditListener = mock(TaskAdapter.OnTaskEditedListener.class);

        // Initialize adapter with mock data and listeners
        adapter = new TaskAdapter(taskList, mock(android.content.Context.class), mockDeleteListener, mockEditListener, null, null);
    }

    @Test
    public void testGetItemCount() {
        // Act
        int itemCount = adapter.getItemCount();

        // Assert
        assertEquals(2, itemCount);  // Ensure it matches the number of tasks
    }
}
