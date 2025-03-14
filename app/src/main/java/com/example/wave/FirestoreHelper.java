package com.example.wave;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {

    private static final String USERS_COLLECTION = "users";
    private static final String TASKS_COLLECTION = "housetasks";
    private static final String USER_ID = "Ffq2POCPcXSCSUFQ2Nhi";  // Use dynamic user ID in a real app

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface FirestoreCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static void addTask(String title, String priority, String date, String time, boolean remind, String taskType, FirestoreCallback callback) {
        String taskId = db.collection(USERS_COLLECTION).document(USER_ID)
                .collection(TASKS_COLLECTION).document().getId();

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("id", taskId);
        taskData.put("title", title);
        taskData.put("priority", priority);
        taskData.put("date", date);
        taskData.put("time", time);
        taskData.put("remind", remind);
        taskData.put("category", taskType);

        db.collection(USERS_COLLECTION)
                .document(USER_ID)
                .collection(TASKS_COLLECTION)
                .document(taskId)
                .set(taskData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreHelper", "Task added successfully: " + taskId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreHelper", "Error adding task", e);
                    callback.onFailure(e);
                });
    }
}
