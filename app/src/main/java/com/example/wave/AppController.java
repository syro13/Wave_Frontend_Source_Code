package com.example.wave;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class AppController extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Force Light Mode for the entire app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
