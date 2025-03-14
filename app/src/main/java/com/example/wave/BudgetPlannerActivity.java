package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class BudgetPlannerActivity extends BaseActivity {

    private SemiCircularProgressView semiCircularProgressView;
    private FirebaseFirestore db;
    private TextView amountSpentValue, amountLeftValue, totalBudgetValue;
    private FloatingActionButton addExpenseButton;
    private Button editBudgetButton;
    private RecyclerView expensesRecyclerView, promptsRecyclerView;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList;
    private PromptsAdapter promptsAdapter;
    private List<String> displayPromptsList;
    private List<String> actualPromptsList;
    private double totalBudget = 0;
    private double amountSpent = 0;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_planner);

        // Firestore instance
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User is not logged in!", Toast.LENGTH_LONG).show();
        } else {
            Log.d("DEBUG", "User ID: " + auth.getCurrentUser().getUid());
        }


        // Initialize UI elements
        semiCircularProgressView = findViewById(R.id.budgetSemiCircleChart);
        amountSpentValue = findViewById(R.id.amountSpentValue);
        amountLeftValue = findViewById(R.id.amountLeftValue);
        totalBudgetValue = findViewById(R.id.totalBudgetValue);
        addExpenseButton = findViewById(R.id.addExpenseButton);
        editBudgetButton = findViewById(R.id.editBudgetButton);
        expensesRecyclerView = findViewById(R.id.expensesRecyclerView);
        promptsRecyclerView = findViewById(R.id.promptsRecyclerView);


        // Set up RecyclerView for expenses
        expensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(expenseList, totalBudget);
        expensesRecyclerView.setAdapter(expenseAdapter);

        promptsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupPrompts();

        // Always initialize expense categories, even before budget
        initializeExpenseCategories();

         // Ensure budget exists
        ensureBudgetDocumentExists();
        // Check if it's Monday to reset budget
        checkAndResetBudget();
        // Load budget data from Firebase
        loadBudgetData();

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation(bottomNavigationView);

        FragmentManager fragmentManager = getSupportFragmentManager();

        addExpenseButton.setOnClickListener(v -> {
            DocumentReference budgetRef = db.collection("users")
                    .document(userId)
                    .collection("budget")
                    .document("data");

            budgetRef.get().addOnSuccessListener(documentSnapshot -> {
                double currentBudget = documentSnapshot.exists() ? documentSnapshot.getDouble("totalBudget") : 0.0;

                if (currentBudget == 0) {
                    Toast.makeText(this, "Set a budget before adding expenses!", Toast.LENGTH_LONG).show();
                } else {
                    AddExpenseBottomSheetFragment addExpenseFragment = new AddExpenseBottomSheetFragment();
                    addExpenseFragment.show(getSupportFragmentManager(), "AddExpenseBottomSheetFragment");
                }
            });
        });


        // Edit Budget Button
        editBudgetButton.setOnClickListener(v -> {
            SetBudgetBottomSheetFragment setBudgetFragment = new SetBudgetBottomSheetFragment();
            setBudgetFragment.show(getSupportFragmentManager(), "SetBudgetBottomSheetFragment");
        });

        // Handle result from AddExpenseBottomSheetFragment
        fragmentManager.setFragmentResultListener("expense_request", this, (requestKey, result) -> {
            String amount = result.getString("expense_amount");
            String category = result.getString("expense_category");
            updateExpense(category, Double.parseDouble(amount));
        });

        // Handle result from SetBudgetBottomSheetFragment
        fragmentManager.setFragmentResultListener("budget_request", this, (requestKey, result) -> {
            String amount = result.getString("budget_amount");
            updateTotalBudget(Double.parseDouble(amount));
        });

        findViewById(R.id.profileIcon).setOnClickListener(v -> {
            Intent intent = new Intent(BudgetPlannerActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

    }
    private void ensureBudgetDocumentExists() {
        DocumentReference budgetRef = db.collection("users")
                .document(userId)
                .collection("budget")
                .document("data");

        budgetRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Map<String, Object> budgetData = new HashMap<>();
                budgetData.put("totalBudget", 0);
                budgetData.put("amountSpent", 0);
                budgetRef.set(budgetData);
            }
        });
    }

    private void initializeExpenseCategories() {
        String[] categories = {"Bills", "Food", "Groceries", "Health", "Miscellaneous", "Shopping", "Travel"};

        for (String category : categories) {
            DocumentReference categoryRef = db.collection("users")
                    .document(userId)
                    .collection("budget")
                    .document("data")
                    .collection("expenses")
                    .document(category);

            categoryRef.get().addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                    Map<String, Object> categoryData = new HashMap<>();
                    categoryData.put("amount", 0);
                    categoryRef.set(categoryData);
                }
            });
        }
    }

    /**
     * Fetch budget data from Firebase and update UI.
     */
    private void loadBudgetData() {
        DocumentReference budgetRef = db.collection("users")
                .document(userId)
                .collection("budget")
                .document("data");

        budgetRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(this, "Error loading budget!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                totalBudget = documentSnapshot.getDouble("totalBudget") != null ? documentSnapshot.getDouble("totalBudget") : 0.0;
                amountSpent = documentSnapshot.getDouble("amountSpent") != null ? documentSnapshot.getDouble("amountSpent") : 0.0;

                // Update UI elements
                updateUI();

                // Load expenses
                loadExpenses();
            }
        });
    }
    private void loadExpenses() {
        db.collection("users").document(userId)
                .collection("budget").document("data")
                .collection("expenses")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        expenseList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String categoryName = document.getId();
                            double spentAmount = document.getDouble("amount") != null ? document.getDouble("amount") : 0.0;
                            double percentageSpent = (totalBudget > 0) ? (spentAmount / totalBudget) * 100 : 0;
                            expenseList.add(new Expense(categoryName, spentAmount));
                        }
                        expenseAdapter.setTotalBudget(totalBudget);
                        expenseAdapter.updateExpenseList(expenseList);
                    }
                });
    }


    private void updateExpense(String category, double amount) {
        DocumentReference budgetRef = db.collection("users")
                .document(userId)
                .collection("budget")
                .document("data");

        DocumentReference categoryRef = budgetRef.collection("expenses").document(category);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot budgetSnapshot = transaction.get(budgetRef);
            DocumentSnapshot categorySnapshot = transaction.get(categoryRef);

            double currentTotalBudget = budgetSnapshot.getDouble("totalBudget");
            double newSpent = budgetSnapshot.getDouble("amountSpent") + amount;
            double newCategorySpent = categorySnapshot.exists() ? categorySnapshot.getDouble("amount") + amount : amount;

            if (newSpent > currentTotalBudget) {
                throw new FirebaseFirestoreException("Over budget!", FirebaseFirestoreException.Code.ABORTED);
            }

            transaction.update(budgetRef, "amountSpent", newSpent);
            transaction.set(categoryRef, new HashMap<String, Object>() {{
                put("amount", newCategorySpent);
            }}, SetOptions.merge());

            return null;
        }).addOnSuccessListener(aVoid -> {
            // Refresh UI
            loadBudgetData();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to update expense: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }


    private void updateTotalBudget(double newBudget) {
        if (newBudget < amountSpent) {
            Toast.makeText(this, "Budget cannot be lower than spent amount!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .collection("budget")
                .document("data")
                .set(new HashMap<String, Object>() {{
                    put("totalBudget", newBudget);
                }}, SetOptions.merge())
                .addOnSuccessListener(aVoid -> loadBudgetData())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update budget: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }


    private void updateButtonStates(double totalBudget, double amountSpent, float progress) {
        if (progress >= 100 || (amountSpent >= totalBudget && totalBudget != 0)) {
            addExpenseButton.setEnabled(false);
            editBudgetButton.setEnabled(false);
            Toast.makeText(this, "Over budget! No more expenses allowed until reset.", Toast.LENGTH_LONG).show();
        } else if (totalBudget == 0) {
            addExpenseButton.setEnabled(true);
        } else {
            editBudgetButton.setEnabled(true);
            addExpenseButton.setEnabled(totalBudget > amountSpent);
        }
    }


    private void checkAndResetBudget() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            DocumentReference budgetRef = db.collection("users")
                    .document(userId)
                    .collection("budget")
                    .document("data");

            budgetRef.update("amountSpent", 0);

            db.collection("users").document(userId)
                    .collection("budget").document("data")
                    .collection("expenses")
                    .get().addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            document.getReference().update("amount", 0);
                        }
                    });

            Toast.makeText(this, "Budget reset for the new week!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        float progress = (totalBudget > 0) ? (float) ((amountSpent / totalBudget) * 100) : 0;
        progress = Math.min(progress, 100);

        semiCircularProgressView.setProgress(progress);
        amountSpentValue.setText("€" + String.format("%.2f", amountSpent));
        amountLeftValue.setText("€" + String.format("%.2f", Math.max(totalBudget - amountSpent, 0)));
        totalBudgetValue.setText("€" + String.format("%.2f", totalBudget));

        updateButtonStates(totalBudget, amountSpent, progress);
    }
    @Override
    protected int getCurrentMenuItemId() {
        return -1; // No selection
    }

    private void setupPrompts() {
        displayPromptsList = Arrays.asList(
                "Suggest a budgeting tip",
                "How can I save money effectively?",
                "Give me advice on managing my monthly expenses",
                "How to reduce unnecessary spending?",
                "Share a strategy for long-term financial planning"
        );

        actualPromptsList = Arrays.asList(
                "Provide a quick and effective budgeting technique to help users manage their money wisely. Focus on practical strategies.",
                "Suggest a step-by-step approach to saving money effectively, including actionable savings habits.",
                "Offer expert advice on balancing income and expenses to prevent overspending and financial stress.",
                "Explain common spending pitfalls and provide methods to avoid them while maintaining a balanced budget.",
                "Describe a structured approach for long-term financial stability, including investments, savings, and financial discipline."
        );

        promptsAdapter = new PromptsAdapter(displayPromptsList, actualPromptsList, this::showPopup);
        promptsRecyclerView.setAdapter(promptsAdapter);
    }
    private void showPopup(String displayPrompt, String actualPrompt) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://updatedservice-621971573276.us-central1.run.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SchoolTasksFragment.AIService aiService = retrofit.create(SchoolTasksFragment.AIService.class);
        SchoolTasksFragment.AIPromptRequest request = new SchoolTasksFragment.AIPromptRequest(actualPrompt);

        aiService.getAIResponse(request).enqueue(new Callback<SchoolTasksFragment.AIResponse>() {
            @Override
            public void onResponse(Call<SchoolTasksFragment.AIResponse> call, Response<SchoolTasksFragment.AIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String aiResponse = response.body().getResponse();
                    SchoolTasksFragment.AIContentDialog dialog = SchoolTasksFragment.AIContentDialog.newInstance(displayPrompt, aiResponse);
                    dialog.show(getSupportFragmentManager(), "AIContentDialog");
                } else {
                    Toast.makeText(BudgetPlannerActivity.this, "Failed to get AI response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SchoolTasksFragment.AIResponse> call, Throwable t) {
                Toast.makeText(BudgetPlannerActivity.this, "Error connecting to AI server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Prompts RecyclerView Adapter
    public static class PromptsAdapter extends RecyclerView.Adapter<SchoolTasksFragment.PromptsAdapter.ViewHolder> {
        private final List<String> displayPrompts;
        private final List<String> actualPrompts;
        private final SchoolTasksFragment.PromptsAdapter.OnPromptClickListener listener;

        public PromptsAdapter(List<String> displayPrompts, List<String> actualPrompts, SchoolTasksFragment.PromptsAdapter.OnPromptClickListener listener) {
            if (displayPrompts.size() != actualPrompts.size()) {
                throw new IllegalArgumentException("Both lists must have the same number of items.");
            }
            this.displayPrompts = displayPrompts;
            this.actualPrompts = actualPrompts;
            this.listener = listener;
        }

        @NonNull
        @Override
        public SchoolTasksFragment.PromptsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ai_prompt, parent, false);
            return new SchoolTasksFragment.PromptsAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SchoolTasksFragment.PromptsAdapter.ViewHolder holder, int position) {
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

        public static SchoolTasksFragment.AIContentDialog newInstance(String title, String content) {
            SchoolTasksFragment.AIContentDialog dialog = new SchoolTasksFragment.AIContentDialog();
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
        Call<SchoolTasksFragment.AIResponse> getAIResponse(@Body SchoolTasksFragment.AIPromptRequest request);
    }

}
