package com.example.wave;

import static android.content.Context.MODE_PRIVATE;
import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class HomeTasksFragment extends Fragment implements GroceryItemAdapter.SaveGroceryItemsCallback, TaskCompletionListener{
    private NetworkReceiver networkReceiver;
    private static final String GroceryListPREFS_NAME = "GroceryListPrefs";
    private static final String GROCERY_LIST_KEY = "grocery_list";
    private Dialog dialog;
    private ArrayList<GroceryItem> groceryItems;
    private GroceryItemAdapter adapter;
    private static final int MAX_BLOGS = 4;
    private static final String PREFS_NAME = "HomeTasksPrefs";
    private RecyclerView promptsRecyclerView;
    private List<Task> completedTaskList;
    private ProgressBar loadingIndicator;
    private View noInternetOverlay;
    private View loadingOverlay;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration homeTasksListener;
    private AIPromptsAdapter promptsAdapter;
    private List<String> displayPromptsList;
    private List<String> actualPromptsList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_house_tasks, container, false);

        TextView schoolTasksButton = view.findViewById(R.id.SchoolTasksButton);
        TextView homeTasksButton = view.findViewById(R.id.homeTasksButton);

        BaseActivity baseActivity = (BaseActivity) requireActivity();
        baseActivity.setNoInternetOverlay(view.findViewById(R.id.noInternetOverlay));
        baseActivity.configureNoInternetOverlay();
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        requireContext().registerReceiver(networkReceiver, filter);

        checkInitialInternetStatus();

        completedTaskList = new ArrayList<>();
        promptsRecyclerView = view.findViewById(R.id.promptsRecyclerView);
        promptsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setupPrompts();

        setActiveButton(homeTasksButton, schoolTasksButton);

        Button openHouseBreakdown = view.findViewById(R.id.btnOpenSmartBreakdown);
        openHouseBreakdown.setOnClickListener(v -> {
            HouseBreakdownBottomSheet sheet = new HouseBreakdownBottomSheet();
            sheet.setListener(result -> {
                TextView tv = getView().findViewById(R.id.textBreakdownResult);
                tv.setText(result);
                CardView cardHouseBreakdown = getView().findViewById(R.id.cardBreakdownResult);
                cardHouseBreakdown.setVisibility(View.VISIBLE);

                requireContext().getSharedPreferences("HouseBreakdownPrefs", MODE_PRIVATE)
                        .edit().putString("houseBreakdownText", result).apply();
            });
            sheet.show(getParentFragmentManager(), "HouseBreakdown");
        });


        schoolTasksButton.setOnClickListener(v -> {
            setActiveButton(schoolTasksButton, homeTasksButton);
            if (getActivity() instanceof SchoolHomeTasksActivity) {
                ((SchoolHomeTasksActivity) getActivity()).showSchoolTasksFragment();
            }
        });

        ImageView profileIcon = view.findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProfileActivity.class);
            startActivity(intent);
        });

        setupNotesCard(view);
        listenForCompletedTaskUpdates();

        CardView calendarFromTasksButton = view.findViewById(R.id.CalendarFromTasksButton);
        if (calendarFromTasksButton != null) {
            calendarFromTasksButton.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.home_school_tasks_container, new HomeCalendarFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
    }

    private void checkInitialInternetStatus() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        if (!isConnected) {
            showNoInternetUI();
        } else {
            hideNoInternetUI();
        }
    }


    private void showNoInternetUI() {
        if (noInternetOverlay != null) {
            noInternetOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void hideNoInternetUI() {
        if (noInternetOverlay != null) {
            noInternetOverlay.setVisibility(View.GONE);
        }
    }

    private void showLoading() {
        if (loadingIndicator != null) loadingIndicator.setVisibility(View.VISIBLE);
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
    }

    public HomeTasksFragment() {
        // Required empty public constructor
    }

    private void setupNotesCard(View view) {
        CardView notesCard = view.findViewById(R.id.groceryCard);
        notesCard.setOnClickListener(v -> showGroceryListPopup());
    }

    private void showGroceryListPopup() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.grocery_list_popup);

        EditText groceryItemInput = dialog.findViewById(R.id.grocery_item_input);
        ListView groceryListItems = dialog.findViewById(R.id.grocery_list_items);
        Button addGroceryItem = dialog.findViewById(R.id.add_grocery_item);
        ImageView closeButton = dialog.findViewById(R.id.close_popup);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);

        groceryItems = getGroceryItems(); // Initialize the grocery list
        adapter = new GroceryItemAdapter(getContext(), groceryItems, this);
        groceryListItems.setAdapter(adapter);

        addGroceryItem.setOnClickListener(v -> {
            String newItemText = groceryItemInput.getText().toString().trim();
            if (!newItemText.isEmpty()) {
                groceryItems.add(new GroceryItem(newItemText, false));
                adapter.notifyDataSetChanged();
                groceryItemInput.setText("");
                saveGroceryItems(groceryItems);
            }
        });

        groceryItemInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addGroceryItem.callOnClick();
                return true;
            }
            return false;
        });

        closeButton.setOnClickListener(v -> {
            saveGroceryItems(groceryItems);
            dialog.dismiss();
        });

        dialog.setOnDismissListener(dialogInterface -> saveGroceryItems(groceryItems));
        dialog.show();
    }

    @Override
    public void saveGroceryItems(ArrayList<GroceryItem> groceryItems) {
        SharedPreferences prefs = getContext().getSharedPreferences(GroceryListPREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> set = new HashSet<>();
        for (GroceryItem item : groceryItems) {
            set.add(item.text + ":" + item.checked);
        }
        editor.putStringSet(GROCERY_LIST_KEY, set);
        editor.apply();
    }

    private ArrayList<GroceryItem> getGroceryItems() {
        SharedPreferences prefs = getContext().getSharedPreferences(GroceryListPREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet(GROCERY_LIST_KEY, new HashSet<>());
        ArrayList<GroceryItem> list = new ArrayList<>();
        if (set != null) {
            for (String s : set) {
                String[] parts = s.split(":");
                if (parts.length == 2) {
                    list.add(new GroceryItem(parts[0], Boolean.parseBoolean(parts[1])));
                }
            }
        }
        return list;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (networkReceiver != null) {
            requireContext().unregisterReceiver(networkReceiver);
        }
    }

    private void setupPrompts() {
        displayPromptsList = Arrays.asList(
                "How can I stay on top of daily chores?",
                "What's a quick way to organize my room?",
                "Give me tips for staying motivated with housework",
                "How do I manage my time between chores and rest?",
                "Suggest a productive daily home routine"
        );

        actualPromptsList = Arrays.asList(
                "Provide effective strategies to help students consistently stay on top of their daily household chores.",
                "Share quick and practical steps students can take to keep their rooms tidy and organized.",
                "Give motivation techniques that can help students stay consistent with home responsibilities like cleaning and laundry.",
                "Explain how students can manage their time efficiently to balance housework and relaxation without burnout.",
                "Describe a realistic and balanced daily home routine for students that includes time for cleaning, rest, and productivity."
        );

        promptsAdapter = new AIPromptsAdapter(displayPromptsList, actualPromptsList, this::showPopup);
        promptsRecyclerView.setAdapter(promptsAdapter);
    }

    private void showPopup(String displayPrompt, String actualPrompt) {
        // Disable prompt clicking to prevent multiple selections
        promptsRecyclerView.setEnabled(false);
        loadingOverlay.setVisibility(View.VISIBLE);
        // Show ProgressBar when fetching AI response
        loadingIndicator.setVisibility(View.VISIBLE);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://updatedservice-621971573276.us-central1.run.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AIService aiService = retrofit.create(AIService.class);
        AIPromptRequest request = new AIPromptRequest(actualPrompt);

        aiService.getAIResponse(request).enqueue(new Callback<AIResponse>() {
            @Override
            public void onResponse(Call<AIResponse> call, Response<AIResponse> response) {

                // Hide ProgressBar once response is received
                loadingIndicator.setVisibility(View.GONE);
                loadingOverlay.setVisibility(View.GONE); // Re-enable interactions
                if (response.isSuccessful() && response.body() != null) {
                    String aiResponse = response.body().getResponse();
                    AIContentDialog dialog = AIContentDialog.newInstance(displayPrompt, aiResponse);
                    // Show dialog and re-enable clicks only after it's dismissed
                    dialog.show(getParentFragmentManager(), "AIContentDialog");
                    requireActivity().getSupportFragmentManager().executePendingTransactions();

                    dialog.getDialog().setOnDismissListener(dialogInterface -> {
                        promptsRecyclerView.setEnabled(true); // Re-enable clicks after dialog closes
                    });
                } else {
                    // Hide ProgressBar in case of failure
                    loadingIndicator.setVisibility(View.GONE);
                    loadingOverlay.setVisibility(View.GONE);
                    promptsRecyclerView.setEnabled(true);

                }
            }

            @Override
            public void onFailure(Call<AIResponse> call, Throwable t) {
                // Hide ProgressBar in case of failure
                loadingIndicator.setVisibility(View.GONE);
                loadingOverlay.setVisibility(View.GONE);
                promptsRecyclerView.setEnabled(true);
            }
        });
    }
    public void listenForCompletedTaskUpdates() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot listen for completed task updates");
            return;
        }

        Log.d("Firestore", "Listening for completed task updates");
        db.collection("users")
                .document(userId)
                .collection("housetasks")
                .whereEqualTo("completed", true)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }

                    if (value == null) {
                        Log.d("Firestore", "No completed tasks found");
                        return;
                    }

                    int completedCount = value.size();
                    Log.d("Firestore", "Realtime Completed tasks count: " + completedCount);

                    if (getView() != null) {
                        TextView tasksCountTextView = getView().findViewById(R.id.tasks_count);
                        if (tasksCountTextView != null) {
                            tasksCountTextView.setText(String.valueOf(completedCount));
                        }
                    }

                    // ✅ Refresh UI: Remove completed tasks from the active task list
                    List<Task> newTaskList = new ArrayList<>();
                    for (Task task : completedTaskList) {
                        if (!task.isCompleted()) {
                            newTaskList.add(task);
                        }
                    }

                    completedTaskList = newTaskList; // ✅ Update the completed task list
                    updateCompletedTaskCount(); // ✅ Refresh the UI count
                });
    }
    @Override
    public void onStart() {
        super.onStart();
        startHomeTasksListener();  // Begin listening for changes in "schooltasks"
        updatePendingTasksCount();
        updateCancelledTasksCount();
        updateOverdueTasksCount();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (homeTasksListener != null) {
            homeTasksListener.remove();
            homeTasksListener = null;
        }
    }
    // --- NEW method to update the Cancelled Tasks Card ---
    private void updateCancelledTasksCount() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("cancelledHomeTasks")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int cancelledCount = querySnapshot.size();
                    // Assuming you have a TextView in your layout with the ID "cancelledTasksCountView"
                    TextView cancelledTasksCountView = getView().findViewById(R.id.tasks_cancelled_count);
                    if (cancelledTasksCountView != null) {
                        cancelledTasksCountView.setText(String.valueOf(cancelledCount));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeTasksFragment", "Error updating cancelled tasks", e);
                });
    }
    private void updateOverdueTasksCount() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (userId == null) return;

        // Query all uncompleted school tasks.
        db.collection("users")
                .document(userId)
                .collection("schooltasks")
                .whereEqualTo("completed", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int overdueCount = 0;
                    // We'll assume the time is stored in 24-hour format (e.g., "12:16")
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    // Iterate over each task document.
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = doc.toObject(Task.class);
                        try {
                            int day = Integer.parseInt(task.getDate());
                            int month = getMonthIndex(task.getMonth());
                            int year = task.getYear();

                            java.time.LocalTime dueTime = java.time.LocalTime.parse(task.getTime(), timeFormatter);
                            java.time.LocalDate dueDate = java.time.LocalDate.of(year, month, day);
                            java.time.LocalDateTime dueDateTime = java.time.LocalDateTime.of(dueDate, dueTime);

                            // If the task's due datetime is before now, it's overdue.
                            if (dueDateTime.isBefore(java.time.LocalDateTime.now())) {
                                overdueCount++;
                            }
                        } catch (Exception e) {
                            Log.e("OverdueCount", "Error parsing task for overdue count: " + task.getTitle(), e);
                        }
                    }

                    // Update the overdue count TextView.
                    View view = getView();
                    if (view != null) {
                        TextView overdueCountTextView = view.findViewById(R.id.tasks_overdue_count);
                        if (overdueCountTextView != null) {
                            overdueCountTextView.setText(String.valueOf(overdueCount));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("SchoolTasksFragment", "Error fetching tasks for overdue count", e));
    }

    private int getMonthIndex(String month) {
        switch (month.toLowerCase()) {
            case "january":   return 1;
            case "february":  return 2;
            case "march":     return 3;
            case "april":     return 4;
            case "may":       return 5;
            case "june":      return 6;
            case "july":      return 7;
            case "august":    return 8;
            case "september": return 9;
            case "october":   return 10;
            case "november":  return 11;
            case "december":  return 12;
            default:          return 0;
        }
    }

    // New method: updateCompletedTasksCount()
