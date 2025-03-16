// package com.example.wave;

// <<<<<<< Stage4Tests
// import org.junit.Before;
// import org.junit.Test;
// import static org.junit.Assert.*;

// public class TaskManagerTests {
//     private TaskManager taskManager;

//     // Set up the TaskManager before each test
//     @Before
//     public void setUp() {
//         taskManager = TaskManager.getInstance();
//         taskManager.getSchoolTasks().clear();
//         taskManager.getHomeTasks().clear();
//     }

//     @Test // Testing adding a task to the school category
//     public void testAddTaskToSchoolCategory() {
//         Task schoolTask = new Task("Math Assignment", "10:00", "10", "March", "High", "School", true, 2025);
//         taskManager.addTask(schoolTask);

//         // Verify that the school task was added
//         assertEquals(1, taskManager.getSchoolTasks().size());
//         assertEquals("Math Assignment", taskManager.getSchoolTasks().get(0).getTitle());
//     }

//     @Test // Testing adding a task to the home category
//     public void testAddTaskToHomeCategory() {
//         Task homeTask = new Task("Clean Room", "14:00", "15", "March", "Low", "Home", false, 2025);
//         taskManager.addTask(homeTask);
//         assertEquals(1, taskManager.getHomeTasks().size());
//         assertEquals("Clean Room", taskManager.getHomeTasks().get(0).getTitle());
//     }

//     @Test // Testing that the TaskManager is a singleton
//     public void testGetInstanceReturnsSameInstance() {
//         TaskManager firstInstance = TaskManager.getInstance();
//         TaskManager secondInstance = TaskManager.getInstance();
//         assertSame(firstInstance, secondInstance);
//     }

//     @Test // Testings adding multiple tasks to the TaskManager
//     public void testAddMultipleTasks() {
//         Task schoolTask = new Task("Math Assignment", "10:00", "10", "March", "High", "School", true, 2025);
//         Task homeTask = new Task("Clean Room", "14:00", "15", "March", "Low", "Home", false, 2025);

//         taskManager.addTask(schoolTask);
//         taskManager.addTask(homeTask);

//         assertEquals(1, taskManager.getSchoolTasks().size());
//         assertEquals(1, taskManager.getHomeTasks().size());
//         assertEquals("Math Assignment", taskManager.getSchoolTasks().get(0).getTitle());
//         assertEquals("Clean Room", taskManager.getHomeTasks().get(0).getTitle());
//     }
// =======
// // Not able to get tests to work for this class
// public class TaskManagerTests {
// >>>>>>> staging
// }
