package com.example.wave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

public class CombinedCalendarFragment extends Fragment {

    private RecyclerView calendarRecyclerView, taskRecyclerView, weeklyTaskRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TaskAdapter taskAdapter, weeklyTaskAdapter;
    private List<String> calendarDates;
    private List<Task> schoolTaskList, homeTaskList, combinedTaskList;
    private Calendar calendar;
    private Spinner monthYearDropdown;
    private TextView schoolToggleButton, bothToggleButton, houseToggleButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_combined_calendar_screen, container, false);

        // Initialize calendar
        calendar = Calendar.getInstance();

        // Initialize task lists
        schoolTaskList = new ArrayList<>();
        homeTaskList = new ArrayList<>();
        combinedTaskList = new ArrayList<>();

        // Initialize views
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        taskRecyclerView = view.findViewById(R.id.taskRecyclerView);
        weeklyTaskRecyclerView = view.findViewById(R.id.weeklyTaskRecyclerView);
        monthYearDropdown = view.findViewById(R.id.monthYearDropdown);
        schoolToggleButton = view.findViewById(R.id.schoolToggleButton);
        bothToggleButton = view.findViewById(R.id.bothToggleButton);
        houseToggleButton = view.findViewById(R.id.houseToggleButton);

        // Initialize calendar dates
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

        // Set up calendar adapter with click listener
        calendarAdapter = new CalendarAdapter(calendarDates, selectedDate -> {
            int selectedDay = Integer.parseInt(selectedDate);
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
            updateTasksForToday(selectedDay);
        });

        calendarRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);

        // Initialize task adapters
        taskAdapter = new TaskAdapter(new ArrayList<>(), position -> {
            Task taskToRemove = taskAdapter.getTaskList().get(position);
            combinedTaskList.remove(taskToRemove);
            taskAdapter.notifyItemRemoved(position);
            updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
        });

        weeklyTaskAdapter = new TaskAdapter(new ArrayList<>(), position -> {
            Task taskToRemove = weeklyTaskAdapter.getTaskList().get(position);
            combinedTaskList.remove(taskToRemove);
            weeklyTaskAdapter.notifyItemRemoved(position);
            updateWeeklyTasks();
        });

        taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskRecyclerView.setAdapter(taskAdapter);

        weeklyTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        weeklyTaskRecyclerView.setAdapter(weeklyTaskAdapter);

        loadDummyTasks();
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));

        // Set up month selection dropdown
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

        // Set up category toggle buttons
        schoolToggleButton.setOnClickListener(v -> {
            setActiveButton(schoolToggleButton, bothToggleButton, houseToggleButton);
            updateCurrentTasks();
        });

        houseToggleButton.setOnClickListener(v -> {
            setActiveButton(houseToggleButton, bothToggleButton, schoolToggleButton);
            updateCurrentTasks();
        });

        bothToggleButton.setOnClickListener(v -> {
            setActiveButton(bothToggleButton, schoolToggleButton, houseToggleButton);
            updateCurrentTasks();
        });

        return view;
    }

    private void updateTasksForToday(int day) {
        String currentCategory = getCurrentSelectedCategory();
        List<Task> todayTasks;

        switch (currentCategory) {
            case "School":
                todayTasks = filterTasksByDate(day, getMonthYearList().get(calendar.get(Calendar.MONTH)), schoolTaskList);
                break;
            case "Home":
                todayTasks = filterTasksByDate(day, getMonthYearList().get(calendar.get(Calendar.MONTH)), homeTaskList);
                break;
            default: // "Both"
                todayTasks = filterTasksByDate(day, getMonthYearList().get(calendar.get(Calendar.MONTH)), combinedTaskList);
                break;
        }

        taskAdapter.updateTasks(todayTasks);
    }

    private void updateWeeklyTasks() {
        // Weekly task filtering logic (for the selected week)
        // Call `weeklyTaskAdapter.updateTasks(weeklyTasks);` after filtering
    }

    private void updateCalendar() {
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        calendarAdapter.updateData(calendarDates);
    }

    private List<String> getCalendarDates(int year, int month) {
        List<String> dates = new ArrayList<>();
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.set(year, month, 1);
        int firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDayOfWeek; i++) {
            dates.add(""); // Empty strings for blank spaces
        }
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

    private List<Task> filterTasksByDate(int day, String month, List<Task> taskListToFilter) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : taskListToFilter) {
            if (Integer.parseInt(task.getDate()) == day && task.getMonth().equalsIgnoreCase(month)) {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }

    private void loadDummyTasks() {
        // School tasks
        schoolTaskList.add(new Task("Math Homework", "10:00 AM", "25", "January", "High", "School", false));
        schoolTaskList.add(new Task("Science Project", "2:00 PM", "26", "January", "Medium", "School", true));
        schoolTaskList.add(new Task("Physics Lab Report", "11:00 AM", "1", "February", "High", "School", false));
        schoolTaskList.add(new Task("History Assignment", "9:30 AM", "2", "February", "Medium", "School", false));
        schoolTaskList.add(new Task("Group Project Meeting", "3:00 PM", "3", "February", "Low", "School", true));
        schoolTaskList.add(new Task("Biology Quiz Preparation", "4:00 PM", "4", "February", "Medium", "School", false));
        schoolTaskList.add(new Task("Chemistry Lab Prep", "10:00 AM", "5", "February", "High", "School", true));
        schoolTaskList.add(new Task("Essay Submission", "1:00 PM", "6", "February", "High", "School", false));
        schoolTaskList.add(new Task("Math Revision", "10:30 AM", "7", "February", "Medium", "School", false));
        schoolTaskList.add(new Task("Sports Practice", "5:00 PM", "7", "February", "Low", "School", false));
        schoolTaskList.add(new Task("Computer Science Coding Assignment", "2:00 PM", "8", "February", "High", "School", false));
        schoolTaskList.add(new Task("Art Project Presentation", "12:00 PM", "9", "February", "Medium", "School", true));

        // Home tasks
        homeTaskList.add(new Task("Clean Kitchen", "8:00 PM", "27", "January", "Low", "Home", false));
        homeTaskList.add(new Task("Buy Groceries", "6:00 PM", "28", "January", "High", "Home", true));
        homeTaskList.add(new Task("Laundry", "9:00 AM", "1", "February", "Medium", "Home", false));
        homeTaskList.add(new Task("Pay Bills", "10:00 AM", "2", "February", "High", "Home", true));
        homeTaskList.add(new Task("Meal Prep", "5:30 PM", "3", "February", "Medium", "Home", false));
        homeTaskList.add(new Task("Organize Closet", "11:00 AM", "4", "February", "Low", "Home", false));
        homeTaskList.add(new Task("Repair Leaky Faucet", "2:30 PM", "5", "February", "High", "Home", true));
        homeTaskList.add(new Task("Take Dog for a Walk", "7:00 AM", "6", "February", "Low", "Home", false));
        homeTaskList.add(new Task("Wash Car", "10:00 AM", "7", "February", "Medium", "Home", false));
        homeTaskList.add(new Task("Clean Living Room", "1:00 PM", "8", "February", "Low", "Home", false));
        homeTaskList.add(new Task("Grocery Shopping", "4:00 PM", "9", "February", "Medium", "Home", true));
        homeTaskList.add(new Task("Cook Dinner", "6:30 PM", "9", "February", "High", "Home", false));

        // Combine school and home tasks
        combinedTaskList.addAll(schoolTaskList);
        combinedTaskList.addAll(homeTaskList);
    }


    public void addTaskToCalendar(String taskTitle, String taskPriority, String taskDate, String taskTime, boolean remind, String taskType) {
        Task newTask = new Task(taskTitle, taskTime, taskDate, getMonthYearList().get(calendar.get(Calendar.MONTH)), taskPriority, taskType, remind);

        if (taskType.equals("School")) {
            schoolTaskList.add(newTask);
        } else if (taskType.equals("Home")) {
            homeTaskList.add(newTask);
        }

        combinedTaskList.add(newTask);
        updateCurrentTasks();
    }

    private String getCurrentSelectedCategory() {
        if (schoolToggleButton.isSelected()) {
            return "School";
        } else if (houseToggleButton.isSelected()) {
            return "Home";
        } else {
            return "Both";
        }
    }

    private void updateCurrentTasks() {
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
    }

    private void setActiveButton(TextView activeButton, TextView... inactiveButtons) {
        activeButton.setSelected(true);

        if (activeButton == schoolToggleButton) {
            activeButton.setBackgroundResource(R.drawable.toggle_school_selected);
        } else if (activeButton == bothToggleButton) {
            activeButton.setBackgroundResource(R.drawable.toggle_both_selected);
        } else if (activeButton == houseToggleButton) {
            activeButton.setBackgroundResource(R.drawable.toggle_house_selected);
        }

        activeButton.setTextColor(getResources().getColor(R.color.dark_blue));

        for (TextView inactiveButton : inactiveButtons) {
            inactiveButton.setSelected(false);
            if (inactiveButton.getId() == R.id.bothToggleButton) {
                inactiveButton.setBackgroundResource(R.drawable.both_toggle_background);
            } else if (inactiveButton.getId() == R.id.houseToggleButton) {
                inactiveButton.setBackgroundResource(R.drawable.house_toggle_background);
            } else {
                inactiveButton.setBackgroundResource(R.drawable.school_toggle_background);
            }
            inactiveButton.setTextColor(getResources().getColor(R.color.gray));
        }
    }
}
