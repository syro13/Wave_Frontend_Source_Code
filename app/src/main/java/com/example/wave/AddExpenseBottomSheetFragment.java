package com.example.wave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class AddExpenseBottomSheetFragment extends BottomSheetDialogFragment {

    private KeypadHelper keypadHelper;
    private TextView amountDisplay;
    private AutoCompleteTextView expenseCategoryDropdown;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        // Initialize TextView for amount display
        amountDisplay = view.findViewById(R.id.amountValue);

        // Initialize KeypadHelper
        keypadHelper = new KeypadHelper(amountDisplay);

        // Set up keypad button listeners
        setupKeypad(view);

        Button submitButton = view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> {
            String amount = amountDisplay.getText().toString();

            // Send data to parent using FragmentManager
            Bundle result = new Bundle();
            result.putString("expense_amount", amount);
            getParentFragmentManager().setFragmentResult("expense_request", result);

            dismiss(); // Close the bottom sheet
        });

        // Initialize dropdown
        expenseCategoryDropdown = view.findViewById(R.id.expenseCategoryDropdown);
        setupExpenseCategoryDropdown();


        return view;

    }
    @Override
    public int getTheme() {
        // Return the custom theme with rounded corners and transparent background
        return R.style.RoundedBottomSheetDialogTheme;
    }
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            View bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Automatically expand to wrap content
                behavior.setSkipCollapsed(true);
            }
        }
    }
    private void setupKeypad(View view) {
        int[] buttonIds = {
                R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4,
                R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9,
                R.id.buttonDot, R.id.buttonBackspace
        };

        for (int buttonId : buttonIds) {
            Button button = view.findViewById(buttonId);
            button.setOnClickListener(v -> {
                String key = button.getText().toString();
                keypadHelper.handleKeyPress(key);
            });
        }
    }

    private void setupExpenseCategoryDropdown() {
        List<ExpenseCategory> categories = new ArrayList<>();
        categories.add(new ExpenseCategory("Shopping", R.drawable.ic_shopping));
        categories.add(new ExpenseCategory("Food", R.drawable.ic_food));
        categories.add(new ExpenseCategory("Groceries", R.drawable.ic_groceries));
        categories.add(new ExpenseCategory("Bills", R.drawable.ic_bills));
        categories.add(new ExpenseCategory("Travel", R.drawable.ic_travel));
        categories.add(new ExpenseCategory("Health", R.drawable.ic_health));
        categories.add(new ExpenseCategory("Miscellaneous", R.drawable.ic_miscellaneous));

        ExpenseCategoryAdapter adapter = new ExpenseCategoryAdapter(requireContext(), categories);
        expenseCategoryDropdown.setAdapter(adapter);
    }
}
