package com.example.wave;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseCategoryAdapter extends RecyclerView.Adapter<ExpenseCategoryAdapter.ViewHolder> {

    private final List<ExpenseCategory> categories;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(ExpenseCategory category);
    }

    public ExpenseCategoryAdapter(List<ExpenseCategory> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseCategory category = categories.get(position);
        holder.categoryName.setText(category.getName());
        holder.categoryIcon.setImageResource(category.getIconResId());

        // Set default icon color to gray (for dropdown menu items)
        holder.categoryIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.spinner_icon_gray));

        // Set icon size dynamically using dimens.xml (For dropdown menu icons)
        int iconSize = (int) holder.itemView.getContext().getResources().getDimension(R.dimen.dropdown_icon_size);
        ViewGroup.LayoutParams layoutParams = holder.categoryIcon.getLayoutParams();
        layoutParams.width = iconSize;
        layoutParams.height = iconSize;
        holder.categoryIcon.setLayoutParams(layoutParams);

        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }


    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIcon;
        TextView categoryName;

        ViewHolder(View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }
}
