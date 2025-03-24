package com.example.wave;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExpenseTest {

    private Expense expense;

    @Before
    public void setUp() {
        expense = new Expense("Food", 50.0);
    }

    @Test
    public void testConstructor() {
        Expense expense = new Expense("Transport", 100.0);
        assertEquals("Transport", expense.getCategoryName());
        assertEquals(100.0, expense.getAmountSpent(), 0.0);
    }

    @Test
    public void testGetCategoryName() {
        String categoryName = expense.getCategoryName();
        assertEquals("Food", categoryName);
    }

    @Test
    public void testGetAmountSpent() {
        double amountSpent = expense.getAmountSpent();
        assertEquals(50.0, amountSpent, 0.0);
    }

    @Test
    public void testSetCategoryName() {
        expense.setCategoryName("Shopping");
        assertEquals("Shopping", expense.getCategoryName());
    }

    @Test
    public void testSetAmountSpent() {
        expense.setAmountSpent(200.0);
        assertEquals(200.0, expense.getAmountSpent(), 0.0);
    }

    @Test
    public void testGetPercentageSpent() {
        double totalBudget = 200.0;
        double percentage = expense.getPercentageSpent(totalBudget);
        assertEquals(25.0, percentage, 0.0);
    }

    @Test
    public void testGetPercentageSpent_NoBudget() {
        double totalBudget = 0.0;
        double percentage = expense.getPercentageSpent(totalBudget);
        assertEquals(0.0, percentage, 0.0);
    }
}
