package com.example.wave;

import android.app.Application;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class App extends Application {
    private static FirebaseFirestore firestoreInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        firestoreInstance = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestoreInstance.setFirestoreSettings(settings);
    }

    public static FirebaseFirestore getFirestore() {
        return firestoreInstance;
    }
}
