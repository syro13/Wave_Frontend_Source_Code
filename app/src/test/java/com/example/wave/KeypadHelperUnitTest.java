package com.example.wave;

import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class KeypadHelperUnitTest {

    private KeypadHelper keypadHelper;
    private TextView mockAmountDisplay;

    @Before
    public void setUp() {
        // Create a mocked TextView
        mockAmountDisplay = Mockito.mock(TextView.class);
        // Initialize KeypadHelper with the mocked TextView
        keypadHelper = new KeypadHelper(mockAmountDisplay);
    }

    // Test pressing a number key
    @Test
    public void testHandleKeyPress_Number() {
        // Test pressing "1"
        keypadHelper.handleKeyPress("1");
        verify(mockAmountDisplay).setText("1");

        // Test appending another number
        keypadHelper.handleKeyPress("2");
        verify(mockAmountDisplay).setText("12");
    }

    // Test pressing "0" initially and then adding a non-zero digit
    @Test
    public void testHandleKeyPress_LeadingZero() {
        // Test pressing "0" initially
        keypadHelper.handleKeyPress("0");
        verify(mockAmountDisplay).setText("0");

        // Add a non-zero digit after "0"
        keypadHelper.handleKeyPress("5");
        verify(mockAmountDisplay).setText("5");
    }

    // Test backspace when the display is empty
    @Test
    public void testHandleKeyPress_BackspaceEmpty() {
        // Test backspace when the display is empty
        keypadHelper.handleKeyPress("⌫");
        verify(mockAmountDisplay).setText("0");
    }

    // Test the decimal point functionality
    @Test
    public void testHandleKeyPress_DecimalPoint() {
        // Test entering a decimal point
        keypadHelper.handleKeyPress(".");
        verify(mockAmountDisplay).setText("0.");

        // Add numbers after the decimal
        keypadHelper.handleKeyPress("5");
        keypadHelper.handleKeyPress("6");
        verify(mockAmountDisplay).setText("0.56");
    }
}
