package com.example.wave;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CombinedCalendarFragment extends Fragment implements
        TaskAdapter.OnTaskDeletedListener,
        TaskAdapter.OnTaskEditedListener,
        TaskAdapter.OnTaskCompletedListener {

    // UI elements and adapters
    private RecyclerView calendarRecyclerView, taskRecyclerView, weeklyTaskRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TaskAdapter taskAdapter, weeklyTaskAdapter;
    private List<String> calendarDates;
    // Separate lists for each category, plus a combined list
    private List<Task> schoolTaskList, homeTaskList, combinedTaskList;
    private Calendar calendar;
    private Spinner monthYearDropdown;
    // Toggle buttons for combined view
    private TextView schoolToggleButton, bothToggleButton, houseToggleButton;
    // Additional toggles for switching fragments (if used)
    private TextView homeCalendarButton, schoolCalendarButton;
    public static final int REQUEST_EDIT_TASK = 1001;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Snapshot listeners for real-time updates
    private ListenerRegistration schoolListener, homeListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
        TextView selectedDateText = view.findViewById(R.id.selectedDateText);
        updateSelectedDateText(selectedDateText, calendar.get(Calendar.DAY_OF_MONTH));

        // Setup fragment toggles (if using separate fragments)
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
// Find Previous and Next Month Arrows
        ImageView previousMonth = view.findViewById(R.id.previousMonth);
        ImageView nextMonth = view.findViewById(R.id.nextMonth);

// Handle previous month click
        // Handle previous month click
        previousMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1); // Move to previous month
            calendar.set(Calendar.DAY_OF_MONTH, 1); // Reset to first day to prevent out-of-range issues
            monthYearDropdown.setSelection(calendar.get(Calendar.MONTH)); // Ensure dropdown syncs
            updateCalendar();  // Refresh calendar UI
        });

// Handle next month click
        nextMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1); // Move to next month
            calendar.set(Calendar.DAY_OF_MONTH, 1); // Reset to first day to prevent out-of-range issues
            monthYearDropdown.setSelection(calendar.get(Calendar.MONTH)); // Ensure dropdown syncs
            updateCalendar();  // Refresh calendar UI
        });



        // Setup month-year spinner
        MonthYearSpinnerAdapter spinnerAdapter = new MonthYearSpinnerAdapter(requireContext(), getMonthYearList());
        spinnerAdapter.setDropDownViewResource(R.layout.month_year_spinner_dropdown_item);
        monthYearDropdown.setAdapter(spinnerAdapter);
        monthYearDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (calendar.get(Calendar.MONTH) != position) {  // Avoid unnecessary reloads
                    calendar.set(Calendar.MONTH, position);
                    calendar.set(Calendar.DAY_OF_MONTH, 1); // Reset to first day for safety
                    updateCalendar();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });



        // Initialize calendar adapter
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        calendarAdapter = new CalendarAdapter(
                calendarDates,
                selectedDate -> {
                    if (!selectedDate.isEmpty()) {
                        int selectedDay = Integer.parseInt(selectedDate);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                        String currentCategory = getCurrentSelectedCategory();
                        List<Task> selectedDateTasks = filterTasksByDateBasedOnCategory(selectedDay, currentCategory);
                        taskAdapter.updateTasks(selectedDateTasks);
                        updateTasksTitle(selectedDateTasks, selectedDay);
                        updateWeeklyTasks();
                        ImageView emptyTasksImage = getView().findViewById(R.id.emptyTasksImage);
                        if (selectedDateTasks.isEmpty()) {
                            emptyTasksImage.setVisibility(View.VISIBLE);
                        } else {
                            emptyTasksImage.setVisibility(View.GONE);
                        }
                    }
                },
                new HashSet<>(), // School task dates (will be updated by listeners)
                new HashSet<>(), // Home task dates (will be updated by listeners)
                getCurrentSelectedCategory()
        );
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);
        // Auto-select today's date
        calendarAdapter.setSelectedDate(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));

        // Initialize task adapters
        taskAdapter = new TaskAdapter(new ArrayList<>(), requireContext(), this, this, this, editTaskLauncher);
        weeklyTaskAdapter = new TaskAdapter(new ArrayList<>(), requireContext(), this, this, this, editTaskLauncher);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskRecyclerView.setAdapter(taskAdapter);
        weeklyTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        weeklyTaskRecyclerView.setAdapter(weeklyTaskAdapter);

        // Setup toggle buttons for category selection
        setupToggleButtons();

        // Profile icon click listener
        ImageView profileIcon = view.findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProfileActivity.class);
            startActivity(intent);
        });


        return view;
    }

    // Attach snapshot listeners in onStart for real-time updates.
