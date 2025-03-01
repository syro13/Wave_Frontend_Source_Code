package com.example.wave;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;



public class HomeCalendarFragment extends Fragment implements TaskAdapter.OnTaskDeletedListener, TaskAdapter.OnTaskEditedListener, TaskAdapter.OnTaskCompletedListener   {

    private RecyclerView calendarRecyclerView, taskRecyclerView, weeklyTaskRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TaskAdapter taskAdapter, weeklyTaskAdapter;
    private List<String> calendarDates;
    private List<Task> taskList;
    private List<Task> completedTaskList;
    private Calendar calendar;
    private Spinner monthYearDropdown;
    private TextView homeCalendarButton, schoolCalendarButton; // Toggle buttons
    public static final int REQUEST_EDIT_TASK = 1001;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_house_calendar_screen, container, false);

        // Initialize calendar and task list
        calendar = Calendar.getInstance();
        taskList = new ArrayList<>();
        completedTaskList = new ArrayList<>();

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
        int todayYear = calendar.get(Calendar.YEAR);

        // ✅ Ensure the UI shows today's date
        updateSelectedDateText(selectedDateText, todayDay);

        // Toggle buttons
        homeCalendarButton = view.findViewById(R.id.homeCalendarButton);
        schoolCalendarButton = view.findViewById(R.id.SchoolCalendarButton);
        setActiveButton(homeCalendarButton, schoolCalendarButton);

        homeCalendarButton.setOnClickListener(v -> {
            setActiveButton(homeCalendarButton, schoolCalendarButton);
            updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
        });

        schoolCalendarButton.setOnClickListener(v -> {
            setActiveButton(schoolCalendarButton, homeCalendarButton);
            updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
        });

        MonthYearSpinnerAdapter adapter = new MonthYearSpinnerAdapter(requireContext(), getMonthYearList());
        adapter.setDropDownViewResource(R.layout.month_year_spinner_dropdown_item);
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

        // ✅ Set dropdown to today's month
        monthYearDropdown.setSelection(todayMonth);

        // ✅ Initialize calendar adapter with empty data
        calendarDates = getCalendarDates(todayYear, todayMonth);
        calendarAdapter = new CalendarAdapter(
                calendarDates,
                selectedDate -> {
                    int selectedDay = Integer.parseInt(selectedDate);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    List<Task> selectedDateTasks = filterTasksByDateBasedOnCategory(selectedDay, "Home");
                    taskAdapter.updateTasks(selectedDateTasks);
                    updateTasksTitle(selectedDateTasks, selectedDay);
                    updateWeeklyTasks();
                },
                new HashSet<>(),
                new HashSet<>(), // Empty set initially, tasks will be loaded
                "Home"
        );

        calendarRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);

        // ✅ Auto-select today's date in the calendar adapter
        calendarAdapter.setSelectedDate(String.valueOf(todayDay));

        // Initialize TaskAdapters with the editTaskLauncher
        taskAdapter = new TaskAdapter(new ArrayList<>(), requireContext(), this, this, this, editTaskLauncher);
        weeklyTaskAdapter = new TaskAdapter(new ArrayList<>(), requireContext(), this, this, this, editTaskLauncher);

        taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskRecyclerView.setAdapter(taskAdapter);

        weeklyTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        weeklyTaskRecyclerView.setAdapter(weeklyTaskAdapter);

        // ✅ Load tasks from Firestore and update UI
        loadTasksFromFirestore();

        // Profile icon click listener
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
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot delete task");
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("housetasks")
                .document(task.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // ✅ Only remove from taskList (completed tasks aren't in UI)
                    taskList.removeIf(t -> t.getId().equals(task.getId()));

                    // ✅ Update the UI with the latest active tasks
                    updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
                    updateWeeklyTasks();
                    taskAdapter.notifyDataSetChanged();

                    Log.d("Firestore", "Task successfully deleted");
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error deleting task", e));
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
            // ✅ Ensure the task has valid data
            if (t.getCategory() == null || t.getDate() == null || t.getMonth() == null) {
                Log.e("getHomeTaskDates", "Skipping task due to missing fields: " + t);
                continue;
            }

            // ✅ Only add INCOMPLETE tasks that match the category and current month
            if ("Home".equals(t.getCategory()) &&
                    t.getMonth().equalsIgnoreCase(currentMonth) &&
                    t.getYear() == currentYear &&
                    !t.isCompleted()) {
                homeTaskDates.add(t.getDate()); // ✅ Add only the DATE part
            }
        }

        Log.d("getHomeTaskDates", "Home Task Dates Highlighted: " + homeTaskDates);
        return homeTaskDates;
    }




    private List<Task> filterTasksByDateBasedOnCategory(int day, String category) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task t : taskList) {
            if (Integer.parseInt(t.getDate()) == day && t.getCategory().equals(category)) {
                filteredTasks.add(t);
            }
        }
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
            Log.e("updateExistingTask", "Updated task ID is null. Cannot update by ID.");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

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
                .addOnSuccessListener(aVoid -> {
                    for (int i = 0; i < taskList.size(); i++) {
                        if (taskList.get(i).getId().equals(updatedTask.getId())) {
                            taskList.set(i, updatedTask);
                            break;
                        }
                    }
                    updateCalendar();
                    updateWeeklyTasks();
                    taskAdapter.notifyDataSetChanged();
                    Log.d("Firestore", "Task successfully updated");
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating task", e));
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
        String activeCategory = homeCalendarButton.isSelected() ? "Home" : "School";
        List<Task> todayTasks = new ArrayList<>();

        for (Task t : taskList) {
            if (!t.isCompleted() && Integer.parseInt(t.getDate()) == day && t.getCategory().equals(activeCategory)) {
                todayTasks.add(t);
            }
        }

        Log.d("updateTasksForToday", "Updating daily task list with " + todayTasks.size() + " tasks.");
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

        List<Task> allWeeklyTasks = filterTasksByWeek(currentDateString);

        // ✅ Ensure completed tasks are filtered out
        List<Task> weeklyHomeTasks = new ArrayList<>();
        for (Task task : allWeeklyTasks) {
            if ("Home".equals(task.getCategory()) && !task.isCompleted()) {
                weeklyHomeTasks.add(task);
            }
        }

        // ✅ Sort tasks by priority: "High" → "Medium" → "Low"
        weeklyHomeTasks.sort((t1, t2) -> getPriorityValue(t1.getPriority()) - getPriorityValue(t2.getPriority()));

        View tasksTitle = getView().findViewById(R.id.tasksDueThisWeekTitle);
        View weeklyRecyclerView = getView().findViewById(R.id.weeklyTaskRecyclerView);

        if (weeklyHomeTasks.isEmpty()) {
            Log.d("updateWeeklyTasks", "No home tasks this week. Hiding section.");
            tasksTitle.setVisibility(View.GONE);
            weeklyRecyclerView.setVisibility(View.GONE);
        } else {
            Log.d("updateWeeklyTasks", "Home tasks found. Showing section.");
            tasksTitle.setVisibility(View.VISIBLE);
            weeklyRecyclerView.setVisibility(View.VISIBLE);
            weeklyTaskAdapter.updateTasks(weeklyHomeTasks);
            weeklyTaskAdapter.notifyDataSetChanged();
        }
    }




    public void addTaskToCalendar(String title, String priority, String date, String time, boolean remind, String taskType) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot add task");
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        String taskId = UUID.randomUUID().toString();
        String[] dateParts = date.split("/");
        if (dateParts.length != 3) {
            Log.e("addTaskToCalendar", "Invalid date format: " + date);
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
                taskType,
                remind,
                year,
                0, // Default stability value
                System.currentTimeMillis(), // 🔹 Firestore Timestamp Compatibility
                dateParts[0] + "/" + dateParts[1] + "/" + year,
                false
        );

        db.collection("users")
                .document(userId)
                .collection("housetasks")
                .document(taskId)
                .set(newTask)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Task successfully added!");
                    listenForTaskUpdates(); // ✅ Ensures UI updates after adding a task
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding task", e));
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

        // ✅ Get updated task dates to highlight days with tasks
        Set<String> taskDates = getHomeTaskDates();

        Log.d("updateCalendar", "Highlighting Task Dates: " + taskDates);

        // ✅ Update calendar with highlighted task dates
        calendarAdapter.updateHomeTaskDates(taskDates);
        calendarAdapter.updateData(calendarDates);

        // ✅ Ensure today is visually selected
        int todayDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendarAdapter.setSelectedDate(String.valueOf(todayDay));

        // ✅ Ensure today's tasks are displayed immediately
        updateTasksForToday(todayDay);
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

    private int getPriorityValue(String priority) {
        switch (priority) {
            case "High": return 1; // 🔴 Red (Most Important)
            case "Medium": return 2; // 🟡 Yellow
            case "Low": return 3; // 🟢 Green (Least Important)
            default: return 4; // Fallback
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

    private void listenForTaskUpdates() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot listen for task updates");
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("housetasks")
                .whereEqualTo("completed", false) // ✅ Only fetch incomplete tasks
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Firestore Listener Failed.", error);
                        return;
                    }

                    if (value == null) {
                        Log.d("Firestore", "No tasks found");
                        return;
                    }

                    taskList.clear(); // ✅ Ensure we reset the task list
                    for (QueryDocumentSnapshot doc : value) {
                        Task task = doc.toObject(Task.class);
                        taskList.add(task);
                    }

                    // ✅ Ensure the calendar highlights dates with tasks
                    updateCalendar();
                });
    }




    @Override
    public void onResume() {
        super.onResume();
        Log.d("HomeCalendarFragment", "Resuming fragment, refreshing tasks.");
        loadTasksFromFirestore(); // Ensure tasks reload when fragment is resumed
    }

    private void loadTasksFromFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot fetch tasks");
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("housetasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    taskList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = doc.toObject(Task.class);
                        if (!task.isCompleted()) { // ✅ Only add incomplete tasks
                            taskList.add(task);
                        }
                    }

                    // ✅ Update calendar to highlight task dates
                    updateCalendar();

                    // ✅ Ensure today’s date is visually selected
                    int todayDay = calendar.get(Calendar.DAY_OF_MONTH);
                    calendarAdapter.setSelectedDate(String.valueOf(todayDay));

                    // ✅ Trigger today's tasks update AFTER Firestore data loads
                    updateTasksForToday(todayDay);

                    // ✅ Also update weekly tasks
                    updateWeeklyTasks();

                    Log.d("Firestore", "Tasks loaded. UI updated for today's tasks.");
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching tasks", e));
    }


    @Override
    public void onTaskCompleted(Task task) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot complete task");
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean newStatus = true; // ✅ Always set to TRUE when marking completed

        Log.d("TaskCompletion", "Marking task as completed: " + task.getTitle());
        Log.d("TaskCompletion", "Task ID: " + task.getId());

        db.collection("users")
                .document(userId)
                .collection("housetasks")
                .document(task.getId())
                .update("completed", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TaskCompletion", "Firestore update SUCCESSFUL for: " + task.getTitle());

                    // ✅ Remove the completed task from the list
                    taskList.removeIf(t -> t.getId().equals(task.getId()));

                    // ✅ Notify the adapter so the UI updates
                    taskAdapter.updateTasks(taskList);
                    taskAdapter.notifyDataSetChanged();

                    // ✅ Refresh Firestore listener to ensure UI updates
                    listenForTaskUpdates();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating task completion", e));
    }





    private void notifyTaskCompletionChanged() {
        if (getActivity() instanceof TaskCompletionListener) {
            ((TaskCompletionListener) getActivity()).onTaskCompletedUpdate();
        }

        // ✅ Force UI refresh for the completed tasks counter
        if (getView() != null) {
            TextView tasksCountTextView = getView().findViewById(R.id.tasks_count);
            if (tasksCountTextView != null) {
                tasksCountTextView.setText(String.valueOf(getCompletedTaskCount()));
            } else {
                Log.e("notifyTaskCompletion", "tasks_count TextView is null, cannot update.");
            }
        }
    }

    // ✅ Helper method to count completed tasks
    private int getCompletedTaskCount() {
        int count = 0;
        for (Task task : taskList) {
            if (task.isCompleted()) {
                count++;
            }
        }
        return count;
    }


    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        Context context = requireContext();

        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(ContextCompat.getColor(context, android.R.color.white));

        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(ContextCompat.getColor(context, R.color.dark_blue));
    }

}
