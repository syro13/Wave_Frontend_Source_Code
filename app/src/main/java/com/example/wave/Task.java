package com.example.wave;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class Task implements Parcelable {
    private String title;
    private String time;
    private String date; // e.g., "18"
    private String month; // e.g., "January"
    private String priority; // e.g., "High", "Medium", "Low"
    private String category; // e.g., "School", "Home"
    private boolean remind; // Whether the user wants to be reminded
    private int year;

    // Default constructor (needed for Firebase or other serialization)
    public Task() {
    }

    // Full constructor
    public Task(String title, String time, String date, String month, String priority, String category, boolean remind, int year) {
        this.title = title;
        this.time = time;
        this.date = date;
        this.month = month;
        this.priority = priority;
        this.category = category;
        this.remind = remind;
        this.year = year;
    }

    // Parcelable Constructor
    protected Task(Parcel in) {
        title = in.readString();
        time = in.readString();
        date = in.readString();
        month = in.readString();
        priority = in.readString();
        category = in.readString();
        remind = in.readByte() != 0;
        year = in.readInt();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(time);
        dest.writeString(date);
        dest.writeString(month);
        dest.writeString(priority);
        dest.writeString(category);
        dest.writeByte((byte) (remind ? 1 : 0));
        dest.writeInt(year);
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

    public int getYear() {
        return year;
    }

    // Setters (for updating existing tasks)
    public void setTitle(String title) {
        this.title = title;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setRemind(boolean remind) {
        this.remind = remind;
    }

    public void setYear(int year) {
        this.year = year;
    }

    // Get full date in d/M/yyyy format
    public String getFullDate() {
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

    // Equals method for easier task comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return title.equals(task.title) &&
                time.equals(task.time) &&
                date.equals(task.date) &&
                month.equals(task.month) &&
                priority.equals(task.priority) &&
                category.equals(task.category) &&
                remind == task.remind &&
                year == task.year;
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }
}
