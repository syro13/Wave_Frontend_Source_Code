package com.example.wave;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class BudgetPlannerActivity extends AppCompatActivity {

    private SemiCircularProgressView semiCircularProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_planner);

        // Initialize SemiCircularProgressView
        semiCircularProgressView = findViewById(R.id.budgetSemiCircleChart);

        // Set initial progress
        semiCircularProgressView.setProgress(10);

        // Simulate dynamic progress updates
        new android.os.Handler().postDelayed(() -> semiCircularProgressView.setProgress(55), 2000);
        new android.os.Handler().postDelayed(() -> semiCircularProgressView.setProgress(85), 4000);
    }
}
