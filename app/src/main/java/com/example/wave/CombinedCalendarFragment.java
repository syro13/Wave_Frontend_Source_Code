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

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CombinedCalendarFragment extends Fragment implements TaskAdapter.OnTaskDeletedListener, TaskAdapter.OnTaskEditedListener, TaskAdapter.OnTaskCompletedListener  {

    private RecyclerView calendarRecyclerView, taskRecyclerView, weeklyTaskRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TaskAdapter taskAdapter, weeklyTaskAdapter;
    private List<String> calendarDates;
    private List<Task> schoolTaskList, homeTaskList, combinedTaskList;
    private Calendar calendar;
    private Spinner monthYearDropdown;
    private TextView schoolToggleButton, bothToggleButton, houseToggleButton;
    private TextView homeCalendarButton, schoolCalendarButton;
    public static final int REQUEST_EDIT_TASK = 1001;
    private boolean isExpanded = true; // Start with full month view
    private GridLayoutManager gridLayoutManager;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        TextView selectedDateText = view.findViewById(R.id.selectedDateText);
        updateSelectedDateText(selectedDateText, calendar.get(Calendar.DAY_OF_MONTH));
        ImageView expandCollapseIcon = view.findViewById(R.id.expandCollapseIcon);

        // Set initial layout manager (Full Month View)
        gridLayoutManager = new GridLayoutManager(requireContext(), 7);
        calendarRecyclerView.setLayoutManager(gridLayoutManager);
        expandCollapseIcon.setOnClickListener(v -> toggleCalendarView(expandCollapseIcon));

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

        calendarAdapter = new CalendarAdapter(
                calendarDates,
                selectedDate -> {
                    if (!selectedDate.isEmpty()) {
                        int selectedDay = Integer.parseInt(selectedDate);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                        // Get the current category selected by the toggle buttons
                        String currentCategory = getCurrentSelectedCategory();

                        // Filter tasks based on the selected category and date
                        List<Task> selectedDateTasks = filterTasksByDateBasedOnCategory(selectedDay, currentCategory);

                        // Update the task adapter to show the tasks for the selected date
                        taskAdapter.updateTasks(selectedDateTasks);

                        // Update the title for the selected date
                        updateTasksTitle(selectedDateTasks, selectedDay);

                        // ✅ Now updates weekly tasks as well
                        updateWeeklyTasks();
                    }
                },
                getSchoolTaskDates(),
                getHomeTaskDates(),
                getCurrentSelectedCategory()
        );




        // Set up RecyclerView and adapters
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);

        // Initialize TaskAdapter with editTaskLauncher
        // Initialize TaskAdapter with editTaskLauncher
        taskAdapter = new TaskAdapter(new ArrayList<>(), requireContext(), this, this,this, editTaskLauncher);
        weeklyTaskAdapter = new TaskAdapter(new ArrayList<>(), requireContext(), this, this,this,  editTaskLauncher);

        taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskRecyclerView.setAdapter(taskAdapter);

        weeklyTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        weeklyTaskRecyclerView.setAdapter(weeklyTaskAdapter);

        // Load tasks and update UI for the current day
        loadDummyTasks();
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
//set up toggle buttons
        // Show tasks for today's date
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));

        // Setup month-year spinner
        // Create a custom adapter using your layout with text + dropdown icon
        MonthYearSpinnerAdapter adapter = new MonthYearSpinnerAdapter(requireContext(), getMonthYearList());
        monthYearDropdown.setAdapter(adapter);

        monthYearDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                calendar.set(Calendar.MONTH, position);
                updateCalendar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        homeCalendarButton.setOnClickListener(v -> {
            setActiveButton(homeCalendarButton, schoolCalendarButton);
            if (getActivity() instanceof SchoolHomeCalendarActivity) {
                ((SchoolHomeCalendarActivity) getActivity()).showHomeCalendarFragment();
            }
            updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
        });

        schoolCalendarButton.setOnClickListener(v -> {
            setActiveButton(schoolCalendarButton, homeCalendarButton);
            if (getActivity() instanceof SchoolHomeCalendarActivity) {
                ((SchoolHomeCalendarActivity) getActivity()).showSchoolCalendarFragment();
            }
            updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
        });
        // Get today's date
        int todayDay = calendar.get(Calendar.DAY_OF_MONTH);
        int todayMonth = calendar.get(Calendar.MONTH);
        int todayYear = calendar.get(Calendar.YEAR);

        // Set dropdown to today's month
        monthYearDropdown.setSelection(todayMonth);

        // Update selected date TextView to today's date
        updateSelectedDateText(selectedDateText, todayDay);

        // Auto-select today's date in the calendar adapter
        calendarAdapter.setSelectedDate(String.valueOf(todayDay));

        // Load today's tasks
        updateTasksForToday(todayDay);
        updateWeeklyTasks();
        ImageView profileIcon = view.findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProfileActivity.class);
            startActivity(intent);
        });

        setupToggleButtons();
        return view;
    }

    @Override
    public void onTaskCompleted(Task task) {
        // Handle UI updates after completion if needed
    }

    private void toggleCalendarView(ImageView expandCollapseIcon) {
        if (isExpanded) {
            // Shrink to show only current week
            gridLayoutManager.setSpanCount(7);
            int startDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            int firstDayOfWeek = currentDay - startDay; // Adjust to Monday start

            List<String> weekDates = calendarDates.subList(Math.max(firstDayOfWeek, 0), Math.min(firstDayOfWeek + 7, calendarDates.size()));
            calendarAdapter.updateData(weekDates);

            expandCollapseIcon.setImageResource(R.drawable.ic_expand_more);
        } else {
            // Expand to full month
            gridLayoutManager.setSpanCount(7);
            calendarAdapter.updateData(getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)));

            expandCollapseIcon.setImageResource(R.drawable.ic_unexpand);
        }

        isExpanded = !isExpanded;
    }

    private void updateSelectedDateText(TextView selectedDateText, int day) {
        String monthName = getMonthYearList().get(calendar.get(Calendar.MONTH));
        String formattedDate = getFormattedDayOfWeek(calendar) + " " + day + getOrdinalSuffix(day) + " " + monthName;

        selectedDateText.setText(formattedDate);
    }

    // Get the day of the week (e.g., "Tuesday")
    private String getFormattedDayOfWeek(Calendar calendar) {
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, java.util.Locale.getDefault());
    }

    private void setupToggleButtons() {
        schoolToggleButton.setOnClickListener(v -> {
            setActiveButton(schoolToggleButton, bothToggleButton, houseToggleButton);
            calendarAdapter.updateCategory("School");
            updateCurrentTasks();
            updateWeeklyTasks(); // ✅ Ensures weekly tasks update when switching category
        });

        houseToggleButton.setOnClickListener(v -> {
            setActiveButton(houseToggleButton, bothToggleButton, schoolToggleButton);
            calendarAdapter.updateCategory("Home");
            updateCurrentTasks();
            updateWeeklyTasks(); // ✅ Ensures weekly tasks update when switching category
        });

        bothToggleButton.setOnClickListener(v -> {
            setActiveButton(bothToggleButton, schoolToggleButton, houseToggleButton);
            calendarAdapter.updateCategory("Both");
            updateCurrentTasks();
            updateWeeklyTasks(); // ✅ Ensures weekly tasks update when switching category
        });
    }


    @Override
    public void onTaskDeleted(Task task) {
        // Remove from relevant lists
        if ("School".equals(task.getCategory())) {
            schoolTaskList.removeIf(t -> t.getId().equals(task.getId()));
        } else if ("Home".equals(task.getCategory())) {
            homeTaskList.removeIf(t -> t.getId().equals(task.getId()));
        }
        combinedTaskList.removeIf(t -> t.getId().equals(task.getId()));

        // Refresh daily & weekly tasks
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
        updateWeeklyTasks();
        taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskEdited(Task updatedTask, int position) {
        Log.d("CombinedCalendar", "onTaskEdited: " + updatedTask.getTitle());

        // Update in the appropriate list(s)
        if ("School".equals(updatedTask.getCategory())) {
            updateTaskInList(schoolTaskList, updatedTask);
        } else if ("Home".equals(updatedTask.getCategory())) {
            updateTaskInList(homeTaskList, updatedTask);
        }
        updateTaskInList(combinedTaskList, updatedTask);

        // If user changed the date, jump the calendar to that new date so the UI updates right away
        try {
            String updatedFullDate = updatedTask.getFullDate(); // e.g. "3/2/2025"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            LocalDate updatedLocalDate = LocalDate.parse(updatedFullDate, formatter);

            calendar.set(Calendar.YEAR, updatedLocalDate.getYear());
            // LocalDate months are 1-based, Calendar is 0-based
            calendar.set(Calendar.MONTH, updatedLocalDate.getMonthValue() - 1);
            calendar.set(Calendar.DAY_OF_MONTH, updatedLocalDate.getDayOfMonth());

        } catch (Exception e) {
            Log.e("CombinedCalendar", "Error updating calendar date: " + e.getMessage());
        }

        // Refresh everything
        updateCalendar();
        updateWeeklyTasks();
        taskAdapter.notifyDataSetChanged();
    }

    private void updateTaskInList(List<Task> tasks, Task updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(updatedTask.getId())) {
                tasks.set(i, updatedTask);
                return;
            }
        }
    }




    private final ActivityResultLauncher<Intent> editTaskLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task updatedTask = result.getData().getParcelableExtra("updatedTask");
                    if (updatedTask != null) {
                        onTaskEdited(updatedTask, -1);
                    } else {
                        Log.e("CombinedCalendar", "Received null updatedTask");
                    }
                }
            });





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
    private void updateTasksTitle(List<Task> tasks, int selectedDay) {
        TextView tasksDueTodayTitle = getView().findViewById(R.id.tasksDueTodayTitle);
        if (tasks.isEmpty()) {
            tasksDueTodayTitle.setText("No tasks for selected date");
        } else {
            String monthYear = getMonthYearList().get(calendar.get(Calendar.MONTH)) + " " + calendar.get(Calendar.YEAR);
            String formattedDate = selectedDay + getOrdinalSuffix(selectedDay) + " " + monthYear;
            tasksDueTodayTitle.setText("Tasks for " + formattedDate);
        }
    }

    private List<Task> filterTasksByDateBasedOnCategory(int day, String currentCategory) {
        String currentMonthName = getMonthYearList().get(calendar.get(Calendar.MONTH));
        List<Task> results = new ArrayList<>();
        if ("School".equals(currentCategory)) {
            results.addAll(filterTasksByDayAndMonth(schoolTaskList, day, currentMonthName));
        } else if ("Home".equals(currentCategory)) {
            results.addAll(filterTasksByDayAndMonth(homeTaskList, day, currentMonthName));
        } else {
            // Both: combine
            results.addAll(filterTasksByDayAndMonth(schoolTaskList, day, currentMonthName));
            results.addAll(filterTasksByDayAndMonth(homeTaskList, day, currentMonthName));
        }
        return results;
    }

    /**
     * Helper for filtering tasks by day & month in a specific list.
     */
    private List<Task> filterTasksByDayAndMonth(List<Task> tasks, int day, String monthName) {
        List<Task> filtered = new ArrayList<>();
        for (Task t : tasks) {
            if (Integer.parseInt(t.getDate()) == day && t.getMonth().equalsIgnoreCase(monthName)) {
                filtered.add(t);
            }
        }
        return filtered;
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

        Set<String> schoolTaskDates = getSchoolTaskDates();
        Set<String> homeTaskDates = getHomeTaskDates();

        if (calendarAdapter != null) {
            calendarAdapter.updateData(calendarDates);
            calendarAdapter.updateSchoolTaskDates(schoolTaskDates);
            calendarAdapter.updateHomeTaskDates(homeTaskDates);
            calendarAdapter.updateCategory(getCurrentSelectedCategory());
        }

        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
        updateWeeklyTasks();
    }



    private void updateWeeklyTasks() {
        if (getView() == null) {
            Log.e("updateWeeklyTasks", "View is null, skipping UI updates.");
            return;
        }

        // ✅ Select tasks based on the active category
        String selectedCategory = getCurrentSelectedCategory();
        List<Task> filteredTaskList;

        if ("School".equals(selectedCategory)) {
            filteredTaskList = new ArrayList<>(schoolTaskList); // ✅ Only school tasks
        } else if ("Home".equals(selectedCategory)) {
            filteredTaskList = new ArrayList<>(homeTaskList); // ✅ Only home tasks
        } else { // "Both"
            filteredTaskList = new ArrayList<>(combinedTaskList); // ✅ Both tasks
        }

        LocalDate currentDate = LocalDate.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = currentDate.with(DayOfWeek.SUNDAY);

        List<Task> weeklyTasks = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

        for (Task t : filteredTaskList) { // ✅ Now uses the correct filtered list
            try {
                String fullDate = t.getDate() + "/" + (getMonthIndex(t.getMonth()) + 1) + "/" + t.getYear();
                LocalDate taskDate = LocalDate.parse(fullDate, formatter);
                if (!taskDate.isBefore(startOfWeek) && !taskDate.isAfter(endOfWeek)) {
                    weeklyTasks.add(t);
                }
            } catch (DateTimeParseException e) {
                Log.e("updateWeeklyTasks", "Error parsing date: " + e.getMessage());
            }
        }

        // ✅ Sort by priority (High → Medium → Low)
        weeklyTasks.sort((t1, t2) -> getPriorityValue(t1.getPriority()) - getPriorityValue(t2.getPriority()));

        // ✅ Update UI visibility
        View tasksTitle = getView().findViewById(R.id.tasksDueThisWeekTitle);
        View weeklyRecyclerView = getView().findViewById(R.id.weeklyTaskRecyclerView);

        if (weeklyTasks.isEmpty()) {
            tasksTitle.setVisibility(View.GONE);
            weeklyRecyclerView.setVisibility(View.GONE);
        } else {
            tasksTitle.setVisibility(View.VISIBLE);
            weeklyRecyclerView.setVisibility(View.VISIBLE);
            weeklyTaskAdapter.updateTasks(weeklyTasks);
            weeklyTaskAdapter.notifyDataSetChanged();
        }
    }



    // ✅ Helper Function for Priority Sorting
    private int getPriorityValue(String priority) {
        switch (priority) {
            case "High": return 1; // 🔴 Red (Most Important)
            case "Medium": return 2; // 🟡 Yellow
            case "Low": return 3; // 🟢 Green (Least Important)
            default: return 4;
        }
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
    /**
     * Called when user adds a new task.
     */
    public void addTaskToCalendar(String taskTitle, String taskPriority, String taskDate, String taskTime,
                                  boolean remind, String taskType) {
        String taskId = UUID.randomUUID().toString();
        // Suppose user typed "4/2/2025" => parse that
        String[] dateParts = taskDate.split("/");
        if (dateParts.length != 3) {
            Toast.makeText(requireContext(), "Invalid date format!", Toast.LENGTH_SHORT).show();
            return;
        }
        String day = dateParts[0];
        String month = dateParts[1];
        int year = Integer.parseInt(dateParts[2]);

        Task newTask = new Task(
                taskId,
                taskTitle,
                taskTime,
                dateParts[0],
                getMonthYearList().get(Integer.parseInt(dateParts[1]) - 1),
                taskPriority,
                taskType,
                remind,
                year,
                0, // Default stability value, update as needed
                System.currentTimeMillis(), // Set task timestamp to current time
                dateParts[0] + "/" + dateParts[1] + "/" + year,
                false

        );
        // Insert into relevant list
        if ("School".equals(taskType)) {
            schoolTaskList.add(newTask);
        } else if ("Home".equals(taskType)) {
            homeTaskList.add(newTask);
        }
        // Always keep combined in sync
        combinedTaskList.add(newTask);

        // Refresh
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
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(calendar.get(Calendar.YEAR), getMonthIndex(month), day);

        for (Task task : taskListToFilter) {
            try {
                Calendar taskCalendar = Calendar.getInstance();
                taskCalendar.set(task.getYear(), getMonthIndex(task.getMonth()), Integer.parseInt(task.getDate()));

                if (taskCalendar.get(Calendar.DAY_OF_MONTH) == day &&
                        taskCalendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH)) {
                    filteredTasks.add(task);
                }
            } catch (NumberFormatException e) {
                Log.e("FilterTasksByDate", "Error parsing task date: " + e.getMessage());
            }
        }
        return filteredTasks;
    }



    private void loadDummyTasks() {
            // School tasks
        // School tasks
        schoolTaskList.add(new Task(UUID.randomUUID().toString(), "Math Homework", "10:00 AM", "25",
                "January", "High", "School", false, 2025, 0, System.currentTimeMillis(), "25/01/2025", false));

        schoolTaskList.add(new Task(UUID.randomUUID().toString(), "Science Project", "2:00 PM", "26",
                "January", "Medium", "School", true, 2025, 0, System.currentTimeMillis(), "26/01/2025", false));

        schoolTaskList.add(new Task(UUID.randomUUID().toString(), "Physics Lab Report", "11:00 AM", "1",
                "February", "High", "School", false, 2025, 0, System.currentTimeMillis(), "01/02/2025", false));

        schoolTaskList.add(new Task(UUID.randomUUID().toString(), "History Assignment", "9:30 AM", "2",
                "February", "Medium", "School", false, 2025, 0, System.currentTimeMillis(), "02/02/2025", false));

        schoolTaskList.add(new Task(UUID.randomUUID().toString(), "Group Project Meeting", "3:00 PM", "3",
                "February", "Low", "School", true, 2025, 0, System.currentTimeMillis(), "03/02/2025", false));

        schoolTaskList.add(new Task(UUID.randomUUID().toString(), "Biology Quiz Preparation", "4:00 PM", "4",
                "February", "Medium", "School", false, 2025, 0, System.currentTimeMillis(), "04/02/2025", false));

        schoolTaskList.add(new Task(UUID.randomUUID().toString(), "Chemistry Lab Prep", "10:00 AM", "5",
                "February", "High", "School", true, 2025, 0, System.currentTimeMillis(), "05/02/2025", false));

        schoolTaskList.add(new Task(UUID.randomUUID().toString(), "Essay Submission", "1:00 PM", "6",
                "February", "High", "School", false, 2025, 0, System.currentTimeMillis(), "06/02/2025", false));

        schoolTaskList.add(new Task(UUID.randomUUID().toString(), "Math Revision", "10:30 AM", "7",
                "February", "Medium", "School", false, 2025, 0, System.currentTimeMillis(), "07/02/2025", false));

        schoolTaskList.add(new Task(UUID.randomUUID().toString(), "Sports Practice", "5:00 PM", "7",
                "February", "Low", "School", false, 2025, 0, System.currentTimeMillis(), "07/02/2025", false));

        schoolTaskList.add(new Task(UUID.randomUUID().toString(), "Computer Science Coding Assignment", "2:00 PM", "8",
                "February", "High", "School", false, 2025, 0, System.currentTimeMillis(), "08/02/2025", false));

        schoolTaskList.add(new Task(UUID.randomUUID().toString(), "Art Project Presentation", "12:00 PM", "9",
                "February", "Medium", "School", true, 2025, 0, System.currentTimeMillis(), "09/02/2025", false));


// Home tasks
        homeTaskList.add(new Task(UUID.randomUUID().toString(), "Clean Kitchen", "8:00 PM", "27",
                "January", "Low", "Home", false, 2025, 0, System.currentTimeMillis(), "27/01/2025", false));

        homeTaskList.add(new Task(UUID.randomUUID().toString(), "Buy Groceries", "6:00 PM", "28",
                "January", "High", "Home", true, 2025, 0, System.currentTimeMillis(), "28/01/2025", false));

        homeTaskList.add(new Task(UUID.randomUUID().toString(), "Laundry", "9:00 AM", "1",
                "February", "Medium", "Home", false, 2025, 0, System.currentTimeMillis(), "01/02/2025", false));

        homeTaskList.add(new Task(UUID.randomUUID().toString(), "Pay Bills", "10:00 AM", "2",
                "February", "High", "Home", true, 2025, 0, System.currentTimeMillis(), "02/02/2025", false));

        homeTaskList.add(new Task(UUID.randomUUID().toString(), "Meal Prep", "5:30 PM", "3",
                "February", "Medium", "Home", false, 2025, 0, System.currentTimeMillis(), "03/02/2025", false));

        homeTaskList.add(new Task(UUID.randomUUID().toString(), "Organize Closet", "11:00 AM", "4",
                "February", "Low", "Home", false, 2025, 0, System.currentTimeMillis(), "04/02/2025", false));

        homeTaskList.add(new Task(UUID.randomUUID().toString(), "Repair Leaky Faucet", "2:30 PM", "5",
                "February", "High", "Home", true, 2025, 0, System.currentTimeMillis(), "05/02/2025", false));

        homeTaskList.add(new Task(UUID.randomUUID().toString(), "Take Dog for a Walk", "7:00 AM", "6",
                "February", "Low", "Home", false, 2025, 0, System.currentTimeMillis(), "06/02/2025", false));

        homeTaskList.add(new Task(UUID.randomUUID().toString(), "Wash Car", "10:00 AM", "7",
                "February", "Medium", "Home", false, 2025, 0, System.currentTimeMillis(), "07/02/2025", false));

        homeTaskList.add(new Task(UUID.randomUUID().toString(), "Clean Living Room", "1:00 PM", "8",
                "February", "Low", "Home", false, 2025, 0, System.currentTimeMillis(), "08/02/2025", false));

        homeTaskList.add(new Task(UUID.randomUUID().toString(), "Grocery Shopping", "4:00 PM", "9",
                "February", "Medium", "Home", true, 2025, 0, System.currentTimeMillis(), "09/02/2025", false));

        homeTaskList.add(new Task(UUID.randomUUID().toString(), "Cook Dinner", "6:30 PM", "9",
                "February", "High", "Home", false, 2025, 0, System.currentTimeMillis(), "09/02/2025", false));


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
            return "School"; // ✅ Only school tasks
        } else if (houseToggleButton.isSelected()) {
            return "Home"; // ✅ Only home tasks
        } else {
            return "Both"; // ✅ Home + School tasks
        }
    }


    private void updateCurrentTasks() {
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
    }
}
