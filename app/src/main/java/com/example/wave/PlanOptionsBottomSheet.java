package com.example.wave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PlanOptionsBottomSheet extends BottomSheetDialogFragment {
    public interface Listener {
        void onOptionsSelected(String mood, String time, String thoughts);
    }
    private Listener listener;
    public void setListener(Listener listener) { this.listener = listener; }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_plan_options, container, false);
        RadioGroup moodGroup = view.findViewById(R.id.moodGroup);
        Spinner timeSpinner = view.findViewById(R.id.spinnerTime);
        Button submit = view.findViewById(R.id.btnSubmitPlan);

        EditText thoughtsInput = view.findViewById(R.id.inputThoughts);

        submit.setOnClickListener(v -> {
            int id = moodGroup.getCheckedRadioButtonId();
            String mood = id == R.id.rbHappy ? "Happy" : id == R.id.rbNeutral ? "Neutral" : "Sad";
            String time = timeSpinner.getSelectedItem().toString();
            String thoughts = thoughtsInput.getText().toString().trim();
            if (listener != null) listener.onOptionsSelected(mood, time, thoughts);
            dismiss();
        });

        return view;
    }
}
