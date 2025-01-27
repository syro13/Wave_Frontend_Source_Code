package com.example.wave;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

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
        int monthIndex = getMonthIndex(this.month);
        Log.d("getFullDate", "Month: " + this.month + ", Month Index: " + monthIndex);
        return date + "/" + (monthIndex + 1) + "/" + year;
    }



    // Helper method to get the month index
    private int getMonthIndex(String monthName) {
        List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
        return months.indexOf(monthName);
    }

}
