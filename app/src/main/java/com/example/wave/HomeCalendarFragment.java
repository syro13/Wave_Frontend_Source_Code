package com.example.wave;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeCalendarFragment extends Fragment {

    private RecyclerView calendarRecyclerView, taskRecyclerView, weeklyTaskRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TaskAdapter taskAdapter, weeklyTaskAdapter;
    private List<String> calendarDates;
    private List<Task> taskList;
    private Calendar calendar;
    private Spinner monthYearDropdown;
    private TextView homeCalendarButton, schoolCalendarButton; // Toggle buttons

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_house_calendar_screen, container, false);

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
        homeCalendarButton = view.findViewById(R.id.homeCalendarButton);
        schoolCalendarButton = view.findViewById(R.id.SchoolCalendarButton);

        // Set initial active state for toggle buttons
        setActiveButton(homeCalendarButton, schoolCalendarButton);

        homeCalendarButton.setOnClickListener(v -> {
            setActiveButton(homeCalendarButton, schoolCalendarButton);
            if (getActivity() instanceof SchoolHomeCalendarActivity) {
                ((SchoolHomeCalendarActivity) getActivity()).showHomeCalendarFragment();
            }
        });

        schoolCalendarButton.setOnClickListener(v -> {
            setActiveButton(schoolCalendarButton, homeCalendarButton);
            if (getActivity() instanceof SchoolHomeCalendarActivity) {
                ((SchoolHomeCalendarActivity) getActivity()).showSchoolCalendarFragment();
            }
        });

        // Set up spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getMonthYearList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthYearDropdown.setAdapter(adapter);

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
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

            List<Task> selectedDateTasks = filterTasksByDate(selectedDay, getMonthYearList().get(calendar.get(Calendar.MONTH)));
            taskAdapter.updateTasks(selectedDateTasks);

            updateWeeklyTasks(); // Refresh weekly tasks when a new date is selected

            TextView tasksDueTodayTitle = getView().findViewById(R.id.tasksDueTodayTitle);
            if (selectedDateTasks.isEmpty()) {
                tasksDueTodayTitle.setText("No tasks for selected date");
            } else {
                tasksDueTodayTitle.setText("Tasks for " + selectedDate);
            }
        });

        calendarRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);

        // Initialize task adapters with proper deletion logic
        taskAdapter = new TaskAdapter(new ArrayList<>(), position -> {
            Task taskToRemove = taskAdapter.getTaskList().get(position);
            taskList.remove(taskToRemove);
            taskAdapter.notifyItemRemoved(position);
            updateTasksForToday(view);
            updateWeeklyTasks(); // Refresh weekly tasks
        });

        weeklyTaskAdapter = new TaskAdapter(new ArrayList<>(), position -> {
            Task taskToRemove = weeklyTaskAdapter.getTaskList().get(position);
            taskList.remove(taskToRemove);
            weeklyTaskAdapter.removeTask(position);
            updateTasksForToday(view);
            updateWeeklyTasks(); // Refresh weekly tasks
        });

        taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskRecyclerView.setAdapter(taskAdapter);

        weeklyTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        weeklyTaskRecyclerView.setAdapter(weeklyTaskAdapter);

        loadDummyTasks(); // Load initial tasks

        updateTasksForToday(view);

        monthYearDropdown.setSelection(calendar.get(Calendar.MONTH));

        return view;
    }

    private void updateWeeklyTasks() {
        // Get the current week range
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        int[] weekRange = getWeekRange(selectedDay, daysInMonth);

        // Filter tasks for the current week
        List<Task> weeklyTasks = filterTasksByWeek(weekRange[0], weekRange[1], getMonthYearList().get(calendar.get(Calendar.MONTH)));

        // Update the weekly task adapter
        weeklyTaskAdapter.updateTasks(weeklyTasks);
    }



    // Add this method in SchoolCalendarFragment and HomeCalendarFragment

    public void addTaskToCalendar(String title, String priority, String date, String time, boolean remind) {
        if (taskList == null) {
            taskList = new ArrayList<>();
        }

        // Ensure date is stored in the correct format
        String[] dateParts = date.split("/"); // Assuming date is "28/1/2025"
        String taskDay = dateParts[0];
        String taskMonth = getMonthYearList().get(Integer.parseInt(dateParts[1]) - 1); // Convert month number to name

        // Add the task
        Task newTask = new Task(title, time, taskDay, taskMonth, priority, "School", remind);
        taskList.add(newTask);

        // Filter and update tasks for the current day
        List<Task> todayTasks = filterTasksByDate(calendar.get(Calendar.DAY_OF_MONTH), getMonthYearList().get(calendar.get(Calendar.MONTH)));
        taskAdapter.updateTasks(todayTasks);

        Toast.makeText(requireContext(), "Task added: " + title, Toast.LENGTH_SHORT).show();
    }





    private void updateTasksForToday(View rootView) {
        calendar = Calendar.getInstance(); // Get the current date
        TextView tasksDueTodayTitle = rootView.findViewById(R.id.tasksDueTodayTitle);

        // Filter tasks for today
        List<Task> todayTasks = filterTasksByDate(calendar.get(Calendar.DAY_OF_MONTH), getMonthYearList().get(calendar.get(Calendar.MONTH)));
        taskAdapter.updateTasks(todayTasks);

        if (todayTasks.isEmpty()) {
            tasksDueTodayTitle.setText("No tasks for today");
        } else {
            tasksDueTodayTitle.setText("Tasks for Today");
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
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

            for (Task task : taskList) {
                // Construct task date using task's day and month
                String taskDateString = task.getDate() + "/" + (getMonthYearList().indexOf(task.getMonth()) + 1) + "/" + Calendar.getInstance().get(Calendar.YEAR);
                LocalDate taskDate = LocalDate.parse(taskDateString, formatter);

                // Construct selected date for comparison
                LocalDate selectedDate = LocalDate.of(Calendar.getInstance().get(Calendar.YEAR), getMonthYearList().indexOf(month) + 1, day);

                // Compare the selected date and task date
                if (taskDate.isEqual(selectedDate)) {
                    filteredTasks.add(task);
                }
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            System.out.println("Date parsing error in filterTasksByDate");
        }
        return filteredTasks;
    }



    private List<Task> filterTasksByWeek(int startDay, int endDay, String month) {
        List<Task> weeklyTasks = new ArrayList<>();
        for (Task task : taskList) {
            int taskDay = Integer.parseInt(task.getDate());
            if (taskDay >= startDay && taskDay <= endDay && task.getMonth().equals(month)) {
                weeklyTasks.add(task);
            }
        }
        return weeklyTasks;
    }


    private void loadDummyTasks() {
        taskList.add(new Task("Clean Bedroom", "10:00 AM", "25", "January", "Low", "Home", false));
        taskList.add(new Task("Do Laundry", "2:00 PM", "26", "January", "High", "Home", true));
        taskList.add(new Task("Organize Closet", "11:00 AM", "27", "January", "Low", "Home", false));
        taskList.add(new Task("Grocery Shopping", "5:00 PM", "28", "January", "Medium", "Home", false));
        taskList.add(new Task("Meal Prep", "3:00 PM", "29", "January", "Medium", "Home", false));
        taskList.add(new Task("Vacuum Living Room", "4:00 PM", "30", "January", "Low", "Home", false));
        taskList.add(new Task("Take Out Trash", "7:00 PM", "31", "January", "High", "Home", true));

        taskList.add(new Task("Clean Kitchen", "9:00 AM", "1", "February", "High", "Home", true));
        taskList.add(new Task("Water Plants", "11:00 AM", "2", "February", "High", "Home", true));
        taskList.add(new Task("Dust Shelves", "1:00 PM", "3", "February", "Low", "Home", false));
        taskList.add(new Task("Mop Floors", "10:00 AM", "4", "February", "Medium", "Home", false));
        taskList.add(new Task("Wash Dishes", "6:00 PM", "5", "February", "High", "Home", true));
        taskList.add(new Task("Organize Pantry", "2:00 PM", "6", "February", "Medium", "Home", false));
        taskList.add(new Task("Clean Windows", "12:00 PM", "7", "February", "Low", "Home", false));
    }


    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(requireContext().getResources().getColor(android.R.color.white));

        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(requireContext().getResources().getColor(R.color.dark_blue));
    }
}
