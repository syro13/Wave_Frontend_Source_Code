package com.example.wave;

import static android.content.Context.MODE_PRIVATE;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;


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

public class SchoolTasksFragment extends Fragment  {
    private RecyclerView  promptsRecyclerView;
    private List<String> displayPromptsList;
    private List<String> actualPromptsList;
    private static final String SchoolNotesPREFS_NAME = "SchoolNotesPrefs";
    private static final String SCHOOL_NOTES_KEY = "school_notes";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration schoolTasksListener; // For real-time updates
    private ArrayList<GroceryItem> groceryItems;
    private GroceryItemAdapter adapter;
    private List<Task> completedTaskList;
    private ProgressBar loadingIndicator;
    private View loadingOverlay;
    private AIPromptsAdapter promptsAdapter;
    private View noInternetOverlay;
    private NetworkReceiver networkReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_school_tasks, container, false);

        TextView schoolTasksButton = view.findViewById(R.id.SchoolTasksButton);
        TextView homeTasksButton = view.findViewById(R.id.homeTasksButton);
        setupSchoolNotesCard(view);
        Button openBreakdown = view.findViewById(R.id.btnOpenSmartBreakdown);
        openBreakdown.setOnClickListener(v -> {
            SmartBreakdownBottomSheet sheet = new SmartBreakdownBottomSheet();
            sheet.setListener(result -> {
                TextView tv = getView().findViewById(R.id.textBreakdownResult);
                tv.setText(result);

                 CardView cardBreakdown = getView().findViewById(R.id.cardBreakdownResult);
                cardBreakdown.setVisibility(View.VISIBLE);

                requireContext().getSharedPreferences("SmartBreakdownPrefs", MODE_PRIVATE)
                        .edit().putString("breakdownText", result).apply();
            });
            sheet.show(getParentFragmentManager(), "SmartBreakdown");
        });



        BaseActivity baseActivity = (BaseActivity) requireActivity();
        baseActivity.setNoInternetOverlay(view.findViewById(R.id.noInternetOverlay));
        baseActivity.configureNoInternetOverlay();
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        requireContext().registerReceiver(networkReceiver, filter);

        checkInitialInternetStatus();

        // RecyclerView setup for AI prompts

        promptsRecyclerView = view.findViewById(R.id.promptsRecyclerView);
        promptsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setupPrompts();

        // Set initial active state for buttons
        setActiveButton(schoolTasksButton, homeTasksButton);

        // School Tasks Button Click
        schoolTasksButton.setOnClickListener(v -> {
            setActiveButton(schoolTasksButton, homeTasksButton);
            if (getActivity() instanceof SchoolHomeTasksActivity) {
                ((SchoolHomeTasksActivity) getActivity()).showSchoolTasksFragment();
            }
        });

        // Home Tasks Button Click
        homeTasksButton.setOnClickListener(v -> {
            setActiveButton(homeTasksButton, schoolTasksButton);
            if (getActivity() instanceof SchoolHomeTasksActivity) {
                ((SchoolHomeTasksActivity) getActivity()).showHomeTasksFragment();
            }
        });
        ImageView profileIcon = view.findViewById(R.id.profileIcon);

        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProfileActivity.class);
            startActivity(intent);
        });
        CardView calendarFromTasksButton = view.findViewById(R.id.CalendarFromTasksButton); // ✅ CardView
        if (calendarFromTasksButton == null) {
            Log.e("SchoolTasksFragment", "CalendarFromTasksButton CardView NOT found in XML!");
        } else {
            Log.d("SchoolTasksFragment", "CalendarFromTasksButton CardView found, setting click listener...");

            calendarFromTasksButton.setOnClickListener(v -> {
                Log.d("SchoolTasksFragment", "CalendarFromTasksButton clicked, navigating to HomeCalendarFragment...");

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.home_school_tasks_container, new SchoolCalendarFragment()) // Ensure correct ID
                        .addToBackStack(null) // Allows back navigation
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

    private void setupSchoolNotesCard(View view) {
        CardView schoolNotesCard = view.findViewById(R.id.schoolNotesCard); // Ensure this ID exists in XML
        schoolNotesCard.setOnClickListener(v -> showSchoolNotesPopup());
    }

    private void showSchoolNotesPopup() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.school_notes_popup); // Ensure this XML file exists

        ImageView backArrow = dialog.findViewById(R.id.back_arrow);
        TextView title = dialog.findViewById(R.id.popup_title);
        EditText schoolNoteInput = dialog.findViewById(R.id.school_note_input);
        Button addSchoolNote = dialog.findViewById(R.id.add_school_note);
        ListView schoolNotesList = dialog.findViewById(R.id.school_notes_list);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width  = (int) (getResources().getDisplayMetrics().widthPixels  * 0.95); // 95% of screen width
        lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.80); // 80% of screen height
        dialog.getWindow().setAttributes(lp);

        // Set title
        title.setText("Notes");

        // Load saved notes
        ArrayList<String> schoolNotes = getSchoolNotes();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, schoolNotes);
        schoolNotesList.setAdapter(adapter);

        // Handle adding a note
        addSchoolNote.setOnClickListener(v -> {
            String newNote = schoolNoteInput.getText().toString().trim();
            if (!newNote.isEmpty()) {
                schoolNotes.add("• " + newNote); // Add bullet point
                adapter.notifyDataSetChanged();
                schoolNoteInput.setText("");
                saveSchoolNotes(schoolNotes);
            }
        });
    }

    interface OnFetchCompleteListener {
        void onFetchComplete(boolean success);
    }


    private void saveSchoolNotes(ArrayList<String> schoolNotes) {
        SharedPreferences prefs = requireContext().getSharedPreferences(SchoolNotesPREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> set = new HashSet<>(schoolNotes);
        editor.putStringSet(SCHOOL_NOTES_KEY, set);
        editor.apply();
    }

    private ArrayList<String> getSchoolNotes() {
        SharedPreferences prefs = requireContext().getSharedPreferences(SchoolNotesPREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet(SCHOOL_NOTES_KEY, new HashSet<>());
        return new ArrayList<>(set);
    }


    private void setupPrompts() {
        displayPromptsList = Arrays.asList(
                "How to manage stress during study sessions?",
                "What are some effective study and motivation tips?",
                "Share a quick mindfulness technique to refocus",
                "How can I improve my sleep as a student?",
                "Suggest a balanced daily routine for students"
        );

        actualPromptsList = Arrays.asList(
                "Provide simple and effective stress management techniques that students can use daily to stay mentally healthy.",
                "Give motivation strategies and study techniques that can help students stay consistent and avoid burnout.",
                "Suggest a short mindfulness or breathing exercise that students can do to relax and refocus their minds.",
                "Explain practical steps to improve sleep quality, especially for students struggling with irregular sleep patterns.",
                "Describe an ideal daily routine for students that balances academics, self-care, and physical well-being."
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

        com.example.wave.AIService aiService = retrofit.create(com.example.wave.AIService.class);
        com.example.wave.AIPromptRequest request = new com.example.wave.AIPromptRequest(actualPrompt);

        aiService.getAIResponse(request).enqueue(new Callback<com.example.wave.AIResponse>() {
            @Override
            public void onResponse(Call<com.example.wave.AIResponse> call, Response<com.example.wave.AIResponse> response) {

                // Hide ProgressBar once response is received
                loadingIndicator.setVisibility(View.GONE);
                loadingOverlay.setVisibility(View.GONE); // Re-enable interactions
                if (response.isSuccessful() && response.body() != null) {
                    String aiResponse = response.body().getResponse();
                    com.example.wave.AIContentDialog dialog = com.example.wave.AIContentDialog.newInstance(displayPrompt, aiResponse);
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
            public void onFailure(Call<com.example.wave.AIResponse> call, Throwable t) {
                // Hide ProgressBar in case of failure
                loadingIndicator.setVisibility(View.GONE);
                loadingOverlay.setVisibility(View.GONE);
                promptsRecyclerView.setEnabled(true);
            }
        });
    }
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
                    // Assuming the TextView for cancelled school tasks count has the ID "tasks_cancelled_school_count"
                    TextView cancelledTasksCountTextView = getView().findViewById(R.id.tasks_cancelled_count);
                    if (cancelledTasksCountTextView != null) {
                        cancelledTasksCountTextView.setText(String.valueOf(cancelledCount));
                    }
                })
                .addOnFailureListener(e -> Log.e("SchoolTasksFragment", "Error fetching cancelled school tasks", e));
    }

    @Override
    public void onStart() {
        super.onStart();
        startSchoolTasksListener();  // Begin listening for changes in "schooltasks"
        updatePendingTasksCount();
        updateCancelledTasksCount();
        updateOverdueTasksCount();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (schoolTasksListener != null) {
            schoolTasksListener.remove();
            schoolTasksListener = null;
        }
    }
    private void updatePendingTasksCount() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (userId == null) {
            Log.e("SchoolTasksFragment", "User not logged in, cannot fetch pending tasks");
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("schooltasks") // or "housetasks" in HomeTasksFragment
                .whereEqualTo("completed", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int pendingCount = queryDocumentSnapshots.size();
                    Log.d("SchoolTasksFragment", "Pending tasks count: " + pendingCount);

                    // Find the TextView in your layout
                    TextView tasksPendingTextView = getView().findViewById(R.id.tasks_pending_count);
                    if (tasksPendingTextView != null) {
                        // Update the text with the number of pending tasks
                        tasksPendingTextView.setText(String.valueOf(pendingCount));
                    }
                })
                .addOnFailureListener(e -> Log.e("SchoolTasksFragment", "Error fetching pending tasks", e));
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

    // Helper method to convert a month name to its corresponding index (1-12)
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


    private void startSchoolTasksListener() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("SchoolTasksFragment", "User not logged in, skipping school tasks listener.");
            return;
        }

        schoolTasksListener = db.collection("users")
                .document(userId)
                .collection("schooltasks")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("SchoolTasksFragment", "Error listening for school tasks", e);
                        return;
                    }

                    // Whenever tasks change, re-count completed tasks
                    updateCompletedTasksCount();
                });
    }


    // New method: updateCompletedTasksCount()
