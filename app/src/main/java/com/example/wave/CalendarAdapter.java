package com.example.wave;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Set;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private List<String> calendarDates;
    private OnDateClickListener listener;
    private String selectedDate = ""; // Keeps track of the currently selected date
    private Set<String> schoolTaskDates;
    private Set<String> homeTaskDates;
    private String currentCategory;

    public CalendarAdapter(List<String> calendarDates, OnDateClickListener listener, Set<String> schoolTaskDates, Set<String> homeTaskDates, String currentCategory) {
        this.calendarDates = calendarDates;
        this.listener = listener;
        this.schoolTaskDates = schoolTaskDates;
        this.homeTaskDates = homeTaskDates;
        this.currentCategory = currentCategory;
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

    public void updateSchoolTaskDates(Set<String> newSchoolTaskDates) {
        this.schoolTaskDates = newSchoolTaskDates;
        notifyDataSetChanged();
    }

    public void updateHomeTaskDates(Set<String> newHomeTaskDates) {
        this.homeTaskDates = newHomeTaskDates;
        notifyDataSetChanged();
    }

    public void updateCategory(String newCategory) {
        this.currentCategory = newCategory;
        notifyDataSetChanged();
    }

    public void setSelectedDate(String date) {
        this.selectedDate = date;
        notifyDataSetChanged();  // Refresh the calendar to show the selected date
    }

    public class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
        }

        public void bind(String date) {
            dateText.setText(date);

            if (date.isEmpty()) {
                dateText.setVisibility(View.INVISIBLE);
                itemView.setOnClickListener(null);
            } else {
                dateText.setVisibility(View.VISIBLE);
                dateText.setBackground(null);  // Clear any previous background
                dateText.setForeground(null);  // Clear any previous foreground selection
                dateText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black)); // Reset default text color

                // Highlight dates based on tasks
                if (currentCategory.equals("Both")) {
                    if (homeTaskDates.contains(date) && schoolTaskDates.contains(date)) {
                        dateText.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.both_circle));
                    } else if (homeTaskDates.contains(date)) {
                        dateText.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.yellow_circle));
                    } else if (schoolTaskDates.contains(date)) {
                        dateText.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.green_circle));
                    }
                } else if (currentCategory.equals("School")) {
                    if (schoolTaskDates.contains(date)) {
                        dateText.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.green_circle));
                    }
                } else if (currentCategory.equals("Home")) {
                    if (homeTaskDates.contains(date)) {
                        dateText.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.yellow_circle));
                    }
                }

                // Highlight selected date
                if (date.equals(selectedDate)) {
                    dateText.setForeground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.circle_background_selected));
                    dateText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.dark_blue));
                }

                itemView.setOnClickListener(v -> {
                    selectedDate = date;  // Update selected date
                    listener.onDateClick(date);  // Notify the listener
                    notifyDataSetChanged();  // Refresh the calendar
                });
            }
        }
    }

    public interface OnDateClickListener {
        void onDateClick(String date);
    }
}
