// Updated SchoolCalendarFragment to match HomeCalendarFragment

package com.example.wave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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

    private RecyclerView calendarRecyclerView, taskRecyclerView, weeklyTaskRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TaskAdapter taskAdapter, weeklyTaskAdapter;
    private List<String> calendarDates;
    private List<Task> taskList;
    private Calendar calendar;
    private Spinner monthYearDropdown;
    private TextView schoolCalendarButton, homeCalendarButton; // Toggle buttons

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_school_calendar_screen, container, false);

        // Initialize calendar
        calendar = Calendar.getInstance();

        // Initialize task list
        taskList = new ArrayList<>();

        // Initialize views
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        taskRecyclerView = view.findViewById(R.id.taskRecyclerView);
        weeklyTaskRecyclerView = view.findViewById(R.id.weeklyTaskRecyclerView);
        monthYearDropdown = view.findViewById(R.id.monthYearDropdown);
        TextView tasksDueThisWeekTitle = view.findViewById(R.id.tasksDueThisWeekTitle);
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
            monthYearDropdown.setSelection(calendar.get(Calendar.MONTH));
        });

        nextMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
            monthYearDropdown.setSelection(calendar.get(Calendar.MONTH));
        });

        // Initialize calendar dates
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

        // Set up calendarAdapter with click listener
        calendarAdapter = new CalendarAdapter(calendarDates, selectedDate -> {
            int selectedDay = Integer.parseInt(selectedDate);
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            // Update calendar's current day
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

            // Get tasks for the selected date
            List<Task> selectedDateTasks = filterTasksByDate(selectedDay, getMonthYearList().get(calendar.get(Calendar.MONTH)));

            // Update the tasks due today RecyclerView
            taskAdapter.updateTasks(selectedDateTasks);

            // Update tasks for the week
            int[] weekRange = getWeekRange(selectedDay, daysInMonth);
            List<Task> weeklyTasks = filterTasksByWeek(weekRange[0], weekRange[1], getMonthYearList().get(calendar.get(Calendar.MONTH)));
            weeklyTaskAdapter.updateTasks(weeklyTasks);

            // Update the titles
            tasksDueThisWeekTitle.setText(String.format("Tasks for %s %d - %d",
                    getMonthYearList().get(calendar.get(Calendar.MONTH)), weekRange[0], weekRange[1]));

            TextView tasksDueTodayTitle = getView().findViewById(R.id.tasksDueTodayTitle);
            if (selectedDateTasks.isEmpty()) {
                tasksDueTodayTitle.setText("No tasks for selected date");
            } else {
                tasksDueTodayTitle.setText("Tasks for " + selectedDate);
            }
        });

        // Attach the adapter to the RecyclerView
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);

        // Initialize task adapters
        taskAdapter = new TaskAdapter(new ArrayList<>(), position -> {
            // Handle task deletion
            taskList.remove(position);
            taskAdapter.notifyItemRemoved(position);
        });

        weeklyTaskAdapter = new TaskAdapter(new ArrayList<>(), position -> {
            // Handle weekly task deletion
            taskList.remove(position);
            weeklyTaskAdapter.notifyItemRemoved(position);
        });

        // Set up RecyclerViews
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskRecyclerView.setAdapter(taskAdapter);

        weeklyTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        weeklyTaskRecyclerView.setAdapter(weeklyTaskAdapter);

        // Load dummy tasks
        loadDummyTasks();

        // Update tasks for today
        updateTasksForToday(view);

        // Set the spinner to the current month
        monthYearDropdown.setSelection(calendar.get(Calendar.MONTH));

        return view;
    }

    private void updateTasksForToday(View rootView) {
        calendar = Calendar.getInstance(); // Reset to the current date
        TextView tasksDueTodayTitle = rootView.findViewById(R.id.tasksDueTodayTitle);
        List<Task> todayTasks = filterTasksByDate(calendar.get(Calendar.DAY_OF_MONTH), getMonthYearList().get(calendar.get(Calendar.MONTH)));
        taskAdapter.updateTasks(todayTasks);

        if (todayTasks.isEmpty()) {
            tasksDueTodayTitle.setText("No tasks for today");
        } else {
            tasksDueTodayTitle.setText("Today");
        }
    }

    private void updateCalendar() {
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        calendarAdapter.updateData(calendarDates);

        // Clear the weekly tasks when switching months
        weeklyTaskAdapter.updateTasks(new ArrayList<>());
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

    private int[] getWeekRange(int selectedDay, int daysInMonth) {
        int startDay = Math.max(1, selectedDay - (selectedDay - 1) % 7);
        int endDay = Math.min(daysInMonth, startDay + 6);
        return new int[]{startDay, endDay};
    }

    private List<Task> filterTasksByDate(int day, String month) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : taskList) {
            if (task.getDate().equals(String.valueOf(day)) && task.getMonth().equals(month)) {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }

    private List<Task> filterTasksByWeek(int startDay, int endDay, String month) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : taskList) {
            int taskDay = Integer.parseInt(task.getDate());
            if (task.getMonth().equals(month) && taskDay >= startDay && taskDay <= endDay) {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }

    private void loadDummyTasks() {
        taskList.add(new Task("Prepare Presentation", "10:00 AM", "25", "January", "School", false, false));
        taskList.add(new Task("Submit Assignment", "11:59 PM", "26", "January", "School", true, false));
        taskList.add(new Task("Attend Workshop", "2:00 PM", "27", "January", "School", false, false));
        taskList.add(new Task("Complete Lab Report", "3:00 PM", "28", "January", "School", false, true));
        taskList.add(new Task("Group Study Session", "5:00 PM", "29", "January", "School", false, false));
        taskList.add(new Task("Faculty Meeting", "1:00 PM", "30", "January", "School", false, false));
        taskList.add(new Task("Prepare Notes", "4:00 PM", "31", "January", "School", false, true));

        taskList.add(new Task("Join Webinar", "9:00 AM", "1", "February", "School", false, true));
        taskList.add(new Task("Library Visit", "10:00 AM", "2", "February", "School", false, false));
        taskList.add(new Task("Schedule Exam", "11:00 AM", "3", "February", "School", false, false));
        taskList.add(new Task("Plan Project", "1:00 PM", "4", "February", "School", false, false));
        taskList.add(new Task("Team Presentation", "3:00 PM", "5", "February", "School", false, true));
        taskList.add(new Task("Mock Exam", "10:00 AM", "6", "February", "School", false, false));
        taskList.add(new Task("Classroom Discussion", "2:00 PM", "7", "February", "School", false, false));
    }

    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(requireContext().getResources().getColor(android.R.color.white));

        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(requireContext().getResources().getColor(R.color.dark_blue));
    }
}
