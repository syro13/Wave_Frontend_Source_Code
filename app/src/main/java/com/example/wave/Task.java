package com.example.wave;
public class Task {
    private String title;
    private String time;
    private String date; // e.g., "18"
    private String month; // e.g., "January"
    private String category;
    private boolean isOverdue;
    private boolean isHighPriority;

    // Constructor
    public Task(String title, String time, String date, String month, String category, boolean isOverdue, boolean isHighPriority) {
        this.title = title;
        this.time = time;
        this.date = date;
        this.month = month;
        this.category = category;
        this.isOverdue = isOverdue;
        this.isHighPriority = isHighPriority;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getMonth() {
        return month; // Add this method
    }

    public String getCategory() {
        return category;
    }

    public boolean isOverdue() {
        return isOverdue;
    }

    public boolean isHighPriority() {
        return isHighPriority;
    }
}
