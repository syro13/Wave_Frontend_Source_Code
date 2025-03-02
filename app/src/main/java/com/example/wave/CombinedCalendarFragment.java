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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
        loadTasksFromFirestore(); // ✅ Ensure Firestore tasks are loaded when fragment starts
        setupToggleButtons();
        return view;
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

        String taskCollection = "Home".equals(task.getCategory()) ? "housetasks" : "schooltasks";

        db.collection("users")
                .document(userId)
                .collection(taskCollection)
                .document(task.getId())
                .update("completed", true) // ✅ Mark as completed in Firestore
                .addOnSuccessListener(aVoid -> {
                    Log.d("TaskCompletion", "Task marked as completed: " + task.getTitle());

                    // ✅ Remove from relevant lists
                    schoolTaskList.removeIf(t -> t.getId().equals(task.getId()));
                    homeTaskList.removeIf(t -> t.getId().equals(task.getId()));
                    combinedTaskList.removeIf(t -> t.getId().equals(task.getId()));

                    // ✅ Refresh UI
                    updateCalendar();
                    updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
                    updateWeeklyTasks();
                    taskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating task completion", e));
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

    private void loadTasksFromFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot fetch tasks");
            return;
        }

        // Clear lists
        schoolTaskList.clear();
        homeTaskList.clear();
        combinedTaskList.clear();

        // Fetch School Tasks
        db.collection("users")
                .document(userId)
                .collection("schooltasks")
                .whereEqualTo("completed", false) // ✅ Fetch only incomplete tasks
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = doc.toObject(Task.class);
                        schoolTaskList.add(task);
                        combinedTaskList.add(task);
                    }

                    // Fetch Home Tasks after School Tasks
                    db.collection("users")
                            .document(userId)
                            .collection("housetasks")
                            .whereEqualTo("completed", false) // ✅ Fetch only incomplete tasks
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots2) {
                                    Task task = doc.toObject(Task.class);
                                    homeTaskList.add(task);
                                    combinedTaskList.add(task);
                                }

                                // ✅ Update UI
                                updateCalendar();
                                updateTasksForToday(calendar.get(Calendar.DAY_OF_MONTH));
                                updateWeeklyTasks();
                            })
                            .addOnFailureListener(e -> Log.e("Firestore", "Error fetching home tasks", e));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching school tasks", e));
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
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot delete task");
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        String taskCollection = "Home".equals(task.getCategory()) ? "housetasks" : "schooltasks";

        db.collection("users")
                .document(userId)
                .collection(taskCollection)
                .document(task.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("TaskDeletion", "Task deleted: " + task.getTitle());
                    combinedTaskList.removeIf(t -> t.getId().equals(task.getId()));
                    updateCalendar();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error deleting task", e));
    }


    @Override
    public void onTaskEdited(Task updatedTask, int position) {
        Log.d("CombinedCalendar", "onTaskEdited: " + updatedTask.getTitle());

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot update task");
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        String taskCollection = "Home".equals(updatedTask.getCategory()) ? "housetasks" : "schooltasks";

        db.collection("users")
                .document(userId)
                .collection(taskCollection)
                .document(updatedTask.getId())
                .set(updatedTask)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Task successfully updated: " + updatedTask.getTitle());

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
                        calendar.set(Calendar.MONTH, updatedLocalDate.getMonthValue() - 1);
                        calendar.set(Calendar.DAY_OF_MONTH, updatedLocalDate.getDayOfMonth());

                    } catch (Exception e) {
                        Log.e("CombinedCalendar", "Error updating calendar date: " + e.getMessage());
                    }

                    // Refresh everything
                    updateCalendar();
                    updateWeeklyTasks();
                    taskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating task", e));
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

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot filter tasks.");
            return results;
        }

        String taskCollection = "Both".equals(currentCategory) ? null :
                ("Home".equals(currentCategory) ? "housetasks" : "schooltasks");

        if (taskCollection != null) {
            // Fetch tasks from Firestore for the specific category
            db.collection("users")
                    .document(userId)
                    .collection(taskCollection)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        results.clear();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Task task = doc.toObject(Task.class);
                            if (Integer.parseInt(task.getDate()) == day && task.getMonth().equalsIgnoreCase(currentMonthName)) {
                                results.add(task);
                            }
                        }
                        taskAdapter.updateTasks(results);
                        taskAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error filtering tasks", e));
        } else {
            // Fetch both Home and School tasks
            db.collection("users").document(userId).collection("housetasks").get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Task task = doc.toObject(Task.class);
                            if (Integer.parseInt(task.getDate()) == day && task.getMonth().equalsIgnoreCase(currentMonthName)) {
                                results.add(task);
                            }
                        }
                        // Fetch School tasks after Home tasks are added
                        db.collection("users").document(userId).collection("schooltasks").get()
                                .addOnSuccessListener(querySnapshot2 -> {
                                    for (QueryDocumentSnapshot doc : querySnapshot2) {
                                        Task task = doc.toObject(Task.class);
                                        if (Integer.parseInt(task.getDate()) == day && task.getMonth().equalsIgnoreCase(currentMonthName)) {
                                            results.add(task);
                                        }
                                    }
                                    taskAdapter.updateTasks(results);
                                    taskAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching school tasks", e));
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error fetching home tasks", e));
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

        String selectedCategory = getCurrentSelectedCategory();
        List<Task> filteredTaskList;

        if ("School".equals(selectedCategory)) {
            filteredTaskList = new ArrayList<>(schoolTaskList);
        } else if ("Home".equals(selectedCategory)) {
            filteredTaskList = new ArrayList<>(homeTaskList);
        } else {
            filteredTaskList = new ArrayList<>(combinedTaskList);
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

        for (Task t : filteredTaskList) {
            try {
                String fullDate = t.getDate() + "/" + (getMonthIndex(t.getMonth()) + 1) + "/" + t.getYear();
                LocalDate taskDate = LocalDate.parse(fullDate, formatter);
                if (!taskDate.isBefore(startOfWeek) && !taskDate.isAfter(endOfWeek) && !t.isCompleted()) {
                    weeklyTasks.add(t);
                }
            } catch (DateTimeParseException e) {
                Log.e("updateWeeklyTasks", "Error parsing date: " + e.getMessage());
            }
        }

        // ✅ Sort by priority (High → Medium → Low)
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
            if (!task.isCompleted() && task.getMonth().equalsIgnoreCase(currentMonth) && task.getYear() == currentYear) {
                schoolTaskDates.add(task.getDate());
            }
        }

        Log.d("getSchoolTaskDates", "School Task Dates Highlighted: " + schoolTaskDates);
        return schoolTaskDates;
    }

    private Set<String> getHomeTaskDates() {
        Set<String> homeTaskDates = new HashSet<>();
        String currentMonth = getMonthYearList().get(calendar.get(Calendar.MONTH));
        int currentYear = calendar.get(Calendar.YEAR);

        for (Task task : homeTaskList) {
            if (!task.isCompleted() && task.getMonth().equalsIgnoreCase(currentMonth) && task.getYear() == currentYear) {
                homeTaskDates.add(task.getDate());
            }
        }

        Log.d("getHomeTaskDates", "Home Task Dates Highlighted: " + homeTaskDates);
        return homeTaskDates;
    }

    /**
     * Get task dates based on the current selected category (School, Home, or Both).
     */
    private Set<String> getCombinedTaskDates() {
        Set<String> combinedTaskDates = new HashSet<>();
        String selectedCategory = getCurrentSelectedCategory();

        if ("School".equals(selectedCategory)) {
            return getSchoolTaskDates(); // ✅ Only School tasks
        } else if ("Home".equals(selectedCategory)) {
            return getHomeTaskDates(); // ✅ Only Home tasks
        } else {
            // ✅ Both: Combine School & Home task dates
            combinedTaskDates.addAll(getSchoolTaskDates());
            combinedTaskDates.addAll(getHomeTaskDates());
        }

        Log.d("getCombinedTaskDates", "Combined Task Dates Highlighted: " + combinedTaskDates);
        return combinedTaskDates;
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
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot add task");
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse date input
        String[] dateParts = taskDate.split("/");
        if (dateParts.length != 3) {
            Toast.makeText(requireContext(), "Invalid date format!", Toast.LENGTH_SHORT).show();
            return;
        }

        int day = Integer.parseInt(dateParts[0]);
        int monthIndex = Integer.parseInt(dateParts[1]) - 1; // Convert 1-based to 0-based
        int year = Integer.parseInt(dateParts[2]);

        String taskId = UUID.randomUUID().toString();
        Task newTask = new Task(
                taskId,
                taskTitle,
                taskTime,
                String.valueOf(day),
                getMonthYearList().get(monthIndex),
                taskPriority,
                taskType,
                remind,
                year,
                0, // Default stability value, update as needed
                System.currentTimeMillis(), // Set task timestamp to current time
                taskDate, // Full formatted date
                false // Not completed
        );

        // Determine the correct Firestore collection based on task type
        String taskCollection = "Home".equals(taskType) ? "housetasks" : "schooltasks";

        db.collection("users")
                .document(userId)
                .collection(taskCollection)
                .document(taskId)
                .set(newTask)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Task successfully added!");

                    // Add locally to keep UI in sync
                    if ("School".equals(taskType)) {
                        schoolTaskList.add(newTask);
                    } else {
                        homeTaskList.add(newTask);
                    }
                    combinedTaskList.add(newTask);

                    // Refresh UI
                    updateCalendar();
                    updateCurrentTasks();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding task", e));
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
