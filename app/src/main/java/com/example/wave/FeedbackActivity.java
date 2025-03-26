package com.example.wave;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wave.utils.GMailSender;

public class FeedbackActivity extends BaseActivity {


    private EditText feedbackInput;
    private Button submitButton;
    private ImageView backButton;
    private ImageView[] stars = new ImageView[5];
    private int selectedRating = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedbackInput = findViewById(R.id.feedbackInput);
        submitButton = findViewById(R.id.submitButton);
        backButton = findViewById(R.id.backButton);

        stars[0] = findViewById(R.id.star1);
        stars[1] = findViewById(R.id.star2);
        stars[2] = findViewById(R.id.star3);
        stars[3] = findViewById(R.id.star4);
        stars[4] = findViewById(R.id.star5);

        for (int i = 0; i < stars.length; i++) {
            final int index = i;
            stars[i].setOnClickListener(v -> {
                selectedRating = index + 1;
                updateStarUI(selectedRating);
            });
        }

        setNoInternetOverlay(findViewById(R.id.noInternetOverlay));
        configureNoInternetOverlay();

        backButton.setOnClickListener(v -> finish());

        submitButton.setOnClickListener(v -> handleFeedbackSubmission());
    }
    @Override
    protected int getCurrentMenuItemId() {
        return -1; // No selection
    }
    private void updateStarUI(int rating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled); // Your blue star
            } else {
                stars[i].setImageResource(R.drawable.ic_star_gray);   // Gray star
            }
        }
    }

    private void handleFeedbackSubmission() {
        int rating = selectedRating;
        String feedbackText = feedbackInput.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please rate your experience.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(feedbackText)) {
            feedbackInput.setError("Please enter feedback.");
            feedbackInput.requestFocus();
            return;
        }

        String subject = "Wave App - User Feedback";
        String body = "⭐ Rating: " + rating + " stars\n\nFeedback:\n" + feedbackText;

        // Run in background thread
        new Thread(() -> {
            try {
                GMailSender sender = new GMailSender("contactus.wave.zircon@gmail.com", "mcwlgjajearmizjv");
                sender.sendMail(subject, body, "contactus.wave.zircon@gmail.com");

                runOnUiThread(() -> Toast.makeText(this, "Feedback sent. Thank you!", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to send feedback.", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

}
