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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    // Attach snapshot listener to "schooltasks" collection
    private void startTaskListener() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;
        tasksListener = db.collection("users")
                .document(userId)
                .collection("schooltasks")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Snapshot listener error", e);
                        return;
                    }
                    if (querySnapshot != null) {
                        taskList.clear();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Task task = doc.toObject(Task.class);
                            // Only add incomplete tasks
                            if (!task.isCompleted()) {
                                taskList.add(task);
                            }
                        }
                        // Update the calendar, daily, and weekly sections
                        updateCalendar();
                        updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
                        updateWeeklyTasks();
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

    @Override
    public void onTaskDeleted(Task task) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot delete task");
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("users")
                .document(userId)
                .collection("schooltasks")
                .document(task.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // The snapshot listener will update the UI automatically.
                    Log.d("Firestore", "Task successfully deleted");
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error deleting task", e));
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
                schoolTaskDates.add(t.getDate());
            }
        }
        return schoolTaskDates;
    }

    // Filter tasks for a given day (for School category)
    private List<Task> filterTasksByDateBasedOnCategory(int day, String category) {
        if (!"School".equals(category)) {
            Log.e("filterTasksByDateBasedOnCategory", "Invalid category: " + category);
            return new ArrayList<>();
        }
        List<Task> filteredTasks = new ArrayList<>();
        for (Task t : taskList) {
            if (Integer.parseInt(t.getDate()) == day && "School".equals(t.getCategory())) {
                filteredTasks.add(t);
            }
        }
        return filteredTasks;
    }

    @Override
    public void onTaskEdited(Task updatedTask, int position) {
        updateExistingTask(updatedTask);
    }

    // Update an existing task by matching its ID
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
                .collection("schooltasks")
                .document(updatedTask.getId())
                .set(updatedTask)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Task successfully updated");
                    // The snapshot listener will update the UI.
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating task", e));
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

    // Update today's tasks based on the current day (School category)
    public void updateTasksForToday(int day) {
        List<Task> todayTasks = new ArrayList<>();
        for (Task t : taskList) {
            if (!t.isCompleted() && Integer.parseInt(t.getDate()) == day && "School".equals(t.getCategory())) {
                todayTasks.add(t);
            }
        }
        taskAdapter.updateTasks(todayTasks);
        taskAdapter.notifyDataSetChanged();
        updateTasksTitle(todayTasks, day);

        // Show the empty state image if there are no tasks for today
        ImageView emptyTasksImage = getView().findViewById(R.id.emptyTasksImage);
        if (todayTasks.isEmpty()) {
            emptyTasksImage.setVisibility(View.VISIBLE);
        } else {
            emptyTasksImage.setVisibility(View.GONE);
        }
    }


    // Update the weekly tasks view (School category)
    private void updateWeeklyTasks() {
        if (getView() == null) {
            Log.e("updateWeeklyTasks", "View is null, skipping UI update.");
            return;
        }
        String currentDateString = calendar.get(Calendar.DAY_OF_MONTH) + "/"
                + (calendar.get(Calendar.MONTH) + 1) + "/"
                + calendar.get(Calendar.YEAR);
        List<Task> allWeeklyTasks = filterTasksByWeek(currentDateString);
        List<Task> weeklySchoolTasks = new ArrayList<>();
        for (Task task : allWeeklyTasks) {
            if ("School".equals(task.getCategory()) && !task.isCompleted()) {
                weeklySchoolTasks.add(task);
            }
        }
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

    // Add a new task to the School tasks collection
    public void addTaskToCalendar(String title, String priority, String date, String time, boolean remind, String taskType) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }
        String taskId = UUID.randomUUID().toString();
        String[] dateParts = date.split("/");
        if (dateParts.length != 3) {
            Toast.makeText(requireContext(), "Invalid date format!", Toast.LENGTH_SHORT).show();
            return;
        }
        int year = Integer.parseInt(dateParts[2]);
        Task newTask = new Task(
                taskId,
                title,
                time,
                dateParts[0],
                getMonthYearList().get(Integer.parseInt(dateParts[1]) - 1),
                priority,
                "School", // Ensure this task is marked as School
                remind,
                year,
                0,
                System.currentTimeMillis(),
                dateParts[0] + "/" + dateParts[1] + "/" + year,
                false
        );
        db.collection("users")
                .document(userId)
                .collection("schooltasks")
                .document(taskId)
                .set(newTask)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "School task successfully added!"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding school task", e));
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
