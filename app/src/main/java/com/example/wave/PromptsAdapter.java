package com.example.wave;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PromptsAdapter extends RecyclerView.Adapter<PromptsAdapter.ViewHolder> {

    private final List<String> promptsList;
    private final Context context;
    private final OnPromptClickListener listener;

    public interface OnPromptClickListener {
        void onPromptClicked(String prompt);
    }

    public PromptsAdapter(Context context, List<String> promptsList, OnPromptClickListener listener) {
        this.context = context;
        this.promptsList = promptsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ai_prompt, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String prompt = promptsList.get(position);
        holder.promptText.setText(prompt);

        // Handle click event
        holder.itemView.setOnClickListener(v -> listener.onPromptClicked(prompt));
    }

    @Override
    public int getItemCount() {
        return promptsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView promptText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            promptText = itemView.findViewById(R.id.promptText);
        }
    }
}

