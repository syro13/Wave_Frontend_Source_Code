package com.example.wave;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class WellnessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the wellness layout
        setContentView(R.layout.activity_wellness);

        // Add a simple log message to verify the activity is running
        System.out.println("WellnessActivity is running!");
    }
}