// Updated onStart and onStop to attach and remove snapshot listeners
    @Override
    public void onStart() {
        super.onStart();
        // Clear lists once when starting to ensure no previous data persists.
        schoolTaskList.clear();
        homeTaskList.clear();
        combinedTaskList.clear();
        startCombinedTaskListeners();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (schoolListener != null) {
            schoolListener.remove();
            schoolListener = null;
        }
        if (homeListener != null) {
            homeListener.remove();
            homeListener = null;
        }
    }


    // Updated snapshot listeners to clear their respective lists before adding new data
    private void startCombinedTaskListeners() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;

        // School tasks snapshot listener
        schoolListener = db.collection("users")
                .document(userId)
                .collection("schooltasks")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "School listener error", e);
                        return;
                    }
                    if (querySnapshot != null) {
                        // Clear list before adding fresh data
                        schoolTaskList.clear();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Task task = doc.toObject(Task.class);
                            // Include tasks with category "School" or "Both"
                            if (!task.isCompleted() &&
                                    ("School".equals(task.getCategory()) || "Both".equals(task.getCategory()))) {
                                schoolTaskList.add(task);
                            }
                        }
                        updateCombinedTasks();
                    }
                });

        // Home tasks snapshot listener
        homeListener = db.collection("users")
                .document(userId)
                .collection("housetasks")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Home listener error", e);
                        return;
                    }
                    if (querySnapshot != null) {
                        homeTaskList.clear();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Task task = doc.toObject(Task.class);
                            // Include tasks with category "Home" or "Both"
                            if (!task.isCompleted() &&
                                    ("Home".equals(task.getCategory()) || "Both".equals(task.getCategory()))) {
                                homeTaskList.add(task);
                            }
                        }
                        updateCombinedTasks();
                    }
                });
    }



    private void updateCombinedTasks() {
        // Use a LinkedHashMap to deduplicate tasks while preserving insertion order.
        Map<String, Task> combinedMap = new LinkedHashMap<>();
        for (Task task : schoolTaskList) {
            combinedMap.put(task.getId(), task);
        }
        for (Task task : homeTaskList) {
            combinedMap.put(task.getId(), task);
        }
        combinedTaskList.clear();
        combinedTaskList.addAll(combinedMap.values());

        // Refresh UI sections
        updateCalendar();
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
        updateWeeklyTasks();
    }




    // Update selected date text.
    private void updateSelectedDateText(TextView selectedDateText, int day) {
        String monthName = getMonthYearList().get(calendar.get(Calendar.MONTH));
        String formattedDate = getFormattedDayOfWeek(calendar) + " " + day + getOrdinalSuffix(day) + " " + monthName;
        selectedDateText.setText(formattedDate);
    }

    // Return day-of-week string.
    private String getFormattedDayOfWeek(Calendar calendar) {
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, java.util.Locale.getDefault());
    }

    // --- TASK OPERATIONS ---

    // When a task is deleted, Firestore changes will update the UI via snapshot listeners.
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

        // Determine if this is a "Home" or "School" task
        boolean isHomeTask = "Home".equals(task.getCategory());
        String cancelledCollection = isHomeTask ? "cancelledHomeTasks" : "cancelledSchoolTasks";
        String originalCollection = isHomeTask ? "housetasks" : "schooltasks";

        // 1. Archive the task in the appropriate "cancelled" collection
        db.collection("users")
                .document(userId)
                .collection(cancelledCollection)
                .document(task.getId())
                .set(task)
                .addOnSuccessListener(aVoid -> {
                    // 2. After archiving, delete the task from its original collection
                    db.collection("users")
                            .document(userId)
                            .collection(originalCollection)
                            .document(task.getId())
                            .delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d("Firestore", "Task archived and deleted: " + task.getTitle());
                                // 3. Update your UI as needed
                                updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
                                updateWeeklyTasks();
                                updateCalendar();
                                updateCancelledTasksCount();
                            })
                            .addOnFailureListener(e -> Log.e("Firestore", "Error deleting task", e));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error archiving task", e));
    }
    private List<String> getRepeatedDates(String startDate, Task.RepeatOption repeatOption) {
        List<String> repeatedDates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        LocalDate startLocalDate = LocalDate.parse(startDate, formatter);

        // If the task is set to not repeat, return the original date.
        if (repeatOption == Task.RepeatOption.DOES_NOT_REPEAT) {
            repeatedDates.add(startDate);
            return repeatedDates;
        }

        // Map the repeat option to the corresponding DayOfWeek.
        DayOfWeek repeatDay;
        switch (repeatOption) {
            case REPEAT_EVERY_MONDAY:
                repeatDay = DayOfWeek.MONDAY;
                break;
            case REPEAT_EVERY_TUESDAY:
                repeatDay = DayOfWeek.TUESDAY;
                break;
            case REPEAT_EVERY_WEDNESDAY:
                repeatDay = DayOfWeek.WEDNESDAY;
                break;
            case REPEAT_EVERY_THURSDAY:
                repeatDay = DayOfWeek.THURSDAY;
                break;
            case REPEAT_EVERY_FRIDAY:
                repeatDay = DayOfWeek.FRIDAY;
                break;
            case REPEAT_EVERY_SATURDAY:
                repeatDay = DayOfWeek.SATURDAY;
                break;
            case REPEAT_EVERY_SUNDAY:
                repeatDay = DayOfWeek.SUNDAY;
                break;
            default:
                repeatedDates.add(startDate);
                return repeatedDates;
        }

        // Find the first occurrence of the specified day in the month.
        LocalDate firstOccurrence = startLocalDate.with(TemporalAdjusters.firstInMonth(repeatDay));

        // Loop through all occurrences of that day in the current month.
        while (firstOccurrence.getMonthValue() == startLocalDate.getMonthValue() &&
                firstOccurrence.getYear() == startLocalDate.getYear()) {
            repeatedDates.add(firstOccurrence.format(formatter));
            firstOccurrence = firstOccurrence.with(TemporalAdjusters.next(repeatDay));
        }

        return repeatedDates;
    }


    @Override
    public void onTaskEdited(Task updatedTask, int position) {
        updateExistingTask(updatedTask);
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));  // Ensure UI refresh
        updateWeeklyTasks();
        updateCalendar();
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
    public void addTaskToCalendar(String taskTitle, String taskPriority, String taskDate, String taskTime,
                                  String taskType, String repeatOptionString) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Expected date format: "day/month/year" (e.g., "3/2/2025")
        String[] dateParts = taskDate.split("/");
        if (dateParts.length != 3) {
            Toast.makeText(requireContext(), "Invalid date format!", Toast.LENGTH_SHORT).show();
            return;
        }

        int day, monthIndex, year;
        try {
            day = Integer.parseInt(dateParts[0]);
            monthIndex = Integer.parseInt(dateParts[1]) - 1;
            year = Integer.parseInt(dateParts[2]);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid date numbers!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert the repeat option string to the RepeatOption enum.
        Task.RepeatOption repeatOption = getRepeatOptionFromString(repeatOptionString);

        // Get the repeated dates for the current month.
        List<String> repeatedDates = getRepeatedDates(taskDate, repeatOption);

        for (String repeatedDate : repeatedDates) {
            String taskId = UUID.randomUUID().toString();
            String[] repeatedDateParts = repeatedDate.split("/");
            int repeatedYear = Integer.parseInt(repeatedDateParts[2]);

            // Create a new Task.
            // Note: The "date" field is set to repeatedDateParts[0] (the day only) so that filtering
            // (which compares against a day string like "8") will work correctly.
            Task newTask = new Task(
                    taskId,
                    taskTitle,
                    taskTime,
                    repeatedDateParts[0], // Store just the day (e.g., "8")
                    getMonthYearList().get(Integer.parseInt(repeatedDateParts[1]) - 1), // Month string (e.g., "March")
                    taskPriority,
                    taskType,
                    repeatedYear,
                    0, // Default stability value
                    System.currentTimeMillis(),
                    repeatedDate, // Full date string (e.g., "8/3/2025") stored elsewhere if needed
                    false,
                    repeatOption
            );

            // Determine collection based on taskType.
            String taskCollection = "Home".equals(taskType) ? "housetasks" : "schooltasks";

            // Save the task to Firestore.
            db.collection("users")
                    .document(userId)
                    .collection(taskCollection)
                    .document(taskId)
                    .set(newTask)
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Task successfully added!"))
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error adding task", e);
                        Toast.makeText(requireContext(), "Error adding task", Toast.LENGTH_SHORT).show();
                    });
        }
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

        // Determine the Firestore collection based on task category
        String taskCollection = "Home".equals(updatedTask.getCategory()) ? "housetasks" : "schooltasks";

        db.collection("users")
                .document(userId)
                .collection(taskCollection)
                .document(updatedTask.getId())
                .update(
                        "title", updatedTask.getTitle(),
                        "time", updatedTask.getTime(),
                        "date", updatedTask.getDate(),
                        "month", updatedTask.getMonth(),
                        "priority", updatedTask.getPriority(),
                        "category", updatedTask.getCategory(),
                        "year", updatedTask.getYear(),
                        "fullDate", updatedTask.getFullDate(),
                        "completed", updatedTask.isCompleted(),
                        "repeatOption", updatedTask.getRepeatOption().name()
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Task successfully updated: " + updatedTask.getTitle());

                    // ✅ Update the local task lists manually so UI refreshes immediately
                    updateLocalTaskLists(updatedTask);

                    // ✅ Refresh the UI
                    updateCalendar();
                    updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
                    updateWeeklyTasks();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating task", e));
    }

    /**
     * ✅ Updates the task lists in memory to ensure instant UI refresh
     */
    private void updateLocalTaskLists(Task updatedTask) {
        String taskId = updatedTask.getId();
        boolean found = false;

        // ✅ Update schoolTaskList
        for (int i = 0; i < schoolTaskList.size(); i++) {
            if (schoolTaskList.get(i).getId().equals(taskId)) {
                schoolTaskList.set(i, updatedTask);
                found = true;
                break;
            }
        }

        // ✅ Update homeTaskList if not found in schoolTaskList
        if (!found) {
            for (int i = 0; i < homeTaskList.size(); i++) {
                if (homeTaskList.get(i).getId().equals(taskId)) {
                    homeTaskList.set(i, updatedTask);
                    found = true;
                    break;
                }
            }
        }

        // ✅ Update combinedTaskList
        for (int i = 0; i < combinedTaskList.size(); i++) {
            if (combinedTaskList.get(i).getId().equals(taskId)) {
                combinedTaskList.set(i, updatedTask);
                break;
            }
        }
    }


    // Mark a task as completed.
    // ----- UPDATED onTaskCompleted() -----
    @Override
    public void onTaskCompleted(Task task) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Determine the collection based on task category.
        String taskCollection = "Home".equals(task.getCategory()) ? "housetasks" : "schooltasks";

        db.collection("users")
                .document(userId)
                .collection(taskCollection)
                .document(task.getId())
                .update("completed", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TaskCompletion", "Task marked as completed: " + task.getTitle());
                    // Remove the task from both lists
                    schoolTaskList.removeIf(t -> t.getId().equals(task.getId()));
                    homeTaskList.removeIf(t -> t.getId().equals(task.getId()));
                    // Rebuild the combined list (this deduplicates any duplicate entries)
                    updateCombinedTasks();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating task completion", e));
    }

    private final ActivityResultLauncher<Intent> editTaskLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task updatedTask = result.getData().getParcelableExtra("updatedTask");
                    if (updatedTask != null) {
                        updateExistingTask(updatedTask);
                        taskAdapter.updateTaskInList(updatedTask);  // ✅ Ensure UI updates instantly
                    }
                }
            });


    // --- UI UPDATE METHODS ---
    public void updateTasksForToday(int day) {
        String currentCategory = getCurrentSelectedCategory();
        List<Task> tasksToFilter = new ArrayList<>();

        if ("School".equals(currentCategory)) {
            tasksToFilter.addAll(schoolTaskList);
        } else if ("Home".equals(currentCategory)) {
            tasksToFilter.addAll(homeTaskList);
        } else { // "Both"
            tasksToFilter.addAll(schoolTaskList);
            tasksToFilter.addAll(homeTaskList);
        }

        List<Task> todayTasks = new ArrayList<>();
        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        int currentYear = calendar.get(Calendar.YEAR);

        for (Task t : tasksToFilter) {
            String dateStr = t.getDate();
            int taskDay;
            try {
                // If the date contains a slash, split and use only the first part.
                if (dateStr.contains("/")) {
                    String[] parts = dateStr.split("/");
                    taskDay = Integer.parseInt(parts[0]);
                } else {
                    taskDay = Integer.parseInt(dateStr);
                }
            } catch (NumberFormatException e) {
                Log.e("CombinedTaskFilter", "Error parsing task day from: " + t.getDate(), e);
                continue;
            }
            if (taskDay == day &&
                    t.getMonth().equalsIgnoreCase(currentMonth) &&
                    t.getYear() == currentYear &&
                    !t.isCompleted()) {
                todayTasks.add(t);
            }
        }

        taskAdapter.updateTasks(todayTasks);
        taskAdapter.notifyDataSetChanged();
        updateTasksTitle(todayTasks, day);

        ImageView emptyTasksImage = getView().findViewById(R.id.emptyTasksImage);
        emptyTasksImage.setVisibility(todayTasks.isEmpty() ? View.VISIBLE : View.GONE);
    }






    // Update weekly tasks based on current category and week.
    private void updateWeeklyTasks() {
        if (getView() == null) {
            Log.e("updateWeeklyTasks", "View is null, skipping UI update.");
            return;
        }
        String currentCategory = getCurrentSelectedCategory();
        List<Task> filteredTaskList;
        if ("School".equals(currentCategory)) {
            filteredTaskList = new ArrayList<>(schoolTaskList);
        } else if ("Home".equals(currentCategory)) {
            filteredTaskList = new ArrayList<>(homeTaskList);
        } else {
            filteredTaskList = new ArrayList<>(combinedTaskList);
        }
        LocalDate currentDate = LocalDate.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        LocalDate startOfWeek = currentDate.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = currentDate.with(java.time.DayOfWeek.SUNDAY);
        List<Task> weeklyTasks = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        for (Task t : filteredTaskList) {
            try {
                // If the stored date already contains a slash, assume it's the full date.
                String fullDate;
                if (t.getDate().contains("/")) {
                    fullDate = t.getDate();
                } else {
                    fullDate = t.getDate() + "/" + (getMonthIndex(t.getMonth()) + 1) + "/" + t.getYear();
                }
                LocalDate taskDate = LocalDate.parse(fullDate, formatter);
                if (!taskDate.isBefore(startOfWeek) && !taskDate.isAfter(endOfWeek) && !t.isCompleted()) {
                    weeklyTasks.add(t);
                }
            } catch (DateTimeParseException e) {
                Log.e("updateWeeklyTasks", "Error parsing date: " + e.getMessage());
            }
        }

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

    // Update the calendar view (dates, highlights, daily and weekly tasks).
    private void updateCalendar() {
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        Set<String> schoolDates = getSchoolTaskDates();
        Set<String> homeDates = getHomeTaskDates();

        if (calendarAdapter != null) {
            calendarAdapter.updateData(calendarDates);
            calendarAdapter.updateSchoolTaskDates(schoolDates);
            calendarAdapter.updateHomeTaskDates(homeDates);
            calendarAdapter.updateCategory(getCurrentSelectedCategory());
        }

        // Ensure the selected month in Spinner matches the actual calendar month
        monthYearDropdown.setSelection(calendar.get(Calendar.MONTH));

        // Refresh task lists and UI
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
        updateWeeklyTasks();
    }


    // ----- UPDATED getSchoolTaskDates() -----
    private Set<String> getSchoolTaskDates() {
        Set<String> schoolTaskDates = new HashSet<>();
        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        int currentYear = calendar.get(Calendar.YEAR);
        for (Task task : schoolTaskList) {
            // Only include tasks that are not completed and that actually belong to School (or Both if intended)
            if (!task.isCompleted() &&
                    ( "School".equals(task.getCategory()) || "Both".equals(task.getCategory()) ) &&
                    task.getMonth().equalsIgnoreCase(currentMonth) &&
                    task.getYear() == currentYear) {
                schoolTaskDates.add(task.getDate());
            }
        }
        Log.d("getSchoolTaskDates", "School Task Dates Highlighted: " + schoolTaskDates);
        return schoolTaskDates;
    }


    // ----- UPDATED getHomeTaskDates() -----
    private Set<String> getHomeTaskDates() {
        Set<String> homeTaskDates = new HashSet<>();
        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        int currentYear = calendar.get(Calendar.YEAR);
        for (Task task : homeTaskList) {
            // Only include tasks that are not completed and that belong to Home (or Both if intended)
            if (!task.isCompleted() &&
                    ( "Home".equals(task.getCategory()) || "Both".equals(task.getCategory()) ) &&
                    task.getMonth().equalsIgnoreCase(currentMonth) &&
                    task.getYear() == currentYear) {
                homeTaskDates.add(task.getDate());
            }
        }
        Log.d("getHomeTaskDates", "Home Task Dates Highlighted: " + homeTaskDates);
        return homeTaskDates;
    }


    // Update the "Tasks for Today" title.
    private void updateTasksTitle(List<Task> selectedDateTasks, int selectedDay) {
        TextView tasksDueTodayTitle = getView().findViewById(R.id.tasksDueTodayTitle);
        if (selectedDateTasks.isEmpty()) {
            tasksDueTodayTitle.setText("No school tasks for selected date");
        } else {
            String monthYear = getMonthYearList().get(calendar.get(Calendar.MONTH)) + " " + calendar.get(Calendar.YEAR);
            String formattedDate = selectedDay + getOrdinalSuffix(selectedDay) + " " + monthYear;
            tasksDueTodayTitle.setText("School Tasks for " + formattedDate);
        }
    }

    // --- HELPER METHODS ---

    // Get the calendar dates for a given month.
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

    // Return a list of month names.
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

    // Get week range for a given selected day.
    private int[] getWeekRange(int selectedDay, int daysInMonth) {
        int startDay = Math.max(1, selectedDay - (selectedDay - 1) % 7);
        int endDay = Math.min(daysInMonth, startDay + 6);
        return new int[]{startDay, endDay};
    }

    // Filter tasks by date from a given list.
    private List<Task> filterTasksByDate(int day, String month, List<Task> taskListToFilter) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task t : taskListToFilter) {
            try {
                if (Integer.parseInt(t.getDate()) == day && t.getMonth().equalsIgnoreCase(month)) {
                    filteredTasks.add(t);
                }
            } catch (NumberFormatException e) {
                Log.e("filterTasksByDate", "Error parsing task date: " + e.getMessage());
            }
        }
        return filteredTasks;
    }

    // Filter tasks by date and category using the combined task list.
    private List<Task> filterTasksByDateBasedOnCategory(int day, String category) {
        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        List<Task> results = new ArrayList<>();
        if ("School".equals(category)) {
            results = filterTasksByDate(day, currentMonth, schoolTaskList);
        } else if ("Home".equals(category)) {
            results = filterTasksByDate(day, currentMonth, homeTaskList);
        } else { // "Both"
            Set<String> seenIds = new HashSet<>();
            for (Task t : combinedTaskList) {
                String dateStr = t.getDate();
                int taskDay;
                try {
                    if (dateStr.contains("/")) {
                        String[] parts = dateStr.split("/");
                        taskDay = Integer.parseInt(parts[0]);
                    } else {
                        taskDay = Integer.parseInt(dateStr);
                    }
                } catch (NumberFormatException e) {
                    Log.e("filterTasksByCategory", "Error parsing date: " + t.getDate(), e);
                    continue;
                }
                if (taskDay == day && t.getMonth().equalsIgnoreCase(currentMonth)) {
                    if (!seenIds.contains(t.getId())) {
                        results.add(t);
                        seenIds.add(t.getId());
                    }
                }
            }
        }
        return results;
    }



    // Get priority value for sorting.
    private int getPriorityValue(String priority) {
        switch (priority) {
            case "High": return 1;
            case "Medium": return 2;
            case "Low": return 3;
            default: return 4;
        }
    }

    // Get the index of a month name.
    private int getMonthIndex(String month) {
        return getMonthYearList().indexOf(month);
    }

    // Get ordinal suffix for a given day.
    private String getOrdinalSuffix(int day) {
        if (day >= 11 && day <= 13) return "th";
        switch (day % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    private void loadTasksFromFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot fetch tasks");
            return;
        }
        // Clear all lists to avoid duplicates
        schoolTaskList.clear();
        homeTaskList.clear();
        combinedTaskList.clear();

        // Fetch School tasks
        db.collection("users")
                .document(userId)
                .collection("schooltasks")
                .whereEqualTo("completed", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = doc.toObject(Task.class);
                        schoolTaskList.add(task);
                        combinedTaskList.add(task);
                    }
                    // Then fetch Home tasks
                    db.collection("users")
                            .document(userId)
                            .collection("housetasks")
                            .whereEqualTo("completed", false)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots2) {
                                    Task task = doc.toObject(Task.class);
                                    homeTaskList.add(task);
                                    combinedTaskList.add(task);
                                }
                                // Deduplicate combined list and update UI
                                updateCombinedTasks();
                            })
                            .addOnFailureListener(e -> Log.e("Firestore", "Error fetching home tasks", e));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching school tasks", e));
    }


    // Manual updateTasks method (if parent activity needs to refresh tasks)
    public void updateTasks(List<Task> tasks) {
        combinedTaskList.clear();
        combinedTaskList.addAll(tasks);
        updateCalendar();
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
    }

    // Update current tasks when toggle selection changes.
    private void updateCurrentTasks() {
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
    }

    // Setup the three-way toggle buttons for category selection.
    private void setupToggleButtons() {
        schoolToggleButton.setOnClickListener(v -> {
            setActiveTopButton(schoolToggleButton, bothToggleButton, houseToggleButton);
            calendarAdapter.updateCategory("School");
            updateCurrentTasks();
            updateWeeklyTasks();
        });
        houseToggleButton.setOnClickListener(v -> {
            setActiveTopButton(houseToggleButton, bothToggleButton, schoolToggleButton);
            calendarAdapter.updateCategory("Home");
            updateCurrentTasks();
            updateWeeklyTasks();
        });
        bothToggleButton.setOnClickListener(v -> {
            setActiveTopButton(bothToggleButton, schoolToggleButton, houseToggleButton);
            calendarAdapter.updateCategory("Both");
            updateCurrentTasks();
            updateWeeklyTasks();
        });
    }

    // --- method to update the Cancelled Tasks Card ---
    private void updateCancelledTasksCount() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("cancelledSchoolTasks")
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


    // Helper to set the active top toggle button.
    private void setActiveTopButton(TextView activeButton, TextView... inactiveButtons) {
        activeButton.setSelected(true);
        if (activeButton == schoolToggleButton) {
            activeButton.setBackgroundResource(R.drawable.toggle_school_selected);
        } else if (activeButton == houseToggleButton) {
            activeButton.setBackgroundResource(R.drawable.toggle_house_selected);
        } else if (activeButton == bothToggleButton) {
            activeButton.setBackgroundResource(R.drawable.toggle_both_selected);
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


    // Return the currently selected category based on toggle buttons.
    private String getCurrentSelectedCategory() {
        if (schoolToggleButton.isSelected()) {
            return "School";
        } else if (houseToggleButton.isSelected()) {
            return "Home";
        } else {
            return "Both";
        }
    }
}
