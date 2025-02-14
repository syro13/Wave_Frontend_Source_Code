package com.example.wave;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ExpenseCategoryUnitTest {

    // Test the ExpenseCategory class constructor
    @Test
    public void testExpenseCategoryConstructor() {
        // Create an ExpenseCategory object
        ExpenseCategory category = new ExpenseCategory("Food", R.drawable.ic_food);

        // Assert the name and icon resource ID
        assertEquals("Food", category.getName());
        assertEquals(R.drawable.ic_food, category.getIconResId());
    }

 // Test the getName method of the ExpenseCategory class
    @Test
    public void testExpenseCategoryGetName() {
        // Create an ExpenseCategory object
        ExpenseCategory category = new ExpenseCategory("Travel", R.drawable.ic_travel);

        // Assert the getName method
        assertEquals("Travel", category.getName());
    }

    // Test the getIconResId method of the ExpenseCategory class
    @Test
    public void testExpenseCategoryGetIconResId() {
        // Create an ExpenseCategory object
        ExpenseCategory category = new ExpenseCategory("Shopping", R.drawable.ic_shopping);

        // Assert the getIconResId method
        assertEquals(R.drawable.ic_shopping, category.getIconResId());
    }
}
