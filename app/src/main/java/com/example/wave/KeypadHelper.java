package com.example.wave;

import android.view.View;
import android.widget.TextView;

public class KeypadHelper {

    private final TextView amountDisplay;
    private final StringBuilder currentAmount;

    public KeypadHelper(TextView amountDisplay) {
        this.amountDisplay = amountDisplay;
        this.currentAmount = new StringBuilder();
    }

    public void handleKeyPress(String key) {
        if (key.equals("⌫")) { // Backspace
            if (currentAmount.length() > 0) {
                currentAmount.deleteCharAt(currentAmount.length() - 1);
            }
        } else if (key.equals(".")) { // Decimal point
            if (!currentAmount.toString().contains(".")) {
                if (currentAmount.length() == 0) {
                    currentAmount.append("0."); // Start with "0." if empty
                } else {
                    currentAmount.append(".");
                }
            }
        } else { // Numbers
            if (currentAmount.toString().equals("0")) {
                currentAmount.setLength(0); // Remove leading zero
            }
            currentAmount.append(key);
        }

        if (currentAmount.length() == 0) {
            amountDisplay.setText("0");
        } else {
            amountDisplay.setText(currentAmount.toString());
        }
    }
}
