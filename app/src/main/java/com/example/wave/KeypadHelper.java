package com.example.wave;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class KeypadHelper {

    private final TextView amountDisplay;
    private final Context context;
    private final StringBuilder currentAmount;
    private static final int MAX_DIGITS = 8;
    private static final int MAX_DECIMALS = 2;

    public KeypadHelper(TextView amountDisplay) {
        this.amountDisplay = amountDisplay;
        this.context = amountDisplay.getContext(); // Needed for Toast
        this.currentAmount = new StringBuilder();
    }
    public void handleKeyPress(String key) {
        if (key.equals("⌫")) {
            if (currentAmount.length() > 0) {
                currentAmount.deleteCharAt(currentAmount.length() - 1);
            }

        } else if (key.equals(".")) {
            if (!currentAmount.toString().contains(".")) {
                if (currentAmount.length() == 0) {
                    currentAmount.append("0.");
                } else {
                    currentAmount.append(".");
                }
            }

        } else { // Numbers
            String[] parts = currentAmount.toString().split("\\.");

            // Check digit count (excluding ".")
            int totalDigits = currentAmount.toString().replace(".", "").length();
            if (totalDigits >= MAX_DIGITS) {
                showToast("Max digits: " + MAX_DIGITS);
                return;
            }

            // Check decimal precision
            if (parts.length == 2 && parts[1].length() >= MAX_DECIMALS) {
                showToast("Only " + MAX_DECIMALS + " decimal places allowed");
                return;
            }

            // Prevent multiple leading zeros
            if (currentAmount.toString().equals("0")) {
                currentAmount.setLength(0); // Remove leading 0
            }

            currentAmount.append(key);
        }

        // Update display
        if (currentAmount.length() == 0) {
            amountDisplay.setText("0");
        } else {
            amountDisplay.setText(currentAmount.toString());
        }
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
