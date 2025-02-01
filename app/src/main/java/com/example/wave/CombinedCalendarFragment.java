package com.example.wave;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CombinedCalendarFragment extends Fragment {

    private RecyclerView calendarRecyclerView, taskRecyclerView, weeklyTaskRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TaskAdapter taskAdapter, weeklyTaskAdapter;
    private List<String> calendarDates;
    private List<Task> schoolTaskList, homeTaskList, combinedTaskList;
    private Calendar calendar;
    private Spinner monthYearDropdown;
    private TextView schoolToggleButton, bothToggleButton, houseToggleButton;
    private TextView homeCalendarButton, schoolCalendarButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_combined_calendar_screen, container, false);

        // Initialize calendar and task lists
        calendar = Calendar.getInstance();
        schoolTaskList = new ArrayList<>();
        homeTaskList = new ArrayList<>();
        combinedTaskList = new ArrayList<>();

        // Initialize views
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        taskRecyclerView = view.findViewById(R.id.taskRecyclerView);
        weeklyTaskRecyclerView = view.findViewById(R.id.weeklyTaskRecyclerView);
        monthYearDropdown = view.findViewById(R.id.monthYearDropdown);
        homeCalendarButton = view.findViewById(R.id.homeCalendarButton);
        schoolCalendarButton = view.findViewById(R.id.SchoolCalendarButton);
        schoolToggleButton = view.findViewById(R.id.schoolToggleButton);
        bothToggleButton = view.findViewById(R.id.bothToggleButton);
        houseToggleButton = view.findViewById(R.id.houseToggleButton);
        ImageView previousMonth = view.findViewById(R.id.previousMonth);
        ImageView nextMonth = view.findViewById(R.id.nextMonth);

        // Set up month navigation
        previousMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

