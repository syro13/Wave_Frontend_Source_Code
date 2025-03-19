package com.example.wave;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;
public class ReminderManager {

    public static void scheduleReminder(Context context, Task task) {
        Calendar reminderTime = Calendar.getInstance();
        try {
            String fullDateTime = task.getFullDate() + " " + task.getTime();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
            reminderTime.setTime(sdf.parse(fullDateTime));
            Log.d("ReminderManager", "Parsed reminder time: " + reminderTime.getTime());
        } catch (Exception e) {
            Log.e("ReminderManager", "Error parsing date/time for task: " + task.getTitle(), e);
        }

        long triggerTime = reminderTime.getTimeInMillis();
        Log.d("ReminderManager", "Scheduling alarm for: " + triggerTime);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("taskId", task.getId());
        intent.putExtra("taskTitle", task.getTitle());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            // Use setExactAndAllowWhileIdle for Doze mode devices.
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Log.d("ReminderManager", "Alarm scheduled successfully for task: " + task.getTitle());
        } catch (SecurityException e) {
            Log.e("ReminderManager", "SecurityException scheduling alarm for task: " + task.getTitle(), e);
        }
    }


    public static void cancelReminder(Context context, Task task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
}

