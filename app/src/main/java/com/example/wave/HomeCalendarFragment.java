package com.example.wave;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Color;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HomeCalendarFragment extends Fragment implements TaskAdapter.OnTaskDeletedListener, TaskAdapter.OnTaskEditedListener  {

    private RecyclerView calendarRecyclerView, taskRecyclerView, weeklyTaskRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TaskAdapter taskAdapter, weeklyTaskAdapter;
    private List<String> calendarDates;
    private List<Task> taskList;
    private Calendar calendar;
    private Spinner monthYearDropdown;
    private TextView homeCalendarButton, schoolCalendarButton; // Toggle buttons
    public static final int REQUEST_EDIT_TASK = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_house_calendar_screen, container, false);

        // Initialize calendar
        calendar = Calendar.getInstance();

        // Initialize task list and load tasks
        taskList = new ArrayList<>();
        loadDummyTasks(); // Load initial tasks before setting task dates

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

        ImageView profileIcon = view.findViewById(R.id.profileIcon);

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), ProfileActivity.class);
                startActivity(intent);
            }
        });
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

        // Set up month selection dropdown
        monthYearDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.BLACK); // Force black text
                }
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

        // Populate school task dates
        Set<String> schoolTaskDates = getSchoolTaskDates();
        Set<String> homeTaskDates = getHomeTaskDates();

        // Initialize calendar dates
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

        // Initialize CalendarAdapter with dynamically retrieved dates
        calendarAdapter = new CalendarAdapter(
                calendarDates,
                selectedDate -> {
                    int selectedDay = Integer.parseInt(selectedDate);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                    // Home tasks only for HomeCalendarFragment
                    List<Task> selectedDateTasks = filterTasksByDateBasedOnCategory(selectedDay, "Home");

                    // Update the task adapter to show the tasks for the selected date
                    taskAdapter.updateTasks(selectedDateTasks);

                    // Update the title (e.g., "Tasks for February 1st")
                    updateTasksTitle(selectedDateTasks, selectedDay);
                },
                getSchoolTaskDates(),   // Can remain as is or pass empty Set
                getHomeTaskDates(),     // Important for Home tasks
                "Home"                  // Pass "Home" as the selected category
        );

        calendarRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);


        taskAdapter = new TaskAdapter(new ArrayList<>(), requireContext(), this, this, editTaskLauncher);
        weeklyTaskAdapter = new TaskAdapter(new ArrayList<>(), requireContext(), this, this, editTaskLauncher);


        // Set adapters to RecyclerViews
        taskRecyclerView.setAdapter(taskAdapter);
        weeklyTaskRecyclerView.setAdapter(weeklyTaskAdapter);

        // Update tasks initially
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
        updateWeeklyTasks();

        taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskRecyclerView.setAdapter(taskAdapter);

        weeklyTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        weeklyTaskRecyclerView.setAdapter(weeklyTaskAdapter);

        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
        monthYearDropdown.setSelection(calendar.get(Calendar.MONTH));

        return view;
    }
    @Override
    public void onTaskDeleted(Task task) {
        // Perform any necessary actions after a task is deleted
        updateWeeklyTasks();
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onTaskEdited(Task updatedTask, int position) {
        updateExistingTask(updatedTask);
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
        updateWeeklyTasks();
        taskAdapter.notifyDataSetChanged();
    }

    /**
     * Finds the existing task in the list and updates it instead of adding a new one.
     */
    private void updateExistingTask(Task updatedTask) {
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getId().equals(updatedTask.getId())) { // Ensure unique ID check
                taskList.set(i, updatedTask);
                return;
            }
        }
    }

    private final ActivityResultLauncher<Intent> editTaskLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Task updatedTask = result.getData().getParcelableExtra("updatedTask");
                    int position = result.getData().getIntExtra("position", -1);

                    Log.d("SchoolCalendarFragment", "Received updated task: " + (updatedTask != null ? updatedTask.getTitle() : "NULL"));

                    if (updatedTask != null && position >= 0 && position < taskList.size()) {
                        taskList.set(position, updatedTask);
                        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
                        updateWeeklyTasks();
                        taskAdapter.notifyItemChanged(position);
                    } else {
                        Log.e("SchoolCalendarFragment", "Invalid task update: NULL or position out of bounds.");
                    }
                }
            });


    private void updateTasksTitle(List<Task> selectedDateTasks, int selectedDay) {
        TextView tasksDueTodayTitle = getView().findViewById(R.id.tasksDueTodayTitle);
        if (selectedDateTasks.isEmpty()) {
            tasksDueTodayTitle.setText("No tasks for selected date");
        } else {
            String monthYear = getMonthYearList().get(calendar.get(Calendar.MONTH)) + " " + calendar.get(Calendar.YEAR);
            String formattedDate = selectedDay + getOrdinalSuffix(selectedDay) + " " + monthYear;
            tasksDueTodayTitle.setText("Tasks for " + formattedDate);
        }
    }

    private List<Task> filterTasksByDateBasedOnCategory(int day, String category) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : taskList) {
            // Compare task day and category
            if (Integer.parseInt(task.getDate()) == day && task.getCategory().equals(category)) {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }

    private Set<String> getSchoolTaskDates() {
        Set<String> schoolTaskDates = new HashSet<>();

        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        int currentYear = calendar.get(Calendar.YEAR);

        for (Task task : taskList) {
            if ("School".equals(task.getCategory()) &&
                    task.getMonth().equalsIgnoreCase(currentMonth) &&
                    task.getYear() == currentYear) {  // Ensure correct year check
                schoolTaskDates.add(task.getDate());  // Only add dates for current month/year
            }
        }
        return schoolTaskDates;
    }
    private Set<String> getHomeTaskDates() {
        Set<String> homeTaskDates = new HashSet<>();

        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        int currentYear = calendar.get(Calendar.YEAR);

        for (Task task : taskList) {
            if ("Home".equals(task.getCategory()) &&
                    task.getMonth().equalsIgnoreCase(currentMonth) &&
                    task.getYear() == currentYear) {  // Ensure correct year check
                homeTaskDates.add(task.getDate());  // Only add dates for current month/year
            }
        }
        return homeTaskDates;
    }


    private String getOrdinalSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th"; // Special case for 11th, 12th, and 13th
        }
        switch (day % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }


    private void updateWeeklyTasks() {
        String currentDateString = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                (calendar.get(Calendar.MONTH) + 1) + "/" +
                calendar.get(Calendar.YEAR);
        filterTasksByWeek(currentDateString);
    }


    public void addTaskToCalendar(String title, String priority, String date, String time, boolean remind, String taskType) {
        if (taskList == null) {
            taskList = new ArrayList<>();
        }

        // Generate a unique ID for the task
        String taskId = UUID.randomUUID().toString();

        // Parse the date to extract day, month, and year
        String[] dateParts = date.split("/");
        if (dateParts.length != 3) {
            Log.e("addTaskToCalendar", "Invalid date format: " + date);
            Toast.makeText(requireContext(), "Invalid date format!", Toast.LENGTH_SHORT).show();
            return;
        }

        String day = dateParts[0];
        String month = dateParts[1];
        int year = Integer.parseInt(dateParts[2]); // Convert year to integer

        // Create a new task (Ensure correct parameter order)
        Task newTask = new Task(
                taskId,                                    // Unique Task ID (String)
                title,                                     // Task Title
                time,                                      // Time (String)
                day,                                       // Date (String)
                getMonthYearList().get(Integer.parseInt(month) - 1), // Month Name (String)
                priority,                                  // Priority (String)
                taskType,                                  // Task Type (String)
                remind,                                    // Reminder (Boolean)
                year                                       // Year (int)
        );

        // Check if task exists, update it instead of adding a duplicate
        updateExistingTask(newTask);

        // Update task lists
        int dayInt = Integer.parseInt(day);
        String monthName = getMonthYearList().get(Integer.parseInt(month) - 1);
        List<Task> selectedDateTasks = filterTasksByDate(dayInt, monthName);
        taskAdapter.updateTasks(selectedDateTasks);
        updateWeeklyTasks();

        // Confirmation
        Toast.makeText(requireContext(), "Task added: " + title, Toast.LENGTH_SHORT).show();
    }


    private void updateTasksForToday(int day) {
        // Get the current category directly (either Home or School based on fragment)
        String currentCategory = "Home";

        // Filter tasks based on the current day and category
        List<Task> todayTasks = filterTasksByDateBasedOnCategory(day, currentCategory);

        // Update the task adapter with the filtered tasks
        taskAdapter.updateTasks(todayTasks);
    }


    private void updateCalendar() {
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        Set<String> taskDates = getSchoolTaskDates();

        calendarAdapter.updateSchoolTaskDates(taskDates);
        calendarAdapter.updateData(calendarDates);

        // Update weekly tasks
        updateWeeklyTasks();
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

    List<String> getMonthYearList() {
        return List.of("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
    }


    private int[] getWeekRange(int selectedDay, int daysInMonth) {
        int startDay = Math.max(1, selectedDay - (selectedDay - 1) % 7);
        int endDay = Math.min(daysInMonth, startDay + 6);
        return new int[]{startDay, endDay};
    }

    private List<Task> filterTasksByDate(int day, String month) {
        List<Task> filteredTasks = new ArrayList<>();
        try {
            for (Task task : taskList) {
                // Compare task day and month
                if (Integer.parseInt(task.getDate()) == day &&
                        task.getMonth().equals(month)) {
                    filteredTasks.add(task);
                }
            }
        } catch (NumberFormatException e) {
            Log.e("filterTasksByDate", "Error parsing task date: " + e.getMessage());
        }
        return filteredTasks;
    }

    private void filterTasksByWeek(String dateString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            LocalDate selectedDate = LocalDate.parse(dateString, formatter);
            int selectedWeekOfYear = selectedDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);

            List<Task> weeklyTasks = new ArrayList<>();
            for (Task task : taskList) {
                try {
                    // Correctly construct the task's full date using the day, month, and year
                    String taskFullDate = task.getDate() + "/" + (getMonthIndex(task.getMonth()) + 1) + "/" + task.getYear();

                    // Parse the task date and compare the week of the year
                    LocalDate taskDate = LocalDate.parse(taskFullDate, formatter);
                    int taskWeekOfYear = taskDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);

                    // Check if the task falls in the same week
                    if (taskWeekOfYear == selectedWeekOfYear) {
                        weeklyTasks.add(task);
                    }
                } catch (DateTimeParseException e) {
                    Log.e("WeeklyTasks", "Error parsing task date: " + task.getDate() + " - " + e.getMessage());
                }
            }

            // Update the weekly task adapter with the filtered tasks
            weeklyTaskAdapter.updateTasks(weeklyTasks);
        } catch (DateTimeParseException e) {
            Log.e("WeeklyTasks", "Error parsing selected date: " + e.getMessage());
        }
    }

    private int getMonthIndex(String month) {
        return getMonthYearList().indexOf(month);
    }


    private void loadDummyTasks() {
            taskList.add(new Task(UUID.randomUUID().toString(),"Clean Bedroom", "10:00 AM", "25", "January", "Low", "Home", false, 2025));
            taskList.add(new Task(UUID.randomUUID().toString(),"Do Laundry", "2:00 PM", "26", "January", "High", "Home", true, 2025));
            taskList.add(new Task(UUID.randomUUID().toString(),"Organize Closet", "11:00 AM", "27", "January", "Low", "Home", false, 2025));
            taskList.add(new Task(UUID.randomUUID().toString(),"Grocery Shopping", "5:00 PM", "28", "January", "Medium", "Home", false, 2025));
            taskList.add(new Task(UUID.randomUUID().toString(),"Meal Prep", "3:00 PM", "29", "January", "Medium", "Home", false, 2025));
            taskList.add(new Task(UUID.randomUUID().toString(),"Vacuum Living Room", "4:00 PM", "30", "January", "Low", "Home", false, 2025));
            taskList.add(new Task(UUID.randomUUID().toString(),"Take Out Trash", "7:00 PM", "31", "January", "High", "Home", true, 2025));

            taskList.add(new Task(UUID.randomUUID().toString(),"Clean Kitchen", "9:00 AM", "1", "February", "High", "Home", true, 2025));
            taskList.add(new Task(UUID.randomUUID().toString(),"Water Plants", "11:00 AM", "2", "February", "High", "Home", true, 2025));
            taskList.add(new Task(UUID.randomUUID().toString(),"Dust Shelves", "1:00 PM", "3", "February", "Low", "Home", false, 2025));
            taskList.add(new Task(UUID.randomUUID().toString(),"Mop Floors", "10:00 AM", "4", "February", "Medium", "Home", false, 2025));
            taskList.add(new Task(UUID.randomUUID().toString(),"Wash Dishes", "6:00 PM", "5", "February", "High", "Home", true, 2025));
            taskList.add(new Task(UUID.randomUUID().toString(),"Organize Pantry", "2:00 PM", "6", "February", "Medium", "Home", false, 2025));
            taskList.add(new Task(UUID.randomUUID().toString(),"Clean Windows", "12:00 PM", "7", "February", "Low", "Home", false, 2025));
        }




    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(requireContext().getResources().getColor(android.R.color.white));

        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(requireContext().getResources().getColor(R.color.dark_blue));
    }
}
