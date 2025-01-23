package com.example.wave;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SchoolCalendarFragment extends Fragment {

    private RecyclerView calendarRecyclerView, taskRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TaskAdapter taskAdapter;
    private List<String> calendarDates;
    private List<Task> taskList;
    private Calendar calendar;
    private boolean isExpanded = true;
    private Spinner monthYearDropdown;
    private TextView schoolCalendarButton, homeCalendarButton; // Toggle buttons

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_school_calendar_screen, container, false);

        // Initialize calendar
        calendar = Calendar.getInstance();

        // Initialize task list before loading dummy tasks
        taskList = new ArrayList<>();

        // Initialize views
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        taskRecyclerView = view.findViewById(R.id.taskRecyclerView);
        monthYearDropdown = view.findViewById(R.id.monthYearDropdown);
        ImageView expandCollapseIcon = view.findViewById(R.id.expandCollapseIcon);
        ImageView previousMonth = view.findViewById(R.id.previousMonth);
        ImageView nextMonth = view.findViewById(R.id.nextMonth);

        // Initialize toggle buttons
        schoolCalendarButton = view.findViewById(R.id.SchoolCalendarButton);
        homeCalendarButton = view.findViewById(R.id.homeCalendarButton);

        // Set initial active state for toggle buttons
        setActiveButton(schoolCalendarButton, homeCalendarButton);


        // Handle Home Tasks button click
        homeCalendarButton.setOnClickListener(v -> {
            setActiveButton(homeCalendarButton, schoolCalendarButton);
            if (getActivity() instanceof SchoolHomeCalendarActivity) {
                ((SchoolHomeCalendarActivity) getActivity()).showHomeCalendarFragment();
            }
        });


        // Set up spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getMonthYearList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthYearDropdown.setAdapter(adapter);

        // Handle dropdown selection
        monthYearDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calendar.set(Calendar.MONTH, position);
                updateCalendar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Month navigation logic
        previousMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        // Expand/collapse logic
        expandCollapseIcon.setOnClickListener(v -> {
            isExpanded = !isExpanded;
            updateCalendarView(isExpanded);

            // Update the icon based on the current state
            if (isExpanded) {
                expandCollapseIcon.setImageResource(R.drawable.ic_unexpand); // "Up arrow"
            } else {
                expandCollapseIcon.setImageResource(R.drawable.ic_expand_more); // "Down arrow"
            }
        });

        // Initialize calendar dates and adapters
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

        calendarAdapter = new CalendarAdapter(calendarDates, date -> {
            // Get the selected month as a string
            String selectedMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));

            // Filter tasks for the selected date
            List<Task> filteredTasks = filterTasksByDate(date, selectedMonth);

            // Update the TaskAdapter with the filtered tasks
            taskAdapter.updateTasks(filteredTasks);
        });

        taskAdapter = new TaskAdapter(new ArrayList<>(), position -> {
            // Handle task deletion
            taskList.remove(position);
            taskAdapter.notifyItemRemoved(position);
        });

        // Set up RecyclerViews
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);

        taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskRecyclerView.setAdapter(taskAdapter);

        // Load dummy tasks
        loadDummyTasks();

        // Set the spinner to the current month
        monthYearDropdown.setSelection(calendar.get(Calendar.MONTH));

        return view;
    }

    private void updateCalendar() {
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        calendarAdapter.updateData(calendarDates);

        // Clear the task list when switching months
        taskAdapter.updateTasks(new ArrayList<>());
    }

    private void updateCalendarView(boolean expand) {
        if (expand) {
            calendarRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            calendarRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 200)); // Height for week view
        }
    }

    private List<String> getCalendarDates(int year, int month) {
        List<String> dates = new ArrayList<>();
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.set(year, month, 1);

        int firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Add padding for days before the first day of the month
        for (int i = 0; i < firstDayOfWeek; i++) {
            dates.add(""); // Empty strings for blank spaces
        }

        // Add actual dates
        for (int day = 1; day <= daysInMonth; day++) {
            dates.add(String.valueOf(day));
        }

        return dates;
    }

    private List<String> getMonthYearList() {
        List<String> months = new ArrayList<>();
        months.add("January");
        months.add("February");
        months.add("March");
        months.add("April");
        months.add("May");
        months.add("June");
        months.add("July");
        months.add("August");
        months.add("September");
        months.add("October");
        months.add("November");
        months.add("December");
        return months;
    }

    private List<Task> filterTasksByDate(String date, String month) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : taskList) {
            if (task.getDate().equals(date) && task.getMonth().equals(month)) {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }

    private void loadDummyTasks() {
        // Add your dummy tasks here
    }

    private void loadSchoolTasks() {
        // Load school-specific tasks
        taskAdapter.updateTasks(taskList); // Replace with filtered tasks for "School"
    }

    private void loadHomeTasks() {
        // Load home-specific tasks
        taskAdapter.updateTasks(taskList); // Replace with filtered tasks for "Home"
    }

    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(requireContext().getResources().getColor(android.R.color.white));

        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(requireContext().getResources().getColor(R.color.dark_blue));
    }
}
