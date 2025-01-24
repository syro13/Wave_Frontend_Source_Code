package com.example.wave;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private List<String> calendarDates;
    private OnDateClickListener listener;
    private String selectedDate = ""; // Keeps track of the currently selected date

    public CalendarAdapter(List<String> calendarDates, OnDateClickListener listener) {
        this.calendarDates = calendarDates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String date = calendarDates.get(position);
        holder.bind(date);
    }

    @Override
    public int getItemCount() {
        return calendarDates.size();
    }

    public void updateData(List<String> newDates) {
        this.calendarDates = newDates;
        notifyDataSetChanged();
    }

    public class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
        }

        public void bind(String date) {
            dateText.setText(date);

            // Hide empty cells (padding for the first week of the month)
            if (date.isEmpty()) {
                dateText.setVisibility(View.INVISIBLE);
            } else {
                dateText.setVisibility(View.VISIBLE);

                // Highlight selected date
                if (date.equals(selectedDate)) {
                    dateText.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.circle_background_selected));
                } else {
                    dateText.setBackground(null); // Reset background for non-selected dates
                }

                // Handle date click
                itemView.setOnClickListener(v -> {
                    selectedDate = date;
                    notifyDataSetChanged(); // Refresh RecyclerView to update selection
                    listener.onDateClick(date); // Pass clicked date to listener
                });
            }
        }
    }

    public interface OnDateClickListener {
        void onDateClick(String date); // Callback for date clicks
    }
}
