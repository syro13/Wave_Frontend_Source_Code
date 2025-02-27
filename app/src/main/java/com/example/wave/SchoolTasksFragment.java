package com.example.wave;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
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
    private PromptsAdapter promptsAdapter;
    private List<String> displayPromptsList;
    private List<String> actualPromptsList;
    private static final String SchoolNotesPREFS_NAME = "SchoolNotesPrefs";
    private static final String SCHOOL_NOTES_KEY = "school_notes";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_school_tasks, container, false);

        TextView schoolTasksButton = view.findViewById(R.id.SchoolTasksButton);
        TextView homeTasksButton = view.findViewById(R.id.homeTasksButton);
        setupSchoolNotesCard(view);

        // RecyclerView setup for AI prompts
        promptsRecyclerView = view.findViewById(R.id.promptsRecyclerView);
        promptsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        displayPromptsList = Arrays.asList(
                "Suggest a quick study tip",
                "How can I stay organized in my studies?",
                "Give me advice on balancing chores with studying",
                "How to make studying less stressful?",
                "Share a tip for keeping my work clean and tidy"
        );
        actualPromptsList = Arrays.asList(
                "As a student counselor, share a quick, research-backed study technique that helps students maximize learning in minimal time. Focus on cognitive science principles of memory and retention.",
                "Provide a comprehensive yet concise organizational system for a college student struggling with managing multiple courses, assignments, and personal commitments. Include digital and physical organization techniques.",
                "Develop a realistic approach for a student balancing academic workload with personal responsibilities. Include time-blocking techniques, prioritization methods, and stress reduction strategies.",
                "Outline practical, immediate stress-reduction techniques specifically tailored to academic environments. Address mental health, study environment optimization, and emotional regulation.",
                "Create a systematic approach to maintaining a clean, productive study space that enhances focus and reduces mental clutter. Include both physical organization and psychological strategies."
        );

        promptsAdapter = new PromptsAdapter(displayPromptsList, actualPromptsList, this::showPopup);
        promptsRecyclerView.setAdapter(promptsAdapter);

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

        return view;
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
        lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85); // 85% of screen width
        lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.65);
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

        // Handle back button
        backArrow.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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

    private void showPopup(String displayPrompt, String actualPrompt) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://updatedservice-621971573276.us-central1.run.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AIService aiService = retrofit.create(AIService.class);
        AIPromptRequest request = new AIPromptRequest(actualPrompt);

        aiService.getAIResponse(request).enqueue(new Callback<AIResponse>() {
            @Override
            public void onResponse(Call<AIResponse> call, Response<AIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String aiResponse = response.body().getResponse();
                    AIContentDialog dialog = AIContentDialog.newInstance(displayPrompt, aiResponse);
                    dialog.show(getParentFragmentManager(), "AIContentDialog");
                } else {
                    Toast.makeText(getContext(), "Failed to get AI response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AIResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error connecting to AI server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Prompts RecyclerView Adapter
    public static class PromptsAdapter extends RecyclerView.Adapter<PromptsAdapter.ViewHolder> {
        private final List<String> displayPrompts;
        private final List<String> actualPrompts;
        private final OnPromptClickListener listener;

        public PromptsAdapter(List<String> displayPrompts, List<String> actualPrompts, OnPromptClickListener listener) {
            if (displayPrompts.size() != actualPrompts.size()) {
                throw new IllegalArgumentException("Both lists must have the same number of items.");
            }
            this.displayPrompts = displayPrompts;
            this.actualPrompts = actualPrompts;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ai_prompt, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String displayText = displayPrompts.get(position);
            holder.promptText.setText(displayText);
            holder.itemView.setOnClickListener(v -> listener.onClick(displayPrompts.get(position), actualPrompts.get(position)));
        }

        @Override
        public int getItemCount() {
            return displayPrompts.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView promptText;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                promptText = itemView.findViewById(R.id.promptText);
            }
        }

        public interface OnPromptClickListener {
            void onClick(String displayPrompt, String actualPrompt);
        }
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
