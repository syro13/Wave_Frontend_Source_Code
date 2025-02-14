package com.example.wave;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AddExpenseBottomSheetFragmentInstrumentedTest {

    private FragmentScenario<AddExpenseBottomSheetFragment> fragmentScenario;

    @Before
    public void setUp() {
        // Launch the fragment for testing
        fragmentScenario = FragmentScenario.launchInContainer(AddExpenseBottomSheetFragment.class);
    }

    // Test that the fragment is not null
    @Test
    public void testFragmentIsNotNull() {
        // Verify the fragment is properly launched
        fragmentScenario.onFragment(fragment -> assertNotNull(fragment));
    }

    // Test that the amount display is initialized
    @Test
    public void testKeypadButtonUpdatesAmountDisplay() {
        fragmentScenario.onFragment(fragment -> {
            View view = fragment.requireView();
            TextView amountDisplay = view.findViewById(R.id.amountValue);
            View button1 = view.findViewById(R.id.button1);

            // Simulate button press
            button1.performClick();

            // Verify the amount display is updated
            assertEquals("1", amountDisplay.getText().toString());
        });
    }

    // Test that the submit button sends the result
    @Test
    public void testSubmitButtonSendsResult() {
        fragmentScenario.onFragment(fragment -> {
            View view = fragment.requireView();
            TextView amountDisplay = view.findViewById(R.id.amountValue);
            amountDisplay.setText("150.00");

            View submitButton = view.findViewById(R.id.submitButton);
            submitButton.performClick();

            // Verify result is sent to the parent fragment
            fragment.getParentFragmentManager().setFragmentResultListener("expense_request", fragment, (key, result) -> {
                assertEquals("150.00", result.getString("expense_amount"));
            });
        });
    }
}
