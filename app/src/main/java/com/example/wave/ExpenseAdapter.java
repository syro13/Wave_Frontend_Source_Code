package com.example.wave;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Expense> expenseList;
    private double totalBudget;

    public ExpenseAdapter(List<Expense> expenseList, double totalBudget) {
        this.expenseList = expenseList;
        this.totalBudget = totalBudget;
    }

    public void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
        notifyDataSetChanged();
    }

    public void updateExpenseList(List<Expense> newList) {
        this.expenseList = newList;
        notifyDataSetChanged(); // Ensure RecyclerView updates
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.categoryName.setText(expense.getCategoryName());
        holder.expenseAmount.setText("€" + String.format("%.2f", expense.getAmountSpent()));

        // Dynamically calculate the percentage of budget spent per category
        double percentageSpent = expense.getPercentageSpent(totalBudget);
        holder.expensePercentage.setText(String.format("%.1f%%", percentageSpent));

        if (expense.getAmountSpent() == 0) {
            holder.expenseAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));
            holder.expensePercentage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));
        } else {
            holder.expenseAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.blue));
            holder.expensePercentage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.blue));
        }

        int iconResId = getCategoryIcon(expense.getCategoryName());
        holder.categoryIcon.setImageResource(iconResId);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName, expenseAmount, expensePercentage;
        ImageView categoryIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.expenseTitle);
            expenseAmount = itemView.findViewById(R.id.expenseAmount);
            expensePercentage = itemView.findViewById(R.id.expensePercentage);
            categoryIcon = itemView.findViewById(R.id.expenseIcon);
        }
    }

    private int getCategoryIcon(String categoryName) {
        switch (categoryName) {
            case "Shopping":
                return R.drawable.ic_shopping_blue;
            case "Food":
                return R.drawable.ic_food_blue;
            case "Groceries":
                return R.drawable.ic_groceries_blue;
            case "Bills":
                return R.drawable.ic_bills_blue;
            case "Travel":
                return R.drawable.ic_travel_blue;
            case "Health":
                return R.drawable.ic_health_blue;
            case "Miscellaneous":
                return R.drawable.ic_miscellaneous_blue;
            default:
                return R.drawable.ic_expense; // Default icon
        }
    }
}
