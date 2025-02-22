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

public class SchoolCalendarFragment extends Fragment implements TaskAdapter.OnTaskDeletedListener, TaskAdapter.OnTaskEditedListener  {

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

        View view = inflater.inflate(R.layout.fragment_school_calendar_screen, container, false);

    calendar = Calendar.getInstance();
    taskList = new ArrayList<>();
    loadDummyTasks();

    calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
    taskRecyclerView = view.findViewById(R.id.taskRecyclerView);
    weeklyTaskRecyclerView = view.findViewById(R.id.weeklyTaskRecyclerView);
    monthYearDropdown = view.findViewById(R.id.monthYearDropdown);
    TextView tasksDueThisWeekTitle = view.findViewById(R.id.tasksDueThisWeekTitle);
    ImageView previousMonth = view.findViewById(R.id.previousMonth);
    ImageView nextMonth = view.findViewById(R.id.nextMonth);
    TextView selectedDateText = view.findViewById(R.id.selectedDateText);

    homeCalendarButton = view.findViewById(R.id.homeCalendarButton);
    schoolCalendarButton = view.findViewById(R.id.SchoolCalendarButton);
    setActiveButton(schoolCalendarButton, homeCalendarButton);

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

    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getMonthYearList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthYearDropdown.setAdapter(adapter);

        monthYearDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            if (v instanceof TextView) {
                ((TextView) v).setTextColor(Color.BLACK);
            }
            calendar.set(Calendar.MONTH, position);
            updateCalendar();
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    });

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

    int todayDay = calendar.get(Calendar.DAY_OF_MONTH);
    int todayMonth = calendar.get(Calendar.MONTH);
    int todayYear = calendar.get(Calendar.YEAR);

        monthYearDropdown.setSelection(todayMonth);

    calendarDates = getCalendarDates(todayYear, todayMonth);
    calendarAdapter = new CalendarAdapter(
            calendarDates,
            selectedDate -> {
        int selectedDay = Integer.parseInt(selectedDate);
        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
        List<Task> selectedDateTasks = filterTasksByDateBasedOnCategory(selectedDay, "School");
        taskAdapter.updateTasks(selectedDateTasks);
        updateTasksTitle(selectedDateTasks, selectedDay);
        updateWeeklyTasks();
    },
    getSchoolTaskDates(),
    getHomeTaskDates(),
                "School"
                        );

        calendarRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);

        calendarAdapter.setSelectedDate(String.valueOf(todayDay));

    updateSelectedDateText(selectedDateText, todayDay);

    taskAdapter = new TaskAdapter(new ArrayList<>(), requireContext(), this, this, editTaskLauncher);
    weeklyTaskAdapter = new TaskAdapter(new ArrayList<>(), requireContext(), this, this, editTaskLauncher);

        taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskRecyclerView.setAdapter(taskAdapter);

        weeklyTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        weeklyTaskRecyclerView.setAdapter(weeklyTaskAdapter);

    updateTasksForToday(todayDay);
    updateWeeklyTasks();

    ImageView profileIcon = view.findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(v -> {
        Intent intent = new Intent(requireContext(), ProfileActivity.class);
        startActivity(intent);
    });

        return view;
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
    @Override
    public void onTaskDeleted(Task task) {
        taskList.removeIf(t -> t.getId().equals(task.getId()));
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
        updateWeeklyTasks();
        taskAdapter.notifyDataSetChanged();
    }

    private Set<String> getSchoolTaskDates() {
        Set<String> schoolTaskDates = new HashSet<>();
        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        int currentYear = calendar.get(Calendar.YEAR);
        for (Task t : taskList) {
            if ("School".equals(t.getCategory())
                    && t.getMonth().equalsIgnoreCase(currentMonth)
                    && t.getYear() == currentYear) {
                schoolTaskDates.add(t.getDate());
            }
        }
        return schoolTaskDates;
    }

    private Set<String> getHomeTaskDates() {
        Set<String> homeTaskDates = new HashSet<>();
        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        int currentYear = calendar.get(Calendar.YEAR);
        for (Task t : taskList) {
            if ("Home".equals(t.getCategory())
                    && t.getMonth().equalsIgnoreCase(currentMonth)
                    && t.getYear() == currentYear) {
                homeTaskDates.add(t.getDate());
            }
        }
        return homeTaskDates;
    }

    private List<Task> filterTasksByDateBasedOnCategory(int day, String category) {
        List<Task> filteredTasks = new ArrayList<>();
        Log.d("filterTasksByDate", "Filtering for day: " + day + " and category: " + category);

        for (Task task : taskList) {
            Log.d("filterTasksByDate", "Checking task: " + task.getTitle() + ", Date: " + task.getDate() + ", Category: " + task.getCategory());

            if (Integer.parseInt(task.getDate()) == day && task.getCategory().equals(category)) {
                filteredTasks.add(task);
            }
        }

        Log.d("filterTasksByDate", "Filtered tasks count: " + filteredTasks.size());
        return filteredTasks;
    }


    @Override
    public void onTaskEdited(Task updatedTask, int position) {
        Log.d("onTaskEdited", "Updating task: " + updatedTask.getTitle());
        updateExistingTask(updatedTask);
    }

    /**
     * Updates an existing task in the main taskList by matching IDs.
     */
    private void updateExistingTask(Task updatedTask) {
        if (updatedTask.getId() == null) {
            Log.e("updateExistingTask", "Updated task ID is null. Cannot update by id.");
            return;
        }
        boolean taskFound = false;
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getId().equals(updatedTask.getId())) {
                taskList.set(i, updatedTask);
                taskFound = true;
                Log.d("updateExistingTask", "Task updated: " + updatedTask.getTitle());
                break;
            }
        }
        if (!taskFound) {
            Log.e("updateExistingTask", "Task not found for update: " + updatedTask.getTitle());
        }
        updateCalendar();
        updateWeeklyTasks();
        taskAdapter.notifyDataSetChanged();
    }

    /**
     * Called from the editTask activity result.
     * Now, we always update by ID instead of using the adapter position.
     */
    private final ActivityResultLauncher<Intent> editTaskLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task updatedTask = result.getData().getParcelableExtra("updatedTask");
                    if (updatedTask != null) {
                        Log.d("editTaskLauncher", "Received updated task: " + updatedTask.getTitle());
                        updateExistingTask(updatedTask);
                    }
                }
            });

    private void updateTasksForToday(int day) {
        List<Task> todayTasks = new ArrayList<>();
        for (Task t : taskList) {
            if (Integer.parseInt(t.getDate()) == day && t.getCategory().equals("School")) {
                todayTasks.add(t);
            }
        }
        taskAdapter.updateTasks(todayTasks);
        taskAdapter.notifyDataSetChanged();
    }

    private void updateWeeklyTasks() {
        if (getView() == null) {
            Log.e("updateWeeklyTasks", "View is null, skipping UI updates.");
            return;
        }

        String currentDateString = calendar.get(Calendar.DAY_OF_MONTH) + "/"
                + (calendar.get(Calendar.MONTH) + 1) + "/"
                + calendar.get(Calendar.YEAR);

        List<Task> weeklyTasks = filterTasksByWeek(currentDateString);

        // ✅ Sort tasks by priority: "High" → "Medium" → "Low"
        weeklyTasks.sort((t1, t2) -> {
            return getPriorityValue(t1.getPriority()) - getPriorityValue(t2.getPriority());
        });

        View tasksTitle = getView().findViewById(R.id.tasksDueThisWeekTitle);
        View weeklyRecyclerView = getView().findViewById(R.id.weeklyTaskRecyclerView);

        if (weeklyTasks.isEmpty()) {
            Log.d("updateWeeklyTasks", "No tasks this week. Hiding section.");
            tasksTitle.setVisibility(View.GONE);
            weeklyRecyclerView.setVisibility(View.GONE);
        } else {
            Log.d("updateWeeklyTasks", "Tasks found. Showing section.");
            tasksTitle.setVisibility(View.VISIBLE);
            weeklyRecyclerView.setVisibility(View.VISIBLE);
            weeklyTaskAdapter.updateTasks(weeklyTasks);
            weeklyTaskAdapter.notifyDataSetChanged();
        }
    }

    private int getPriorityValue(String priority) {
        switch (priority) {
            case "High":
                return 1; // 🔴 High priority (red) → First
            case "Medium":
                return 2; // 🟡 Medium priority (yellow) → Second
            case "Low":
                return 3; // 🟢 Low priority (blue) → Last
            default:
                return 4; // Anything else (should not happen)
        }
    }



    public void addTaskToCalendar(String title, String priority, String date, String time, boolean remind, String taskType) {
        if (taskList == null) {
            taskList = new ArrayList<>();
        }
        String taskId = UUID.randomUUID().toString();
        String[] dateParts = date.split("/");
        if (dateParts.length != 3) {
            Log.e("addTaskToCalendar", "Invalid date format: " + date);
            Toast.makeText(requireContext(), "Invalid date format!", Toast.LENGTH_SHORT).show();
            return;
        }
        String day = dateParts[0];
        String month = dateParts[1];
        int year = Integer.parseInt(dateParts[2]);

        Task newTask = new Task(
                taskId,
                title,
                time,
                day,
                getMonthYearList().get(Integer.parseInt(month) - 1),
                priority,
                taskType,
                remind,
                year
        );
        taskList.add(newTask);
        int dayInt = Integer.parseInt(day);
        String monthName = getMonthYearList().get(Integer.parseInt(month) - 1);
        List<Task> selectedDateTasks = filterTasksByDate(dayInt, monthName);
        taskAdapter.updateTasks(selectedDateTasks);
        updateWeeklyTasks();
        Toast.makeText(requireContext(), "Task added: " + title, Toast.LENGTH_SHORT).show();
    }

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

    private void updateCalendar() {
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        Set<String> taskDates = getSchoolTaskDates();
        calendarAdapter.updateSchoolTaskDates(taskDates);
        calendarAdapter.updateData(calendarDates);
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
        updateWeeklyTasks();
    }

    private List<String> getCalendarDates(int year, int month) {
        List<String> dates = new ArrayList<>();
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.set(year, month, 1);
        int firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < firstDayOfWeek; i++) {
            dates.add("");
        }
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
            for (Task t : taskList) {
                if (Integer.parseInt(t.getDate()) == day && t.getMonth().equals(month)) {
                    filteredTasks.add(t);
                }
            }
        } catch (NumberFormatException e) {
            Log.e("filterTasksByDate", "Error parsing task date: " + e.getMessage());
        }
        return filteredTasks;
    }

    private List<Task> filterTasksByWeek(String dateString) {
        List<Task> weeklyTasks = new ArrayList<>();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            LocalDate selectedDate = LocalDate.parse(dateString, formatter);
            int selectedWeekOfYear = selectedDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            int selectedYear = selectedDate.getYear(); // ✅ Track year
            int selectedMonth = selectedDate.getMonthValue(); // ✅ Track month

            for (Task t : taskList) {
                String taskFullDate = t.getDate() + "/" + (getMonthIndex(t.getMonth()) + 1) + "/" + t.getYear();
                LocalDate taskDate = LocalDate.parse(taskFullDate, formatter);
                int taskWeekOfYear = taskDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);

                // ✅ Ensure task is in the same week, month, and year
                if (taskWeekOfYear == selectedWeekOfYear && taskDate.getYear() == selectedYear && taskDate.getMonthValue() == selectedMonth) {
                    weeklyTasks.add(t);
                }
            }
        } catch (DateTimeParseException e) {
            Log.e("WeeklyTasks", "Error parsing date: " + e.getMessage());
        }

        return weeklyTasks;
    }



    private int getMonthIndex(String month) {
        return getMonthYearList().indexOf(month);
    }

    private String getOrdinalSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    private void loadDummyTasks() {
        taskList.add(new Task(UUID.randomUUID().toString(), "Complete Assignment", "9:00 AM", "24", "January", "High", "School", true, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(), "Prepare Presentation", "10:00 AM", "25", "January", "High", "School", false, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(), "Submit Assignment", "11:59 PM", "26", "January", "High", "School", true, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(), "Attend Workshop", "2:00 PM", "27", "January", "Low", "School", false, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(), "Complete Lab Report", "3:00 PM", "28", "January", "High", "School", true, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(), "Group Study Session", "5:00 PM", "29", "January", "Medium", "School", false, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(), "Faculty Meeting", "1:00 PM", "30", "January", "Low", "School", false, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(), "Prepare Notes", "4:00 PM", "31", "January", "Medium", "School", true, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(), "Join Webinar", "9:00 AM", "1", "February", "Medium", "School", true, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(), "Library Visit", "10:00 AM", "2", "February", "Low", "School", false, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(), "Schedule Exam", "11:00 AM", "3", "February", "High", "School", true, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(), "Plan Project", "1:00 PM", "4", "February", "Medium", "School", false, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(), "Team Presentation", "3:00 PM", "5", "February", "High", "School", true, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(), "Mock Exam", "10:00 AM", "6", "February", "Low", "School", false, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(), "Classroom Discussion", "2:00 PM", "7", "February", "Medium", "School", false, 2025));
    }

    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(requireContext().getResources().getColor(android.R.color.white));
        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(requireContext().getResources().getColor(R.color.dark_blue));
    }
}
