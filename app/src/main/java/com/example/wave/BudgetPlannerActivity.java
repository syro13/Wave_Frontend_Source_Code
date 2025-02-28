package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BudgetPlannerActivity extends BaseActivity {

    private SemiCircularProgressView semiCircularProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_planner);
        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation(bottomNavigationView);

        FragmentManager fragmentManager = getSupportFragmentManager();

        // Initialize SemiCircularProgressView
        semiCircularProgressView = findViewById(R.id.budgetSemiCircleChart);

        // Set initial progress
        semiCircularProgressView.setProgress(10);

        // Simulate dynamic progress updates
        new android.os.Handler().postDelayed(() -> semiCircularProgressView.setProgress(55), 2000);
        new android.os.Handler().postDelayed(() -> semiCircularProgressView.setProgress(85), 4000);

        // Add Expense Button
        FloatingActionButton addExpenseButton = findViewById(R.id.addExpenseButton);
        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Add Expense Bottom Sheet
                AddExpenseBottomSheetFragment addExpenseFragment = new AddExpenseBottomSheetFragment();
                addExpenseFragment.show(getSupportFragmentManager(), "AddExpenseBottomSheetFragment");
            }
        });

        // Edit Budget Button
        Button editBudgetButton = findViewById(R.id.editBudgetButton);
        editBudgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Set Budget Bottom Sheet
                SetBudgetBottomSheetFragment setBudgetFragment = new SetBudgetBottomSheetFragment();
                setBudgetFragment.show(getSupportFragmentManager(), "SetBudgetBottomSheetFragment");
            }
        });

        // Handle result from AddExpenseBottomSheetFragment
        fragmentManager.setFragmentResultListener("expense_request", this, (requestKey, result) -> {
            String amount = result.getString("expense_amount");
            Toast.makeText(this, "Expense Added: €" + amount, Toast.LENGTH_SHORT).show();
        });

        // Handle result from SetBudgetBottomSheetFragment
        fragmentManager.setFragmentResultListener("budget_request", this, (requestKey, result) -> {
            String amount = result.getString("budget_amount");
            Toast.makeText(this, "Budget Set: €" + amount, Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.profileIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Budget Activity
                Intent intent = new Intent(BudgetPlannerActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_index; // The menu item ID for the Home tab
    }
}
