package com.example.wave;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

// AIContentDialog Fragment
public class AIContentDialog extends DialogFragment {
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
