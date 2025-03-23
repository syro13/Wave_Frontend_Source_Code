package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
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
import com.google.firebase.firestore.CollectionReference;
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
    private ProgressBar loadingIndicator;
    private View loadingOverlay;
    private AIPromptsAdapter promptsAdapter;
    private List<String> displayPromptsList;
    private List<String> actualPromptsList;
    private double totalBudget = 0;
    private double amountSpent = 0;
    private String userId;
    private TextView aiSuggestionContent;


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
        loadingIndicator = findViewById(R.id.loadingIndicator);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        promptsRecyclerView = findViewById(R.id.promptsRecyclerView);


        // Set up RecyclerView for expenses
        expensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(expenseList, totalBudget);
        expensesRecyclerView.setAdapter(expenseAdapter);

        promptsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupPrompts();
        aiSuggestionContent = findViewById(R.id.aiSuggestionContent);

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
        fetchAISuggestions();
    }
    @Override
    protected int getCurrentMenuItemId() {
        return -1; // No selection
    }
    private void fetchAISuggestions() {
        if (totalBudget <= 0) {
            aiSuggestionContent.setText("Set a budget first to get tailored tips.");
            return;
        }

        DocumentReference budgetDocRef = db.collection("users")
                .document(userId)
                .collection("budget")
                .document("data");

        CollectionReference expensesRef = budgetDocRef.collection("expenses");

        expensesRef.get().addOnSuccessListener(expenseSnapshot -> {
                    Map<String, Double> categorySpending = new HashMap<>();
                    double totalSpent = 0;

                    for (QueryDocumentSnapshot doc : expenseSnapshot) {
                        String category = doc.getId(); // Using document ID as category
                        double amount = doc.getDouble("amount") != null ? doc.getDouble("amount") : 0;
                        categorySpending.put(category, amount);
                        totalSpent += amount;
                    }

                    double remainingBudget = totalBudget - totalSpent;

                    StringBuilder breakdown = new StringBuilder();
                    for (Map.Entry<String, Double> entry : categorySpending.entrySet()) {
                        String category = entry.getKey();
                        double spent = entry.getValue();
                        double percentOfTotal = (spent / totalBudget) * 100;

                        breakdown.append(category)
                                .append(": €").append(String.format("%.2f", spent))
                                .append(" (").append(String.format("%.1f", percentOfTotal)).append("% of budget)")
                                .append("; ");
                    }

                    String prompt = String.format(
                            "I need SPECIFIC and PERSONALIZED budget suggestions for this exact situation:\n\n" +
                                    "Weekly budget: €%.2f\n" +
                                    "Total spent so far: €%.2f (%.1f%% of budget)\n" +
                                    "Remaining budget: €%.2f (%.1f%% of budget)\n\n" +
                                    "Category breakdown:\n%s\n\n" +
                                    "Based ONLY on these exact numbers and categories, provide 3 SPECIFIC suggestions that:\n" +
                                    "1. Directly reference actual amounts and categories from the data\n" +
                                    "2. Include specific euro amounts and percentages in the recommendations\n" +
                                    "3. Suggest concrete actions for the remaining €%.2f\n\n" +
                                    "AVOID generic advice. Each suggestion must mention specific categories and amounts from the data.",
                            totalBudget,
                            totalSpent,
                            (totalSpent / totalBudget) * 100,
                            remainingBudget,
                            (remainingBudget / totalBudget) * 100,
                            breakdown.toString().trim(),
                            remainingBudget
                    );

                    callAIService(prompt);
                })
                .addOnFailureListener(e ->
                        aiSuggestionContent.setText("Failed to load expenses for AI suggestions.")
                );
    }

    private void callAIService(String prompt) {
        aiSuggestionContent.setText("Creating personalized suggestions...");

        Log.d("BudgetAI", "Sending prompt: " + prompt);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://updatedservice-621971573276.us-central1.run.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AIService aiService = retrofit.create(AIService.class);
        aiService.getAIResponse(new AIPromptRequest(prompt)).enqueue(new Callback<AIResponse>() {
            @Override
            public void onResponse(Call<AIResponse> call, Response<AIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String aiResponse = response.body().getResponse().trim()
                            .replaceAll("^\\d+\\.\\s*", "")
                            .replaceAll("(?i)Suggestion \\d+:\\s*", "")
                            .replaceAll("(?m)^", "")
                            .trim();

                    String[] suggestions = response.body().getResponse().trim().split("\\n\\n");
                    StringBuilder formattedResponse = new StringBuilder();
                    int max = Math.min(suggestions.length, 3);

                    for (int i = 0; i < max; i++) {
                        String clean = suggestions[i]
                                .replaceAll("(?i)^(Suggestion\\s*\\d+:\\s*|\\d+\\.\\s*)", "")
                                .trim();

                        formattedResponse
                                .append(i + 1)
                                .append(". ")
                                .append(clean);

                        if (i < max - 1) {
                            formattedResponse.append("\n\n");
                        }
                    }

                    aiSuggestionContent.setText(formattedResponse.toString());

                } else {
                    aiSuggestionContent.setText("Unable to load suggestions right now.");
                }
            }
            @Override
            public void onFailure(Call<AIResponse> call, Throwable t) {
                aiSuggestionContent.setText("Error fetching suggestions.");
                Log.e("BudgetAI", "AI call failed", t);
            }
        });
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

        promptsAdapter = new AIPromptsAdapter(displayPromptsList, actualPromptsList, this::showPopup);
        promptsRecyclerView.setAdapter(promptsAdapter);
    }
    private void showPopup(String displayPrompt, String actualPrompt) {
        // Disable prompt clicking to prevent multiple selections
        promptsRecyclerView.setEnabled(false);
        addExpenseButton.setVisibility(View.GONE);
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
                addExpenseButton.setVisibility(View.VISIBLE); // Show FAB again
                if (response.isSuccessful() && response.body() != null) {
                    String aiResponse = response.body().getResponse();
                  AIContentDialog dialog = AIContentDialog.newInstance(displayPrompt, aiResponse);
                    // Show dialog and re-enable clicks only after it's dismissed
                    dialog.show(getSupportFragmentManager(), "AIContentDialog");
                    getSupportFragmentManager().executePendingTransactions();

                    dialog.getDialog().setOnDismissListener(dialogInterface -> {
                        promptsRecyclerView.setEnabled(true); // Re-enable clicks after dialog closes
                    });
                } else {
                    // Hide ProgressBar in case of failure
                    loadingIndicator.setVisibility(View.GONE);
                    loadingOverlay.setVisibility(View.GONE);
                    promptsRecyclerView.setEnabled(true);
                    addExpenseButton.setVisibility(View.VISIBLE); // Show FAB again
                    Toast.makeText(BudgetPlannerActivity.this, "Failed to get AI response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AIResponse> call, Throwable t) {
                // Hide ProgressBar in case of failure
                loadingIndicator.setVisibility(View.GONE);
                loadingOverlay.setVisibility(View.GONE);
                promptsRecyclerView.setEnabled(true);
                addExpenseButton.setVisibility(View.VISIBLE); // Show FAB again
                Toast.makeText(BudgetPlannerActivity.this, "Error connecting to AI server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