// This queries Firestore for completed school tasks and updates the tasks_count TextView in the School Tasks page.
    private void updateCompletedTasksCount() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("schooltasks")
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
                .addOnFailureListener(e -> Log.e("SchoolTasksFragment", "Error fetching completed tasks", e));
    }

    // AIContentDialog Fragment
    public static class AIContentDialog extends DialogFragment {
        private static final String TITLE_KEY = "title";
        private static final String CONTENT_KEY = "content";

        public static AIContentDialog newInstance(String title, String content) {
            AIContentDialog dialog = new AIContentDialog();
            Bundle args = new Bundle();
            args.putString(TITLE_KEY, title);
            args.putString(CONTENT_KEY, content);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.dialog_ai_content, container, false);

            TextView titleView = view.findViewById(R.id.dialogTitle);
            TextView contentView = view.findViewById(R.id.dialogContent);
            View closeButton = view.findViewById(R.id.dialogCloseButton);

            assert getArguments() != null;
            titleView.setText(getArguments().getString(TITLE_KEY));
            contentView.setText(getArguments().getString(CONTENT_KEY));
            ScrollingMovementMethod scrollable = new ScrollingMovementMethod();
            contentView.setMovementMethod(scrollable);


            closeButton.setOnClickListener(v -> dismiss());
            return view;
        }
    }

    // --- AI API Models and Interface ---

    public static class AIPromptRequest {
        private String prompt;

        public AIPromptRequest(String prompt) {
            this.prompt = prompt;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }

    public static class AIResponse {
        private String response;

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }

    public interface AIService {
        @POST("run-prompt")
        Call<AIResponse> getAIResponse(@Body AIPromptRequest request);
    }

    /**
     * Updates the styles of the toggle buttons to show which one is active.
     *
     * @param activeButton   The button to mark as active
     * @param inactiveButton The button to mark as inactive
     */
    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        // Apply active button styles
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        // Apply inactive button styles
        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_blue));
    }
}
