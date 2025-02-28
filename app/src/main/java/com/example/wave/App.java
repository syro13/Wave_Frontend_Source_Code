package com.example.wave;

import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;

public class App extends Application {
    private static FirebaseFirestore firestoreInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        firestoreInstance = FirebaseFirestore.getInstance();
    }

    public static FirebaseFirestore getFirestore() {
        return firestoreInstance;
    }
}
