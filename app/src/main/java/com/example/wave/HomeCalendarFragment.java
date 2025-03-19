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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HomeCalendarFragment extends Fragment implements TaskAdapter.OnTaskDeletedListener,
        TaskAdapter.OnTaskEditedListener, TaskAdapter.OnTaskCompletedListener {

    private RecyclerView calendarRecyclerView, taskRecyclerView, weeklyTaskRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TaskAdapter taskAdapter, weeklyTaskAdapter;
    private List<String> calendarDates;
    private List<Task> taskList;
    private Calendar calendar;
    private Spinner monthYearDropdown;
    private TextView homeCalendarButton, schoolCalendarButton; // Toggle buttons

    public static final int REQUEST_EDIT_TASK = 1001;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Use a snapshot listener for real-time updates
    private ListenerRegistration tasksListener;

    // ActivityResultLauncher for editing tasks
    private final ActivityResultLauncher<Intent> editTaskLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task updatedTask = result.getData().getParcelableExtra("updatedTask");
                    if (updatedTask != null) {
                        updateExistingTask(updatedTask);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_house_calendar_screen, container, false);

        // Initialize calendar and task list
        calendar = Calendar.getInstance();
        taskList = new ArrayList<>();

        // Initialize views
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        taskRecyclerView = view.findViewById(R.id.taskRecyclerView);
        weeklyTaskRecyclerView = view.findViewById(R.id.weeklyTaskRecyclerView);
        monthYearDropdown = view.findViewById(R.id.monthYearDropdown);
        TextView tasksDueThisWeekTitle = view.findViewById(R.id.tasksDueThisWeekTitle);
        ImageView previousMonth = view.findViewById(R.id.previousMonth);
        ImageView nextMonth = view.findViewById(R.id.nextMonth);
        TextView selectedDateText = view.findViewById(R.id.selectedDateText);

        // Set today's date on the header
        int todayDay = calendar.get(Calendar.DAY_OF_MONTH);
        int todayMonth = calendar.get(Calendar.MONTH);
        updateSelectedDateText(selectedDateText, todayDay);

        // Toggle buttons
        homeCalendarButton = view.findViewById(R.id.homeCalendarButton);
        schoolCalendarButton = view.findViewById(R.id.SchoolCalendarButton);
        setActiveButton(homeCalendarButton, schoolCalendarButton);
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

        // Setup month/year spinner
        MonthYearSpinnerAdapter spinnerAdapter = new MonthYearSpinnerAdapter(requireContext(), getMonthYearList());
        spinnerAdapter.setDropDownViewResource(R.layout.month_year_spinner_dropdown_item);
        monthYearDropdown.setAdapter(spinnerAdapter);
        monthYearDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (v instanceof TextView) ((TextView) v).setTextColor(Color.BLACK);
                calendar.set(Calendar.MONTH, position);
                updateCalendar();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
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
        monthYearDropdown.setSelection(todayMonth);

        // Initialize calendar adapter (for Home tasks)
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        calendarAdapter = new CalendarAdapter(
                calendarDates,
                selectedDate -> {
                    int selectedDay = Integer.parseInt(selectedDate);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    List<Task> selectedDateTasks = filterTasksByDateBasedOnCategory(selectedDay, "Home");
                    taskAdapter.updateTasks(selectedDateTasks);
                    updateTasksTitle(selectedDateTasks, selectedDay);
                    updateWeeklyTasks();
                    ImageView emptyTasksImage = getView().findViewById(R.id.emptyTasksImage);
                    if (selectedDateTasks.isEmpty()) {
                        emptyTasksImage.setVisibility(View.VISIBLE);
                    } else {
                        emptyTasksImage.setVisibility(View.GONE);
                    }
                },
                new HashSet<>(), // Initially empty home task dates
                new HashSet<>(), // School task dates (unused here)
                "Home"
        );
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);
        calendarAdapter.setSelectedDate(String.valueOf(todayDay));

        // Initialize task adapters
        taskAdapter = new TaskAdapter(new ArrayList<>(), requireContext(), this, this, this, editTaskLauncher);
        weeklyTaskAdapter = new TaskAdapter(new ArrayList<>(), requireContext(), this, this, this, editTaskLauncher);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskRecyclerView.setAdapter(taskAdapter);
        weeklyTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        weeklyTaskRecyclerView.setAdapter(weeklyTaskAdapter);

        // Profile icon click listener
        ImageView profileIcon = view.findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProfileActivity.class);
            startActivity(intent);
        });
        return view;
    }

    // Start snapshot listener for home tasks in onStart
    @Override
    public void onStart() {
        super.onStart();
        startTaskListener();
    }

    // Remove snapshot listener in onStop
    @Override
    public void onStop() {
        super.onStop();
        if (tasksListener != null) {
            tasksListener.remove();
            tasksListener = null;
        }
    }

    private void startTaskListener() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;
        tasksListener = db.collection("users")
                .document(userId)
                .collection("housetasks")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Snapshot listener error", e);
                        return;
                    }
                    if (querySnapshot != null) {
                        taskList.clear();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Task task = doc.toObject(Task.class);
                            if (!task.isCompleted()) {
                                taskList.add(task);
                            }
                        }

                        // Update the calendar highlights
                        updateCalendar();

                        // --- Force the same logic as if the user clicked "today" ---
                        int today = calendar.get(Calendar.DAY_OF_MONTH);
                        calendar.set(Calendar.DAY_OF_MONTH, today);

                        List<Task> selectedDateTasks = filterTasksByDateBasedOnCategory(today, "Home");
                        taskAdapter.updateTasks(selectedDateTasks);
                        updateTasksTitle(selectedDateTasks, today);
                        updateWeeklyTasks();

                        ImageView emptyTasksImage = getView().findViewById(R.id.emptyTasksImage);
                        if (selectedDateTasks.isEmpty()) {
                            emptyTasksImage.setVisibility(View.VISIBLE);
                        } else {
                            emptyTasksImage.setVisibility(View.GONE);
                        }
                    }
                });

    }

    // Utility methods
    private void updateSelectedDateText(TextView selectedDateText, int day) {
        String monthName = getMonthYearList().get(calendar.get(Calendar.MONTH));
        String formattedDate = getFormattedDayOfWeek(calendar) + " " + day + getOrdinalSuffix(day) + " " + monthName;
        selectedDateText.setText(formattedDate);
    }

    private String getFormattedDayOfWeek(Calendar calendar) {
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, java.util.Locale.getDefault());
    }

    private List<String> getRepeatedDates(String startDate, Task.RepeatOption repeatOption) {
        List<String> repeatedDates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        LocalDate startLocalDate = LocalDate.parse(startDate, formatter);

        // If the user chose "Does not repeat," just return the single date:
        if (repeatOption == Task.RepeatOption.DOES_NOT_REPEAT) {
            repeatedDates.add(startDate); // Just store exactly what they typed
            return repeatedDates;
        }

        // Otherwise, handle the day-of-week logic:
        DayOfWeek dayOfWeek = DayOfWeek.SATURDAY; // Example for Saturday
        // Or map each REPEAT_EVERY_xxx to the correct DayOfWeek

        LocalDate firstOccurrence = startLocalDate.with(
                TemporalAdjusters.firstInMonth(dayOfWeek));

        while (firstOccurrence.getMonthValue() == startLocalDate.getMonthValue()
                && firstOccurrence.getYear() == startLocalDate.getYear()) {
            repeatedDates.add(firstOccurrence.format(formatter));
            firstOccurrence = firstOccurrence.with(TemporalAdjusters.next(dayOfWeek));
        }

        return repeatedDates;
    }


    // --- UPDATED onTaskDeleted() method for SchoolCalendarFragment ---
    @Override
    public void onTaskDeleted(Task task) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot delete task");
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Archive the task by writing it to the "cancelledTasks" collection
        db.collection("users")
                .document(userId)
                .collection("cancelledHomeTasks")
                .document(task.getId())
                .set(task)
                .addOnSuccessListener(aVoid -> {
                    // After archiving, delete the task from the original collection
                    db.collection("users")
                            .document(userId)
                            .collection("housetasks")
                            .document(task.getId())
                            .delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d("Firestore", "Task archived and deleted: " + task.getTitle());
                                // Update the UI after deletion
                                updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
                                updateWeeklyTasks();
                                updateCalendar();
                                // Refresh the Cancelled Tasks Card count
                                updateCancelledTasksCount();
                            })
                            .addOnFailureListener(e -> Log.e("Firestore", "Error deleting task", e));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error archiving task", e));
    }


    // --- method to update the Cancelled Tasks Card ---
    private void updateCancelledTasksCount() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("cancelledHomeTasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int cancelledCount = queryDocumentSnapshots.size();
                    // Assuming the TextView for cancelled tasks count has the id "tasks_cancelled_count"
                    TextView cancelledCountTextView = getView().findViewById(R.id.tasks_cancelled_count);
                    if (cancelledCountTextView != null) {
                        cancelledCountTextView.setText(String.valueOf(cancelledCount));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching cancelled tasks", e));
    }


    // Return a set of dates (as strings) that have tasks for Home
    private Set<String> getHomeTaskDates() {
        Set<String> homeTaskDates = new HashSet<>();
        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        int currentYear = calendar.get(Calendar.YEAR);
        for (Task t : taskList) {
            if ("Home".equals(t.getCategory())
                    && t.getMonth().equalsIgnoreCase(currentMonth)
                    && t.getYear() == currentYear
                    && !t.isCompleted()) {
                String dateStr = t.getDate();
                if (dateStr.contains("/")) {
                    String[] dateParts = dateStr.split("/");
                    if (dateParts.length == 3) {
                        homeTaskDates.add(dateParts[0]); // Only the day portion
                    }
                } else {
                    homeTaskDates.add(dateStr);
                }
            }
        }
        return homeTaskDates;
    }


    // Filter tasks for a given day and category
    private List<Task> filterTasksByDateBasedOnCategory(int day, String category) {
        if (!"Home".equals(category)) {
            Log.e("filterTasksByDateBasedOnCategory", "Invalid category: " + category);
            return new ArrayList<>();
        }

        List<Task> filteredTasks = new ArrayList<>();
        for (Task t : taskList) {
            if (!"Home".equals(t.getCategory())) continue;

            String dateStr = t.getDate();
            if (!dateStr.contains("/")) {
                dateStr = dateStr + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);
            }
            String[] dateParts = dateStr.split("/");
            if (dateParts.length != 3) {
                Log.e("filterTasksByDateBasedOnCategory", "Invalid date format for task: " + t.getDate());
                continue;
            }

            try {
                int taskDay = Integer.parseInt(dateParts[0]);
                if (taskDay == day) {
                    filteredTasks.add(t);
                }
            } catch (NumberFormatException e) {
                Log.e("filterTasksByDateBasedOnCategory", "Error parsing day from date: " + t.getDate(), e);
            }
        }
        return filteredTasks;
    }


    // onTaskEdited: update Firestore; listener will update UI
    @Override
    public void onTaskEdited(Task updatedTask, int position) {
        updateExistingTask(updatedTask);
    }

    private void updateExistingTask(Task updatedTask) {
        if (updatedTask.getId() == null) {
            Log.e("updateExistingTask", "Updated task ID is null. Cannot update.");
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot update task");
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("users")
                .document(userId)
                .collection("housetasks")
                .document(updatedTask.getId())
                .set(updatedTask)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Task successfully updated"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating task", e));
    }

    // onTaskCompleted: update Firestore; the snapshot listener will reflect changes in UI
    @Override
    public void onTaskCompleted(Task task) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot complete task");
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("TaskCompletion", "Marking home task as completed: " + task.getTitle());
        db.collection("users")
                .document(userId)
                .collection("housetasks")
                .document(task.getId())
                .update("completed", true)
                .addOnSuccessListener(aVoid -> {
                    // No need to manually update UI here; the snapshot listener will update taskList
                    Log.d("TaskCompletion", "Home task completed. Snapshot listener will update UI.");
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating home task completion", e));
    }

    // Update the daily tasks and label for a given day
    public void updateTasksForToday(int day) {
        List<Task> todayTasks = new ArrayList<>();
        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        int currentYear = calendar.get(Calendar.YEAR);

        for (Task t : taskList) {
            // Get the task’s date string; if it does not contain "/" assume it’s only the day.
            String dateStr = t.getDate();
            if (!dateStr.contains("/")) {
                dateStr = dateStr + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + currentYear;
            }
            String[] dateParts = dateStr.split("/");
            if (dateParts.length != 3) {
                Log.e("TaskFilter", "Invalid date format for task: " + t.getDate());
                continue;
            }
            int taskDay, taskMonth, taskYear;
            try {
                taskDay = Integer.parseInt(dateParts[0]);
                taskMonth = Integer.parseInt(dateParts[1]) - 1; // convert to 0-based month index
                taskYear = Integer.parseInt(dateParts[2]);
            } catch (NumberFormatException e) {
                Log.e("TaskFilter", "Error parsing date from task: " + t.getDate(), e);
                continue;
            }

            // Only add tasks that match today's day, month, and year and belong to "Home"
            if (taskDay == day && taskMonth == calendar.get(Calendar.MONTH)
                    && taskYear == currentYear && "Home".equals(t.getCategory())
                    && !t.isCompleted()) {
                todayTasks.add(t);
            }
        }

        taskAdapter.updateTasks(todayTasks);
        taskAdapter.notifyDataSetChanged();
        updateTasksTitle(todayTasks, day);

        ImageView emptyTasksImage = getView().findViewById(R.id.emptyTasksImage);
        emptyTasksImage.setVisibility(todayTasks.isEmpty() ? View.VISIBLE : View.GONE);
    }



    // Update the weekly tasks section
    private void updateWeeklyTasks() {
        if (getView() == null) {
            Log.e("updateWeeklyTasks", "View is null, skipping UI update.");
            return;
        }

        // Formatter for full date (d/M/yyyy)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        String currentDateString = calendar.get(Calendar.DAY_OF_MONTH) + "/"
                + (calendar.get(Calendar.MONTH) + 1) + "/"
                + calendar.get(Calendar.YEAR);

        LocalDate currentDate;
        try {
            currentDate = LocalDate.parse(currentDateString, formatter);
        } catch (DateTimeParseException e) {
            Log.e("updateWeeklyTasks", "Error parsing current date: " + e.getMessage());
            return;
        }

        int currentWeek = currentDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        List<Task> weeklyTasks = new ArrayList<>();

        for (Task t : taskList) {
            if (!"Home".equals(t.getCategory()) || t.isCompleted()) continue;

            String dateStr = t.getDate();
            if (!dateStr.contains("/")) {
                dateStr = dateStr + "/" + (getMonthIndex(t.getMonth()) + 1) + "/" + t.getYear();
            }
            try {
                LocalDate taskDate = LocalDate.parse(dateStr, formatter);
                int taskWeek = taskDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                if (taskWeek == currentWeek
                        && taskDate.getYear() == currentDate.getYear()
                        && taskDate.getMonthValue() == currentDate.getMonthValue()) {
                    weeklyTasks.add(t);
                }
            } catch (DateTimeParseException e) {
                Log.e("updateWeeklyTasks", "Error parsing task date: " + t.getDate() + ", " + e.getMessage());
            }
        }

        // Sort weekly tasks by priority (lower number means higher priority)
        weeklyTasks.sort((t1, t2) -> getPriorityValue(t1.getPriority()) - getPriorityValue(t2.getPriority()));

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

    private Task.RepeatOption getRepeatOptionFromString(String repeatOptionString) {
        switch (repeatOptionString) {
            case "Repeat every Monday":
                return Task.RepeatOption.REPEAT_EVERY_MONDAY;
            case "Repeat every Tuesday":
                return Task.RepeatOption.REPEAT_EVERY_TUESDAY;
            case "Repeat every Wednesday":
                return Task.RepeatOption.REPEAT_EVERY_WEDNESDAY;
            case "Repeat every Thursday":
                return Task.RepeatOption.REPEAT_EVERY_THURSDAY;
            case "Repeat every Friday":
                return Task.RepeatOption.REPEAT_EVERY_FRIDAY;
            case "Repeat every Saturday":
                return Task.RepeatOption.REPEAT_EVERY_SATURDAY;
            case "Repeat every Sunday":
                return Task.RepeatOption.REPEAT_EVERY_SUNDAY;
            default:
                return Task.RepeatOption.DOES_NOT_REPEAT;
        }
    }

    // Add a new task to Firestore; the listener will update UI
    public void addTaskToCalendar(String title, String priority, String date, String time, boolean remind, String taskType, String repeatOptionString) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert the repeat option string to the RepeatOption enum.
        Task.RepeatOption repeatOption = getRepeatOptionFromString(repeatOptionString);

        // Get the repeated dates for the current month.
        List<String> repeatedDates = getRepeatedDates(date, repeatOption);

        for (String repeatedDate : repeatedDates) {
            String taskId = UUID.randomUUID().toString();
            String[] dateParts = repeatedDate.split("/");
            int year = Integer.parseInt(dateParts[2]);

            // Create a new Task.
            // For Home tasks, we store the full date string in the 'full date' field.
            Task newTask = new Task(
                    taskId,
                    title,
                    time,
                    dateParts[0], // You can store just the day for quick filtering if needed.
                    getMonthYearList().get(Integer.parseInt(dateParts[1]) - 1),
                    priority,
                    taskType,
                    remind,
                    year,
                    0, // Default stability value
                    System.currentTimeMillis(),
                    repeatedDate, // Full date string (e.g., "8/3/2025")
                    false,
                    repeatOption
            );

            // Save the task to the "housetasks" collection.
            db.collection("users")
                    .document(userId)
                    .collection("housetasks")
                    .document(taskId)
                    .set(newTask)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Task successfully added!");
                        if (newTask.isRemind()) {
                            ReminderManager.scheduleReminder(requireContext(), newTask);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error adding task", e);
                        Toast.makeText(requireContext(), "Error adding task", Toast.LENGTH_SHORT).show();
                    });
        }
    }



    // Update the title above daily tasks based on tasks for the selected date
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

    // Update the calendar (dates, highlights, and selection)
    private void updateCalendar() {
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        Set<String> taskDates = getHomeTaskDates();
        calendarAdapter.updateHomeTaskDates(taskDates);
        calendarAdapter.updateData(calendarDates);
        int todayDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendarAdapter.setSelectedDate(String.valueOf(todayDay));
        updateTasksForToday(todayDay);
        updateWeeklyTasks();
    }



    // Utility methods
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

    private List<String> getMonthYearList() {
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

    private int getPriorityValue(String priority) {
        switch (priority) {
            case "High": return 1;
            case "Medium": return 2;
            case "Low": return 3;
            default: return 4;
        }
    }

    private List<Task> filterTasksByWeek(String dateString) {
        List<Task> weeklyTasks = new ArrayList<>();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            LocalDate selectedDate = LocalDate.parse(dateString, formatter);
            int selectedWeekOfYear = selectedDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            int selectedYear = selectedDate.getYear();
            int selectedMonth = selectedDate.getMonthValue();
            for (Task t : taskList) {
                String taskFullDate = t.getDate() + "/" + (getMonthIndex(t.getMonth()) + 1) + "/" + t.getYear();
                LocalDate taskDate = LocalDate.parse(taskFullDate, formatter);
                int taskWeekOfYear = taskDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                if (taskWeekOfYear == selectedWeekOfYear &&
                        taskDate.getYear() == selectedYear &&
                        taskDate.getMonthValue() == selectedMonth) {
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
        if (day >= 11 && day <= 13) return "th";
        switch (day % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    public void updateTasks(List<Task> tasks) {
        taskList.clear();
        taskList.addAll(tasks);
        updateCalendar(); // This will update the calendar highlights
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH)); // And refresh today's tasks list
    }


    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        // Mark them as selected/deselected
        activeButton.setSelected(true);
        inactiveButton.setSelected(false);

        // Then style them
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(requireContext().getResources().getColor(android.R.color.white));
        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(requireContext().getResources().getColor(R.color.dark_blue));
    }
}