// Initialize CalendarAdapter with dynamically retrieved dates
        calendarAdapter = new CalendarAdapter(calendarDates, selectedDate -> {
            int selectedDay = Integer.parseInt(selectedDate);
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
            updateTasksForToday(selectedDay);
        }, getSchoolTaskDates(), getHomeTaskDates());


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
        homeCalendarButton.setOnClickListener(v -> {
            setActiveTopButton(homeCalendarButton, schoolCalendarButton);
            if (getActivity() instanceof SchoolHomeCalendarActivity) {
                ((SchoolHomeCalendarActivity) getActivity()).showHomeCalendarFragment();
            }
        });

        schoolCalendarButton.setOnClickListener(v -> {
            setActiveTopButton(schoolCalendarButton, homeCalendarButton);
            if (getActivity() instanceof SchoolHomeCalendarActivity) {
                ((SchoolHomeCalendarActivity) getActivity()).showSchoolCalendarFragment();
            }
        });

        setupToggleButtons();
        return view;
    }

    private void setupToggleButtons() {
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

    private void updateCalendar() {
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

        // Refresh task dates for the current month
        Set<String> schoolTaskDates = getSchoolTaskDates();
        Set<String> homeTaskDates = getHomeTaskDates();

        // Initialize or update the CalendarAdapter
        if (calendarAdapter == null) {
            calendarAdapter = new CalendarAdapter(calendarDates, selectedDate -> {
                int selectedDay = Integer.parseInt(selectedDate);
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                updateTasksForToday(selectedDay);
            }, schoolTaskDates, homeTaskDates);
            calendarRecyclerView.setAdapter(calendarAdapter);
        } else {
            calendarAdapter.updateData(calendarDates);
            calendarAdapter.updateSchoolTaskDates(schoolTaskDates);
            calendarAdapter.updateHomeTaskDates(homeTaskDates);
        }
    }
    private void updateWeeklyTasks() {
        List<Task> weeklyTasks = new ArrayList<>();
        Calendar currentCalendar = Calendar.getInstance();

        // Get the start and end date of the current week
        int currentYear = currentCalendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentWeek = calendar.get(Calendar.WEEK_OF_MONTH);

        for (Task task : combinedTaskList) {
            Calendar taskCalendar = Calendar.getInstance();
            taskCalendar.set(currentYear, getMonthIndex(task.getMonth()), Integer.parseInt(task.getDate()));

            int taskWeek = taskCalendar.get(Calendar.WEEK_OF_MONTH);
            int taskMonth = taskCalendar.get(Calendar.MONTH);

            // Check if the task falls within the current week and month
            if (taskMonth == currentMonth && taskWeek == currentWeek) {
                weeklyTasks.add(task);
            }
        }

        // Update weekly task adapter
        weeklyTaskAdapter.updateTasks(weeklyTasks);
    }
    private int getMonthIndex(String month) {
        List<String> months = getMonthYearList();
        return months.indexOf(month);
    }


    private Set<String> getSchoolTaskDates() {
        Set<String> schoolTaskDates = new HashSet<>();
        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        int currentYear = calendar.get(Calendar.YEAR);

        for (Task task : schoolTaskList) {
            if (task.getMonth().equalsIgnoreCase(currentMonth) && task.getYear() == currentYear) {
                schoolTaskDates.add(task.getDate());
            }
        }
        return schoolTaskDates;
    }

    private Set<String> getHomeTaskDates() {
        Set<String> homeTaskDates = new HashSet<>();
        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        int currentYear = calendar.get(Calendar.YEAR);

        for (Task task : homeTaskList) {
            if (task.getMonth().equalsIgnoreCase(currentMonth) && task.getYear() == currentYear) {
                homeTaskDates.add(task.getDate());
            }
        }
        return homeTaskDates;
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
    public void addTaskToCalendar(String taskTitle, String taskPriority, String taskDate, String taskTime, boolean remind, String taskType) {
        Task newTask = new Task(taskTitle, taskTime, taskDate, getMonthYearList().get(calendar.get(Calendar.MONTH)), taskPriority, taskType, remind, calendar.get(Calendar.YEAR));

        // Add the task to the appropriate list based on its type
        if (taskType.equals("School")) {
            schoolTaskList.add(newTask);
        } else if (taskType.equals("Home")) {
            homeTaskList.add(newTask);
        }
        else if (taskType.equals("Combined")){
            combinedTaskList.add(newTask);
        }
        // Always add to the combined list
        combinedTaskList.add(newTask);

        // Refresh the calendar and task views
        updateCalendar();
        updateCurrentTasks();
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");

        for (Task task : taskListToFilter) {
            try {
                // Construct the full date string from task components
                String fullDate = task.getDate() + "/" + (getMonthIndex(task.getMonth()) + 1) + "/" + task.getYear();

                // Parse the full date
                Date taskDate = dateFormat.parse(fullDate);
                Calendar taskCalendar = Calendar.getInstance();
                taskCalendar.setTime(taskDate);

                int taskDay = taskCalendar.get(Calendar.DAY_OF_MONTH);
                String taskMonth = new SimpleDateFormat("MMMM").format(taskCalendar.getTime());

                // Compare day and month
                if (taskDay == day && taskMonth.equalsIgnoreCase(month)) {
                    filteredTasks.add(task);
                }

            } catch (ParseException e) {
                e.printStackTrace();  // Log the error in case of invalid date format
            }
        }
        return filteredTasks;
    }


    private void loadDummyTasks() {
            // School tasks
            schoolTaskList.add(new Task("Math Homework", "10:00 AM", "25", "January", "High", "School", false, 2025));
            schoolTaskList.add(new Task("Science Project", "2:00 PM", "26", "January", "Medium", "School", true, 2025));
            schoolTaskList.add(new Task("Physics Lab Report", "11:00 AM", "1", "February", "High", "School", false, 2025));
            schoolTaskList.add(new Task("History Assignment", "9:30 AM", "2", "February", "Medium", "School", false, 2025));
            schoolTaskList.add(new Task("Group Project Meeting", "3:00 PM", "3", "February", "Low", "School", true, 2025));
            schoolTaskList.add(new Task("Biology Quiz Preparation", "4:00 PM", "4", "February", "Medium", "School", false, 2025));
            schoolTaskList.add(new Task("Chemistry Lab Prep", "10:00 AM", "5", "February", "High", "School", true, 2025));
            schoolTaskList.add(new Task("Essay Submission", "1:00 PM", "6", "February", "High", "School", false, 2025));
            schoolTaskList.add(new Task("Math Revision", "10:30 AM", "7", "February", "Medium", "School", false, 2025));
            schoolTaskList.add(new Task("Sports Practice", "5:00 PM", "7", "February", "Low", "School", false, 2025));
            schoolTaskList.add(new Task("Computer Science Coding Assignment", "2:00 PM", "8", "February", "High", "School", false, 2025));
            schoolTaskList.add(new Task("Art Project Presentation", "12:00 PM", "9", "February", "Medium", "School", true, 2025));

            // Home tasks
            homeTaskList.add(new Task("Clean Kitchen", "8:00 PM", "27", "January", "Low", "Home", false, 2025));
            homeTaskList.add(new Task("Buy Groceries", "6:00 PM", "28", "January", "High", "Home", true, 2025));
            homeTaskList.add(new Task("Laundry", "9:00 AM", "1", "February", "Medium", "Home", false, 2025));
            homeTaskList.add(new Task("Pay Bills", "10:00 AM", "2", "February", "High", "Home", true, 2025));
            homeTaskList.add(new Task("Meal Prep", "5:30 PM", "3", "February", "Medium", "Home", false, 2025));
            homeTaskList.add(new Task("Organize Closet", "11:00 AM", "4", "February", "Low", "Home", false, 2025));
            homeTaskList.add(new Task("Repair Leaky Faucet", "2:30 PM", "5", "February", "High", "Home", true, 2025));
            homeTaskList.add(new Task("Take Dog for a Walk", "7:00 AM", "6", "February", "Low", "Home", false, 2025));
            homeTaskList.add(new Task("Wash Car", "10:00 AM", "7", "February", "Medium", "Home", false, 2025));
            homeTaskList.add(new Task("Clean Living Room", "1:00 PM", "8", "February", "Low", "Home", false, 2025));
            homeTaskList.add(new Task("Grocery Shopping", "4:00 PM", "9", "February", "Medium", "Home", true, 2025));
            homeTaskList.add(new Task("Cook Dinner", "6:30 PM", "9", "February", "High", "Home", false, 2025));


            combinedTaskList.addAll(schoolTaskList);
            combinedTaskList.addAll(homeTaskList);
    }

    private void setActiveTopButton(TextView activeButton, TextView... inactiveButtons) {
        activeButton.setSelected(true);
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(getResources().getColor(R.color.dark_blue));

        for (TextView inactiveButton : inactiveButtons) {
            inactiveButton.setSelected(false);
            inactiveButton.setBackgroundResource(R.drawable.toggle_unselected);
            inactiveButton.setTextColor(getResources().getColor(R.color.gray));
        }
        updateCurrentTasks();
    }
    private void setActiveButton(TextView activeButton, TextView... inactiveButtons) {
        // Mark the active button as selected
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

        // After updating buttons, update tasks
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
}
