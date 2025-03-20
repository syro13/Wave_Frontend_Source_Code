package com.example.wave;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Prompts RecyclerView Adapter
public class AIPromptsAdapter extends RecyclerView.Adapter<AIPromptsAdapter.ViewHolder> {
    private final List<String> displayPrompts;
    private final List<String> actualPrompts;
    private final AIPromptsAdapter.OnPromptClickListener listener;

    public AIPromptsAdapter(List<String> displayPrompts, List<String> actualPrompts, OnPromptClickListener listener) {
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


