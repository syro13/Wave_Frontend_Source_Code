package com.example.wave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SmartBreakdownBottomSheet extends BottomSheetDialogFragment {
    public interface Listener {
        void onBreakdownReady(String breakdown) ;
    }
    private Listener listener;

    public void setListener(Listener l) {
        listener = l;
    }

    private Spinner spinnerTask, spinnerTime;
    private RadioGroup moodGroup;
    private EditText inputDetails;
    private Button btnGenerate;
    private ChipGroup chipGroupTaskType;
    private EditText inputDeadline;
    private Calendar deadlineCalendar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater i, ViewGroup c, Bundle s) {
        View v = i.inflate(R.layout.bottom_sheet_smart_breakdown, c, false);
        spinnerTask = v.findViewById(R.id.spinnerTaskList);
        spinnerTime = v.findViewById(R.id.spinnerTime);
        moodGroup   = v.findViewById(R.id.moodGroup);
        inputDetails= v.findViewById(R.id.inputDetails);
        btnGenerate = v.findViewById(R.id.btnGenerateBreakdown);
        chipGroupTaskType = v.findViewById(R.id.chipGroupTaskType);
        inputDeadline = v.findViewById(R.id.inputDeadline);

        deadlineCalendar = Calendar.getInstance();

        inputDeadline.setOnClickListener(view -> showDateTimePicker());

        initTaskTypeChips();

        loadTasksIntoSpinner();
        btnGenerate.setOnClickListener(x -> validateAndRequestBreakdown());
        return v;
    }

    private void showDateTimePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            deadlineCalendar.set(Calendar.YEAR, year);
            deadlineCalendar.set(Calendar.MONTH, month);
            deadlineCalendar.set(Calendar.DAY_OF_MONTH, day);

            new TimePickerDialog(requireContext(), (timeView, hour, minute) -> {
                deadlineCalendar.set(Calendar.HOUR_OF_DAY, hour);
                deadlineCalendar.set(Calendar.MINUTE, minute);

                inputDeadline.setText(String.format("%d/%d/%d %d:%02d",
                        month + 1, day, year, hour, minute));
            }, deadlineCalendar.get(Calendar.HOUR_OF_DAY),
                    deadlineCalendar.get(Calendar.MINUTE), true).show();
        },
                deadlineCalendar.get(Calendar.YEAR),
                deadlineCalendar.get(Calendar.MONTH),
                deadlineCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void initTaskTypeChips() {
        String[] taskTypes = {"Essay", "Research", "Presentation", "Project", "Exam Prep", "Lab Work", "Reading"};
        for (String type : taskTypes) {
            Chip chip = new Chip(requireContext());
            chip.setText(type);
            chip.setCheckable(true);
            chipGroupTaskType.addView(chip);
        }
    }

    private void loadTasksIntoSpinner() {
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance()
                .collection("users").document(uid).collection("schooltasks")
                .get().addOnSuccessListener(snap -> {
                    List<String> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc: snap)
                        list.add(doc.toObject(Task.class).getTitle());
                    spinnerTask.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, list));
                });
    }

    private void validateAndRequestBreakdown() {
        if (spinnerTask.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Please select a task", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasTaskType = false;
        StringBuilder taskTypes = new StringBuilder();
        for (int i = 0; i < chipGroupTaskType.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTaskType.getChildAt(i);
            if (chip.isChecked()) {
                hasTaskType = true;
                if (taskTypes.length() > 0) taskTypes.append(", ");
                taskTypes.append(chip.getText());
            }
        }

        if (!hasTaskType) {
            Toast.makeText(getContext(), "Please select at least one task type", Toast.LENGTH_SHORT).show();
            return;
        }

        requestBreakdown(taskTypes.toString());
    }

    private void requestBreakdown(String taskTypes) {
        String task = spinnerTask.getSelectedItem().toString();
        String time = spinnerTime.getSelectedItem().toString();
        int id = moodGroup.getCheckedRadioButtonId();
        String mood = id == R.id.rbHappy ? "happy" : id == R.id.rbSad ? "sad" : "neutral";
        String details = inputDetails.getText().toString().trim();
        String deadline = inputDeadline.getText().toString().trim();

        int availableHours = extractTimeValue(time);
        int availableMinutes = availableHours * 60;

        String specificTaskPrompt = getTaskSpecificPrompt(taskTypes);

        String prompt = String.format(
                "You are an expert academic planner who loves to add fun and motivational emojis.\n\n" +
                        "TASK: '%s'\n" +
                        "TASK TYPE: '%s'\n" +
                        "DETAILS: '%s'\n" +
                        "DEADLINE: '%s'\n" +
                        "MOOD: '%s' (provide relevant tips)\n" +
                        "AVAILABLE TIME: %d minutes (strict limit)\n\n" +
                        "ADDITIONAL TASK GUIDANCE:\n%s\n\n" +
                        "Create a smart breakdown with a list of subtasks. " +
                        "Each subtask should include:\n" +
                        "• Duration in minutes (must sum to ≤ %d)\n" +
                        "• A short, clear 'Goal' or deliverable\n" +
                        "• A brief 'How' with actionable guidance\n" +
                        "• A small motivational tip referencing the mood\n" +
                        "Include an emoji at the start of each subtask name for visual appeal (e.g., 📝 for writing, 🔍 for research, etc.).\n\n" +
                        "Output must be plain text (no Markdown) with each subtask on a new line.\n" +
                        "Example format:\n\n" +
                        "1) 📝 Subtask Name (XX minutes): Goal: [Brief outcome], How: [Steps to complete], Tip: [Motivational message]\n" +
                        "2) 🔍 Another Subtask (XX minutes): Goal: [Brief outcome], How: [Steps to complete], Tip: [Motivational message]\n\n" +
                        "TOTAL TIME: [X] minutes (must be ≤ %d)\n",
                task, taskTypes, details, deadline, mood, availableMinutes,
                specificTaskPrompt, availableMinutes, availableMinutes
        );

        btnGenerate.setEnabled(false);
        btnGenerate.setText("Generating...");

        new Retrofit.Builder()
                .baseUrl("https://updatedservice-621971573276.us-central1.run.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SchoolTasksFragment.AIService.class)
                .getAIResponse(new SchoolTasksFragment.AIPromptRequest(prompt))
                .enqueue(new Callback<SchoolTasksFragment.AIResponse>() {
                    @Override
                    public void onResponse(Call<SchoolTasksFragment.AIResponse> c, Response<SchoolTasksFragment.AIResponse> r) {
                        btnGenerate.setEnabled(true);
                        btnGenerate.setText("Generate Breakdown");

                        if (r.isSuccessful() && r.body() != null && listener != null) {
                            String response = r.body().getResponse();

                            if (validateTimeAllocation(response, availableHours)) {
                                listener.onBreakdownReady(response);
                                dismiss();

                                saveBreakdownToHistory(task, taskTypes, response);
                            } else {
                                requestBreakdownWithStricterConstraints(task, taskTypes, time, mood, details, deadline, availableHours);
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to get AI response", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<SchoolTasksFragment.AIResponse> c, Throwable t) {
                        btnGenerate.setEnabled(true);
                        btnGenerate.setText("Generate Breakdown");
                        Toast.makeText(getContext(), "Error generating breakdown", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void requestBreakdownWithStricterConstraints(String task, String taskTypes, String time,
                                                         String mood, String details, String deadline, int availableHours) {
        int totalMinutes = availableHours * 60;

        String specificTaskPrompt = getTaskSpecificPrompt(taskTypes);

        String strictPrompt = String.format(
                "CRITICAL: Create a time-strict breakdown with the following constraints:\n\n" +
                        "TASK: '%s'\n" +
                        "TYPE: '%s'\n" +
                        "DETAILS: '%s'\n" +
                        "DEADLINE: '%s'\n" +
                        "MOOD: '%s'\n\n" +
                        "ADDITIONAL TASK GUIDANCE:\n%s\n\n" +
                        "REQUIREMENTS:\n" +
                        "• Create 4-8 subtasks\n" +
                        "• Each subtask MUST have a specific duration in minutes (TOTAL must be ≤ %d minutes)\n" +
                        "• Focus on practical, achievable steps\n" +
                        "• Include a 'Goal', 'How' instructions, and a motivational tip with an emoji for visual appeal\n\n" +
                        "FORMAT:\n" +
                        "1) [Subtask Name] (XX minutes): Goal: [Brief outcome], How: [Brief guidance], Tip: [Motivational message]\n" +
                        "2) [Subtask Name] (XX minutes): Goal: [Brief outcome], How: [Brief guidance], Tip: [Motivational message]\n\n" +
                        "TOTAL TIME: [X] minutes (must be ≤ %d)",
                task, taskTypes, details, deadline, mood,
                specificTaskPrompt, totalMinutes, totalMinutes
        );

        new Retrofit.Builder()
                .baseUrl("https://updatedservice-621971573276.us-central1.run.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SchoolTasksFragment.AIService.class)
                .getAIResponse(new SchoolTasksFragment.AIPromptRequest(strictPrompt))
                .enqueue(new Callback<SchoolTasksFragment.AIResponse>() {
                    @Override
                    public void onResponse(Call<SchoolTasksFragment.AIResponse> c, Response<SchoolTasksFragment.AIResponse> r) {
                        if (r.isSuccessful() && r.body() != null && listener != null) {
                            String response = r.body().getResponse();
                            listener.onBreakdownReady(response);

                            saveBreakdownToHistory(task, taskTypes, response);

                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Failed to generate suitable breakdown", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<SchoolTasksFragment.AIResponse> c, Throwable t) {
                        Toast.makeText(getContext(), "Error generating breakdown", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getTaskSpecificPrompt(String taskTypes) {
        Map<String, String> taskPrompts = new HashMap<>();
        taskPrompts.put("Essay", "• Divide into Research, Outlining, Drafting, and Editing phases\n• Include specific sections (Intro, Body paragraphs, Conclusion)\n• Suggest topic narrowing techniques");
        taskPrompts.put("Research", "• Include literature search, note-taking, and synthesis phases\n• Suggest search strategies for academic databases\n• Break down reading into manageable chunks");
        taskPrompts.put("Presentation", "• Include content planning, slide creation, and practice sessions\n• Suggest effective visual aids and practice techniques\n• Include time for tech setup and rehearsal");
        taskPrompts.put("Project", "• Structure with planning, execution, and review phases\n• Include checkpoints for feedback\n• Suggest collaboration strategies if applicable");
        taskPrompts.put("Exam Prep", "• Organize by topic/chapter with review sessions\n• Include practice test time\n• Suggest memory techniques for key concepts");
        taskPrompts.put("Lab Work", "• Include pre-lab preparation, execution, and report writing\n• Break down complex procedures into steps\n• Include data analysis and interpretation time");
        taskPrompts.put("Reading", "• Divide text into manageable sections\n• Include note-taking and summary writing\n• Suggest active reading techniques and reflection questions");

        StringBuilder result = new StringBuilder();
        for (String type : taskTypes.split(", ")) {
            if (taskPrompts.containsKey(type)) {
                result.append(type).append(":\n").append(taskPrompts.get(type)).append("\n\n");
            }
        }

        return result.toString();
    }

    private void saveBreakdownToHistory(String taskName, String taskType, String breakdown) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        Map<String, Object> historyItem = new HashMap<>();
        historyItem.put("taskName", taskName);
        historyItem.put("taskType", taskType);
        historyItem.put("breakdown", breakdown);
        historyItem.put("timestamp", Calendar.getInstance().getTimeInMillis());

        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("breakdownHistory")
                .add(historyItem);
    }

    private int extractTimeValue(String timeString) {
        try {
            return Integer.parseInt(timeString.split(" ")[0]);
        } catch (Exception e) {
            return 1;
        }
    }

    private boolean validateTimeAllocation(String response, int availableHours) {
        int totalMinutes = 0;
        Pattern pattern = Pattern.compile("\\((\\d+)\\s*m(?:in|ins|inutes)?\\)");
        Matcher matcher = pattern.matcher(response);

        while (matcher.find()) {
            try {
                int minutes = Integer.parseInt(matcher.group(1));
                totalMinutes += minutes;
            } catch (NumberFormatException e) {
            }
        }

        return totalMinutes <= (availableHours * 60);
    }
}