package com.example.wave;

import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private static TaskManager instance;
    private List<Task> schoolTasks = new ArrayList<>();
    private List<Task> homeTasks = new ArrayList<>();

    private TaskManager() {}

    public static TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    public List<Task> getSchoolTasks() {
        return schoolTasks;
    }

    public List<Task> getHomeTasks() {
        return homeTasks;
    }

    public void addTask(Task task) {
        if (task.getCategory().equalsIgnoreCase("School")) {
            schoolTasks.add(task);
        } else {
            homeTasks.add(task);
        }
    }
}

