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

public class HomeTasksFragment extends Fragment implements GroceryItemAdapter.SaveGroceryItemsCallback{
    private NetworkReceiver networkReceiver;
    private static final String GroceryListPREFS_NAME = "GroceryListPrefs";
    private static final String GROCERY_LIST_KEY = "grocery_list";
    private Dialog dialog;
    private ArrayList<GroceryItem> groceryItems; // Grocery list items
    private GroceryItemAdapter adapter;
    private static final int MAX_BLOGS = 4;
    private static final String PREFS_NAME = "HomeTasksPrefs";
    private RecyclerView  promptsRecyclerView;

    private List<String> displayPromptsList;
    private List<String> actualPromptsList;

    private PromptsAdapter promptsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_house_tasks, container, false);

        TextView schoolTasksButton = view.findViewById(R.id.SchoolTasksButton);
        TextView homeTasksButton = view.findViewById(R.id.homeTasksButton);

        // RecyclerView setup for articles

        // RecyclerView setup for AI prompts
        promptsRecyclerView = view.findViewById(R.id.promptsRecyclerView);
        promptsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        displayPromptsList = Arrays.asList(
                "Suggest a quick cleaning tip",
                "How can I stay organized in my space?",
                "Give me advice on balancing chores with studying",
                "How to make cleaning less stressful?",
                "Share a tip for keeping my home clean and tidy"
        );
        // Detailed prompt text for the API call:
        actualPromptsList = Arrays.asList(
                "As a home care expert, provide a quick cleaning tip that can be implemented in under 5 minutes. Limit the response to a maximum of 200 words.",
                "Provide a comprehensive yet succinct strategy for staying organized at home, including time management and space optimization tips. Limit your answer to 200 words.",
                "Develop advice for balancing household chores with other responsibilities in a realistic manner. Limit your response to 200 words.",
                "Outline practical ways to reduce the stress associated with cleaning, including simple methods for quick fixes. Limit your answer to 200 words.",
                "Share an effective tip for maintaining a clean home environment that is easy to follow and implement. Limit the answer to 200 words."
        );

        promptsAdapter = new PromptsAdapter(displayPromptsList, actualPromptsList, this::showPopup);
        promptsRecyclerView.setAdapter(promptsAdapter);

        setActiveButton(homeTasksButton, schoolTasksButton);

        // Handle School Tasks button click
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
        return view;
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
        requireContext().unregisterReceiver(networkReceiver);
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
            holder.itemView.setOnClickListener(v ->
                    listener.onClick(displayPrompts.get(position), actualPrompts.get(position)));
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

    // AIContentDialog Fragment to display the prompt and the AI response.
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

    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_blue));
    }

    public interface OnFetchCompleteListener {
        void onFetchComplete(boolean success);
    }
}
