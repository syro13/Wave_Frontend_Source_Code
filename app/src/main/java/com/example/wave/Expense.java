package com.example.wave;

import com.google.firebase.firestore.Exclude;

public class Expense {
    private String categoryName;
    private double amountSpent;

    @Exclude
    private double percentageSpent;

    public Expense() { } // Required for Firestore

    public Expense(String categoryName, double amountSpent) {
        this.categoryName = categoryName;
        this.amountSpent = amountSpent;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getAmountSpent() {
        return amountSpent;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setAmountSpent(double amountSpent) {
        this.amountSpent = amountSpent;
    }

    // Exclude percentage from Firestore, calculate dynamically
    @Exclude
    public double getPercentageSpent(double totalBudget) {
        return (totalBudget > 0) ? (amountSpent / totalBudget) * 100 : 0;
    }
}