// This queries Firestore for completed school tasks and updates the tasks_count TextView in the School Tasks page.
    private void updateCompletedTaskCount() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("housetasks")
                .whereEqualTo("completed", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int completedCount = queryDocumentSnapshots.size();
                    // Assuming the completed tasks card's TextView has the ID "tasks_count"
                    TextView tasksCountTextView = getView().findViewById(R.id.tasks_count);
                    if (tasksCountTextView != null) {
                        tasksCountTextView.setText(String.valueOf(completedCount));
                    }
                })
                .addOnFailureListener(e -> Log.e("HouseTasksFragment", "Error fetching completed tasks", e));
    }

    private void startHomeTasksListener() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("HomeTasksFragment", "User not logged in, skipping home tasks listener.");
            return;
        }

        homeTasksListener = db.collection("users")
                .document(userId)
                .collection("housetasks")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("HomeTasksFragment", "Error listening for home tasks", e);
                        return;
                    }

                    // Whenever tasks change, re-count completed tasks
                    updateCompletedTaskCount();
                });
    }

    private void updatePendingTasksCount() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (userId == null) {
            Log.e("HomeTasksFragment", "User not logged in, cannot fetch pending tasks");
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("housetasks") // or "housetasks" in HomeTasksFragment
                .whereEqualTo("completed", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int pendingCount = queryDocumentSnapshots.size();
                    Log.d("HomeTasksFragment", "Pending tasks count: " + pendingCount);

                    // Find the TextView in your layout
                    TextView tasksPendingTextView = getView().findViewById(R.id.tasks_pending_count);
                    if (tasksPendingTextView != null) {
                        // Update the text with the number of pending tasks
                        tasksPendingTextView.setText(String.valueOf(pendingCount));
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeTasksFragment", "Error fetching pending tasks", e));
    }


    @Override
    public void onTaskCompletedUpdate() {
        Log.d("HomeTasksFragment", "Task completed, refreshing UI...");

        // ✅ Listen for task updates
        listenForCompletedTaskUpdates();

        // ✅ Refresh UI to remove completed tasks
        if (getView() != null) {
            RecyclerView tasksRecyclerView = getView().findViewById(R.id.taskRecyclerView);
            if (tasksRecyclerView != null) {
                tasksRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }
    }



    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        Context context = requireContext();
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(ContextCompat.getColor(context, android.R.color.white));

        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(ContextCompat.getColor(context, R.color.dark_blue));
    }


    public interface OnFetchCompleteListener {
        void onFetchComplete(boolean success);
    }
}
