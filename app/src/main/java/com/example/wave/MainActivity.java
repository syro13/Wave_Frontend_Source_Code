package com.example.wave;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TO-DO add this line to intro screen
        //setContentView(R.layout.activity_onboarding);
        setContentView(android.R.layout.simple_list_item_1); // Temporary default layout
    }
}