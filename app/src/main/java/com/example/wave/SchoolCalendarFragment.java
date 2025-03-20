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
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SchoolCalendarFragment extends Fragment implements
        TaskAdapter.OnTaskDeletedListener,
        TaskAdapter.OnTaskEditedListener,
        TaskAdapter.OnTaskCompletedListener {

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

    // Snapshot listener for real-time updates
    private ListenerRegistration tasksListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_school_calendar_screen, container, false);

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

        // Get today's date
        int todayDay = calendar.get(Calendar.DAY_OF_MONTH);
        int todayMonth = calendar.get(Calendar.MONTH);

        // Update selected date text
        updateSelectedDateText(selectedDateText, todayDay);

        // Toggle buttons setup
        homeCalendarButton = view.findViewById(R.id.homeCalendarButton);
        schoolCalendarButton = view.findViewById(R.id.SchoolCalendarButton);
        // For School, default active is School so we set school button as active
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

        // Setup month/year spinner
        MonthYearSpinnerAdapter spinnerAdapter = new MonthYearSpinnerAdapter(requireContext(), getMonthYearList());
        spinnerAdapter.setDropDownViewResource(R.layout.month_year_spinner_dropdown_item);
        monthYearDropdown.setAdapter(spinnerAdapter);
        monthYearDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(Color.BLACK);
                }
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

        // Initialize calendar adapter
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        // For School tasks, pass an empty set initially; the snapshot listener will update it.
        calendarAdapter = new CalendarAdapter(
                calendarDates,
                selectedDate -> {
                    int selectedDay = Integer.parseInt(selectedDate);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    List<Task> selectedDateTasks = filterTasksByDateBasedOnCategory(selectedDay, "School");
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
                new HashSet<>(),  // school task dates
                new HashSet<>(),  // home task dates (unused here)
                "School"
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

    // Real-time listener: attach in onStart, detach in onStop
    @Override
    public void onStart() {
        super.onStart();
        startTaskListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (tasksListener != null) {
            tasksListener.remove();
            tasksListener = null;
        }
    }

    // Returns a list of repeated dates based on the start date and repeat option.
// (Works for both Home and School tasks; no category-specific change needed.)
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
        // For example, if repeating on Saturdays (adjust mapping for other options as needed)
        DayOfWeek dayOfWeek = DayOfWeek.SATURDAY;
        LocalDate firstOccurrence = startLocalDate.with(TemporalAdjusters.firstInMonth(dayOfWeek));

        while (firstOccurrence.getMonthValue() == startLocalDate.getMonthValue()
                && firstOccurrence.getYear() == startLocalDate.getYear()) {
            repeatedDates.add(firstOccurrence.format(formatter));
            firstOccurrence = firstOccurrence.with(TemporalAdjusters.next(dayOfWeek));
        }

        return repeatedDates;
    }


    private void startTaskListener() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;
        tasksListener = db.collection("users")
                .document(userId)
                .collection("schooltasks")   // Updated for school tasks
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

                        // Filter tasks by "School" category now
                        List<Task> selectedDateTasks = filterTasksByDateBasedOnCategory(today, "School");
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


    // Helper method to update the selected date text label
    private void updateSelectedDateText(TextView selectedDateText, int day) {
        String monthName = getMonthYearList().get(calendar.get(Calendar.MONTH));
        String formattedDate = getFormattedDayOfWeek(calendar) + " " + day + getOrdinalSuffix(day) + " " + monthName;
        selectedDateText.setText(formattedDate);
    }

    // Helper to get day-of-week string
    private String getFormattedDayOfWeek(Calendar calendar) {
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, java.util.Locale.getDefault());
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
                .collection("cancelledSchoolTasks")
                .document(task.getId())
                .set(task)
                .addOnSuccessListener(aVoid -> {
                    // After archiving, delete the task from the original collection
                    db.collection("users")
                            .document(userId)
                            .collection("schooltasks")
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

    // For School, we don’t need getHomeTaskDates; we only work with school tasks.
    private Set<String> getSchoolTaskDates() {
        Set<String> schoolTaskDates = new HashSet<>();
        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        int currentYear = calendar.get(Calendar.YEAR);
        for (Task t : taskList) {
            if ("School".equals(t.getCategory()) &&
                    t.getMonth().equalsIgnoreCase(currentMonth) &&
                    t.getYear() == currentYear) {
                String dateStr = t.getDate();
                if (dateStr.contains("/")) {
                    String[] dateParts = dateStr.split("/");
                    if (dateParts.length == 3) {
                        schoolTaskDates.add(dateParts[0]); // e.g., "8" instead of "8/3/2025"
                    }
                } else {
                    schoolTaskDates.add(dateStr);
                }
            }
        }
        Log.d("getSchoolTaskDates", "School Task Dates Highlighted: " + schoolTaskDates);
        return schoolTaskDates;
    }

    private final ActivityResultLauncher<Intent> editTaskLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task updatedTask = result.getData().getParcelableExtra("updatedTask");
                    if (updatedTask != null) {
                        updateExistingTask(updatedTask);
                    }
                }
            });

    @Override
    public void onTaskEdited(Task task, int position) {
        // Open EditTasksActivity with the selected task
        Intent intent = new Intent(requireContext(), EditTasksActivity.class);
        intent.putExtra("task", task);
        editTaskLauncher.launch(intent);
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

        // Update Firestore with the edited task
        db.collection("users")
                .document(userId)
                .collection("schooltasks")
                .document(updatedTask.getId())
                .set(updatedTask)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Task successfully updated");
                    updateCalendar();
                    updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating task", e));
    }



    // Filter tasks for a given day (for School category)
    private List<Task> filterTasksByDateBasedOnCategory(int day, String category) {
        if (!"School".equals(category)) {
            Log.e("filterTasksByDateBasedOnCategory", "Invalid category: " + category);
            return new ArrayList<>();
        }

        List<Task> filteredTasks = new ArrayList<>();
        for (Task t : taskList) {
            if (!"School".equals(t.getCategory())) continue;

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

    // Update today's tasks based on the current day (School category)
    // Update today's tasks for School tasks only.
    public void updateTasksForToday(int day) {
        List<Task> todayTasks = new ArrayList<>();
        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        int currentYear = calendar.get(Calendar.YEAR);

        for (Task t : taskList) {
            // Ensure we have a full date (format: "d/M/yyyy")
            String dateStr = t.getDate();
            if (!dateStr.contains("/")) {
                // If only day is stored, append current month and year
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
                taskMonth = Integer.parseInt(dateParts[1]) - 1; // adjust for 0-based index
                taskYear = Integer.parseInt(dateParts[2]);
            } catch (NumberFormatException e) {
                Log.e("TaskFilter", "Error parsing date from task: " + t.getDate(), e);
                continue;
            }

            // Compare against today's date
            if (taskDay == day && taskMonth == calendar.get(Calendar.MONTH)
                    && taskYear == currentYear && "School".equals(t.getCategory())
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




    // Update the weekly tasks view (School category)
    private void updateWeeklyTasks() {
        if (getView() == null) {
            Log.e("updateWeeklyTasks", "View is null, skipping UI update.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        String currentDateString = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                (calendar.get(Calendar.MONTH) + 1) + "/" +
                calendar.get(Calendar.YEAR);

        LocalDate currentDate;
        try {
            currentDate = LocalDate.parse(currentDateString, formatter);
        } catch (DateTimeParseException e) {
            Log.e("updateWeeklyTasks", "Error parsing current date: " + e.getMessage());
            return;
        }

        int currentWeek = currentDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        List<Task> weeklySchoolTasks = new ArrayList<>();

        for (Task t : taskList) {
            if (!"School".equals(t.getCategory()) || t.isCompleted()) continue;
            try {
                String dateStr = t.getDate();
                if (!dateStr.contains("/")) {
                    dateStr = dateStr + "/" + (getMonthIndex(t.getMonth()) + 1) + "/" + t.getYear();
                }
                LocalDate taskDate = LocalDate.parse(dateStr, formatter);
                int taskWeek = taskDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                if (taskWeek == currentWeek) {
                    weeklySchoolTasks.add(t);
                }
            } catch (DateTimeParseException e) {
                Log.e("updateWeeklyTasks", "Error parsing task date: " + t.getDate() + ", " + e.getMessage());
            }
        }

        // Sort by priority (lower number = higher priority)
        weeklySchoolTasks.sort((t1, t2) -> getPriorityValue(t1.getPriority()) - getPriorityValue(t2.getPriority()));

        View tasksTitle = getView().findViewById(R.id.tasksDueThisWeekTitle);
        View weeklyRecyclerView = getView().findViewById(R.id.weeklyTaskRecyclerView);
        if (weeklySchoolTasks.isEmpty()) {
            tasksTitle.setVisibility(View.GONE);
            weeklyRecyclerView.setVisibility(View.GONE);
        } else {
            tasksTitle.setVisibility(View.VISIBLE);
            weeklyRecyclerView.setVisibility(View.VISIBLE);
            weeklyTaskAdapter.updateTasks(weeklySchoolTasks);
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

    // Add a new task to the School tasks collection
    // Add a new task to the School tasks collection.
// This method now saves school tasks to the "schooltasks" collection.
    public void addTaskToCalendar(String title, String priority, String date, String time, String taskType, String repeatOptionString) {
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

            // IMPORTANT: Store the full date string in the "date" field for proper filtering.
            Task newTask = new Task(
                    taskId,
                    title,
                    time,
                    repeatedDate, // Store full date string ("8/3/2025") instead of just the day.
                    getMonthYearList().get(Integer.parseInt(dateParts[1]) - 1),
                    priority,
                    taskType,
                    year,
                    0, // Default stability value
                    System.currentTimeMillis(),
                    repeatedDate, // Full date string (if used elsewhere)
                    false,
                    repeatOption
            );

            // Save the task to the "schooltasks" collection for School tasks.
            db.collection("users")
                    .document(userId)
                    .collection("schooltasks")
                    .document(taskId)
                    .set(newTask)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Task successfully added!");
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error adding task", e));
        }
    }



    // Update the "Tasks for Today" title based on whether there are tasks for the selected day.
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

    // Update the calendar view: rebuild dates, update highlights, and refresh daily and weekly tasks.
    private void updateCalendar() {
        calendarDates = getCalendarDates(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        Set<String> taskDates = getSchoolTaskDates();
        calendarAdapter.updateSchoolTaskDates(taskDates);
        calendarAdapter.updateData(calendarDates);
        int todayDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendarAdapter.setSelectedDate(String.valueOf(todayDay));
        updateTasksForToday(todayDay);
        updateWeeklyTasks();
    }


    // Get calendar dates for the month
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

    // List of month names
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

    // For manual updates (if needed) from the parent activity.
    public void updateTasks(List<Task> tasks) {
        taskList.clear();
        taskList.addAll(tasks);
        updateCalendar();
        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onTaskCompleted(Task task) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot complete task");
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("TaskCompletion", "Marking school task as completed: " + task.getTitle());
        Log.d("TaskCompletion", "Task ID: " + task.getId());
        db.collection("users")
                .document(userId)
                .collection("schooltasks")
                .document(task.getId())
                .update("completed", true)
                .addOnSuccessListener(aVoid -> {
                    // The snapshot listener will remove the completed task and update UI.
                    Log.d("TaskCompletion", "School task completed and UI updated immediately.");
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating school task completion", e));
    }

    private void notifyTaskCompletionChanged() {
        if (getActivity() instanceof TaskCompletionListener) {
            ((TaskCompletionListener) getActivity()).onTaskCompletedUpdate();
        }
        if (getView() != null) {
            TextView tasksCountTextView = getView().findViewById(R.id.tasks_count);
            if (tasksCountTextView != null) {
                tasksCountTextView.setText(String.valueOf(getCompletedTaskCount()));
            } else {
                Log.e("notifyTaskCompletion", "tasks_count TextView is null, cannot update.");
            }
        }
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
    private int getCompletedTaskCount() {
        int count = 0;
        for (Task task : taskList) {
            if (task.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void onResume() {
        super.onResume();
        // The snapshot listener will update the UI, so no need to manually load tasks here.
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
