package com.example.wave;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import com.google.firebase.Timestamp;


public class Task implements Parcelable {
    private String id;  // Unique identifier for tasks
    private String title;
    private String time;
    private String date;
    private String month;
    private String priority;
    private String category;
    private boolean remind;
    private int year;
    private String fullDate;
    private int stability;
    private long tasktimestamp;
    private boolean completed;
    private RepeatOption repeatOption;
    public enum RepeatOption {
        DOES_NOT_REPEAT,
        REPEAT_EVERY_MONDAY,
        REPEAT_EVERY_TUESDAY,
        REPEAT_EVERY_WEDNESDAY,
        REPEAT_EVERY_THURSDAY,
        REPEAT_EVERY_FRIDAY,
        REPEAT_EVERY_SATURDAY,
        REPEAT_EVERY_SUNDAY
    }
    // Default constructor (needed for Firebase or other serialization)
    public Task() {}

    // Full constructor
    public Task(String id, String title, String time, String date, String month,
                String priority, String category, boolean remind, int year,
                int stability, long tasktimestamp, String fullDate, boolean completed,  RepeatOption repeatOption) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.date = date;
        this.month = month;
        this.priority = priority;
        this.category = category;
        this.remind = remind;
        this.year = year;
        this.stability = stability;
        this.tasktimestamp = tasktimestamp;
        this.fullDate = fullDate;
        this.completed = completed;
        this.repeatOption = repeatOption;

    }

    // --- PARCELABLE IMPLEMENTATION ---
    protected Task(Parcel in) {
        id = in.readString();
        title = in.readString();
        time = in.readString();
        date = in.readString();
        month = in.readString();
        priority = in.readString();
        category = in.readString();
        remind = in.readByte() != 0;
        year = in.readInt();
        fullDate = in.readString();
        stability = in.readInt();
        tasktimestamp = in.readLong();
        completed = in.readByte() != 0;
        repeatOption = RepeatOption.values()[in.readInt()];
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(time);
        dest.writeString(date);
        dest.writeString(month);
        dest.writeString(priority);
        dest.writeString(category);
        dest.writeByte((byte) (remind ? 1 : 0));
        dest.writeInt(year);
        dest.writeString(fullDate);
        dest.writeInt(stability);
        dest.writeLong(tasktimestamp);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeInt(repeatOption.ordinal());
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

    // --- GETTERS & SETTERS ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isRemind() { return remind; }
    public void setRemind(boolean remind) { this.remind = remind; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getStability() { return stability; }
    public void setStability(int stability) { this.stability = stability; }

    public long getTasktimestamp() {
        return tasktimestamp;
    }

    public void setTasktimestamp(Object timestamp) {
        if (timestamp instanceof Timestamp) {
            this.tasktimestamp = ((Timestamp) timestamp).toDate().getTime(); // Convert Firestore Timestamp to long
        } else if (timestamp instanceof Long) {
            this.tasktimestamp = (long) timestamp;
        } else {
            this.tasktimestamp = System.currentTimeMillis(); // Default to current time if unknown type
        }
    }
    public boolean isCompleted() {
        return completed;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getFullDate() {
        if (date == null || month == null) {
            Log.e("getFullDate", "Date or month is null, returning empty string.");
            return "";
        }
        int monthIndex = getMonthIndex(this.month);
        Log.d("getFullDate", "Month: " + this.month + ", Month Index: " + monthIndex);
        return date + "/" + (monthIndex + 1) + "/" + year;
    }

    public void setFullDate(String fullDate) { this.fullDate = fullDate; }

    public RepeatOption getRepeatOption() {
        return repeatOption;
    }

    public void setRepeatOption(RepeatOption repeatOption) {
        this.repeatOption = repeatOption;
    }
    // Helper method to get the month index
    private int getMonthIndex(String monthName) {
        List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
        return months.indexOf(monthName);
    }

    // --- equals() & hashCode() FIXED ---
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return remind == task.remind &&
                year == task.year &&
                stability == task.stability &&
                tasktimestamp == task.tasktimestamp &&
                Objects.equals(title, task.title) &&
                Objects.equals(time, task.time) &&
                Objects.equals(date, task.date) &&
                Objects.equals(month, task.month) &&
                Objects.equals(priority, task.priority) &&
                Objects.equals(category, task.category) &&
                Objects.equals(fullDate, task.fullDate) &&
                repeatOption == task.repeatOption;

    }

    @Override
    public int hashCode() {
        return Objects.hash(title, time, date, month, priority, category, remind, year, stability, tasktimestamp, fullDate, repeatOption);
    }
}
