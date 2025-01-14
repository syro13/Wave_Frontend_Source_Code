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

import java.util.ArrayList;

public class GroceryItemAdapter extends ArrayAdapter<GroceryItem> {

    private ArrayList<GroceryItem> groceryItems;
    private SaveGroceryItemsCallback callback;

    // Callback interface
    public interface SaveGroceryItemsCallback {
        void saveGroceryItems(ArrayList<GroceryItem> groceryItems);
    }

    public GroceryItemAdapter(Context context, ArrayList<GroceryItem> items, SaveGroceryItemsCallback callback) {
        super(context, 0, items);
        this.groceryItems = items;
        this.callback = callback;
    }

    @Override
    public @NonNull View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grocery_list_item, parent, false);
        }

        GroceryItem item = getItem(position);

        ImageView checkIcon = convertView.findViewById(R.id.item_check);
        TextView itemText = convertView.findViewById(R.id.item_text);
        ImageView deleteIcon = convertView.findViewById(R.id.item_delete);

        if (item != null) {
            itemText.setText(item.text);

            Drawable checkedCircle = ContextCompat.getDrawable(getContext(), R.drawable.toggle_button_unselected);
            Drawable uncheckedCircle = ContextCompat.getDrawable(getContext(), R.drawable.toggle_button_selected);

            checkIcon.setImageDrawable(item.checked ? checkedCircle : uncheckedCircle);

            checkIcon.setOnClickListener(v -> {
                item.checked = !item.checked;
                checkIcon.setImageDrawable(item.checked ? checkedCircle : uncheckedCircle);
                callback.saveGroceryItems(groceryItems); // Save updated list
                notifyDataSetChanged();
            });

            deleteIcon.setOnClickListener(v -> {
                groceryItems.remove(position);
                callback.saveGroceryItems(groceryItems); // Save updated list
                notifyDataSetChanged();
            });
        }

        return convertView;
    }
}
