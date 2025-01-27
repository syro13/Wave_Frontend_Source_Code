package com.example.wave;

public class Task {
    private String title;
    private String time;
    private String date; // e.g., "18"
    private String month; // e.g., "January"
    private String priority; // e.g., "High", "Medium", "Low"
    private String category; // e.g., "School", "Home"
    private boolean remind; // Whether the user wants to be reminded

    // Constructor
    public Task(String title, String time, String date, String month, String priority, String category, boolean remind) {
        this.title = title;
        this.time = time;
        this.date = date;
        this.month = month;
        this.priority = priority;
        this.category = category;
        this.remind = remind;
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
        return month;
    }

    public String getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public boolean isRemind() {
        return remind;
    }

    // Method to get full date in d/M/yyyy format
    public String getFullDate(int year) {
        int monthIndex = getMonthIndex(month); // Convert month name to index (e.g., "January" -> 1)
        return date + "/" + monthIndex + "/" + year;
    }

    // Helper method to get the month index
    private int getMonthIndex(String monthName) {
        switch (monthName) {
            case "January":
                return 1;
            case "February":
                return 2;
            case "March":
                return 3;
            case "April":
                return 4;
            case "May":
                return 5;
            case "June":
                return 6;
            case "July":
                return 7;
            case "August":
                return 8;
            case "September":
                return 9;
            case "October":
                return 10;
            case "November":
                return 11;
            case "December":
                return 12;
            default:
                throw new IllegalArgumentException("Invalid month: " + monthName);
        }
    }
}
