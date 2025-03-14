package com.example.wave;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddExpenseBottomSheetFragment extends BottomSheetDialogFragment {

    private KeypadHelper keypadHelper;
    private TextView amountDisplay;
    private TextView expenseCategoryDropdown;

    private final List<ExpenseCategory> categories = Arrays.asList(
            new ExpenseCategory("Shopping", R.drawable.ic_shopping),
            new ExpenseCategory("Food", R.drawable.ic_food),
            new ExpenseCategory("Groceries", R.drawable.ic_groceries),
            new ExpenseCategory("Bills", R.drawable.ic_bills),
            new ExpenseCategory("Travel", R.drawable.ic_travel),
            new ExpenseCategory("Health", R.drawable.ic_health),
            new ExpenseCategory("Miscellaneous", R.drawable.ic_miscellaneous)
    );



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        // Initialize TextView for amount display
        amountDisplay = view.findViewById(R.id.amountValue);

        expenseCategoryDropdown = view.findViewById(R.id.expenseCategoryDropdown);


        // Initialize KeypadHelper
        keypadHelper = new KeypadHelper(amountDisplay);

        // Set up keypad button listeners
        setupKeypad(view);

        // Set up category dropdown (Fix: Pass 'view')
        setupExpenseCategoryDropdown(view);

        Button submitButton = view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> handleSubmit());


        return view;

    }
    @Override
    public int getTheme() {
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

    private void setupExpenseCategoryDropdown(View view) {
        ImageView dropdownArrow = view.findViewById(R.id.dropdownArrow);
        View dropdownContainer = view.findViewById(R.id.dropdownContainer);

        // Set Default Expense Icon (25dp)
        int iconSize = dpToPx(25); // Convert 25dp to pixels
        Drawable defaultIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_expense);
        if (defaultIcon != null) {
            defaultIcon.setBounds(0, 0, iconSize, iconSize);
            expenseCategoryDropdown.setCompoundDrawables(defaultIcon, null, null, null);
        }

        dropdownContainer.setOnClickListener(v -> showDropdownPopup(expenseCategoryDropdown, dropdownArrow,dropdownContainer, categories));
        dropdownArrow.setOnClickListener(v -> showDropdownPopup(expenseCategoryDropdown, dropdownArrow,dropdownContainer, categories));
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                Resources.getSystem().getDisplayMetrics()
        );
    }
    private void showDropdownPopup(TextView expenseCategoryDropdown, ImageView dropdownArrow,View dropdownContainer, List<ExpenseCategory> categories) {
        View popupView = LayoutInflater.from(expenseCategoryDropdown.getContext()).inflate(R.layout.popup_expense_category, null);
        RecyclerView recyclerView = popupView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(expenseCategoryDropdown.getContext()));

        PopupWindow popupWindow = new PopupWindow(popupView, expenseCategoryDropdown.getWidth(), WindowManager.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(expenseCategoryDropdown, 0, 10); // Add 10dp gap

        // Change border color to blue when dropdown is expanded
        dropdownContainer.setBackgroundResource(R.drawable.dropdown_background_selected);
        dropdownArrow.setImageResource(R.drawable.ic_arrow_up);

        ExpenseCategoryAdapter adapter = new ExpenseCategoryAdapter(categories, category -> {
            expenseCategoryDropdown.setText(category.getName());
            expenseCategoryDropdown.setTextColor(ContextCompat.getColor(expenseCategoryDropdown.getContext(), R.color.spinner_selected));

            // Update icon to blue for the selected category
            expenseCategoryDropdown.setCompoundDrawablesWithIntrinsicBounds(category.getIconResId(), 0, 0, 0);
            expenseCategoryDropdown.getCompoundDrawables()[0].setTint(ContextCompat.getColor(expenseCategoryDropdown.getContext(), R.color.spinner_selected));

            // Define icon size for selected category (25dp)
            int iconSize = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    25, // 25dp
                    Resources.getSystem().getDisplayMetrics()
            );

            // Set selected category icon (force 25dp size)
            Drawable selectedIcon = ContextCompat.getDrawable(expenseCategoryDropdown.getContext(), category.getIconResId());
            if (selectedIcon != null) {
                selectedIcon.setBounds(0, 0, iconSize, iconSize);
                expenseCategoryDropdown.setCompoundDrawables(selectedIcon, null, null, null);
            }

            popupWindow.dismiss();
        });

        recyclerView.setAdapter(adapter);

        popupWindow.setOnDismissListener(() -> {
            dropdownContainer.setBackgroundResource(R.drawable.dropdown_background);
            dropdownArrow.setImageResource(R.drawable.ic_arrow_down);
        });

        dropdownArrow.setImageResource(R.drawable.ic_arrow_up);
        popupWindow.showAsDropDown(expenseCategoryDropdown);
    }

    private void handleSubmit() {
        String amountText = amountDisplay.getText().toString().trim();
        String category = expenseCategoryDropdown.getText().toString().trim();

        //  Validation Check: Prevent empty submission
        if (amountText.isEmpty()) {
            Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }
        if (category.isEmpty() || category.equals("Select Category")) {
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                Toast.makeText(getContext(), "Amount must be greater than zero", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount entered", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send data to parent using FragmentManage
        Bundle result = new Bundle();
        result.putString("expense_amount", amountText);
        result.putString("expense_category", category);
        getParentFragmentManager().setFragmentResult("expense_request", result);

        dismiss(); // Close the bottom sheet
    }

}
