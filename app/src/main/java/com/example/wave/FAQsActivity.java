package com.example.wave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FAQsActivity extends BaseActivity {

    private LinearLayout faqContainer;
    private ImageView backButton;

    private final String[][] faqs = new String[][]{
            {"What is Wave?", "Wave is an all-in-one mobile application designed to support students by providing tools for budgeting, task management, wellness tracking, and academic assistance. It aims to simplify student life, making it easier to manage daily activities and achieve personal and academic goals."},
            {"How do I get started with Wave?", "Getting started is easy! Download the app, create an account, and you’ll be guided through a setup process to personalize your experience based on your educational and lifestyle needs."},
            {"Is Wave free to use?", "Yes, Wave is free for all students. We offer a premium version with additional features that you can subscribe to for an enhanced experience."},
            {"Can Wave help me manage my tasks?", "Absolutely! Wave is tailored for individual task management, allowing you to efficiently organize and manage both your school and household tasks. Although it's not designed for group projects, it's perfect for keeping your personal tasks organized and on schedule."},
            {"How does Wave help with budgeting?", "Wave includes a budgeting tool that helps you track your expenses and manage your finances. Set your budget, categorize your spending, and keep track of your expenses to stay financially healthy while studying."},
            {"What wellness features does Wave offer?", "Wave supports your wellness journey by offering a curated selection of motivational quotes, insightful blogs, and inspiring podcasts. These resources are designed to enhance your mental and physical well-being, keeping you motivated and informed."},
            {"How secure is my personal information with Wave?", " We take your privacy and security seriously. Your personal information is encrypted and protected with the latest security measures available, ensuring that your data remains private and secure."},
            {"I forgot my password, what should I do?", "No worries, simply click on the \"Forgot Password?\" link on the login page and follow the instructions to reset your password. You’ll need access to your registered email address to receive the reset link."},
            {"Can I access Wave on multiple devices?", "Yes, you can access Wave on multiple devices. Your data is synchronized across all devices, so you can stay organized whether you’re on your phone, tablet, or computer."},
            {"Who can I contact if I have issues using Wave?", " If you encounter any issues or have questions, feel free to contact our support team at support@waveapp.com. We're here to help ensure your experience is smooth and beneficial."}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqs);

        setNoInternetOverlay(findViewById(R.id.noInternetOverlay));
        configureNoInternetOverlay();

        faqContainer = findViewById(R.id.faqContainer);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> onBackPressed());

        for (String[] pair : faqs) {
            addFaqItem(pair[0], pair[1]);
        }
    }
    @Override
    protected int getCurrentMenuItemId() {
        return -1; // No selection
    }
    private void addFaqItem(String question, String answer) {
        View faqItem = LayoutInflater.from(this).inflate(R.layout.faq_item, faqContainer, false);
        TextView questionText = faqItem.findViewById(R.id.faqQuestion);
        TextView answerText = faqItem.findViewById(R.id.faqAnswer);

        questionText.setText(question);
        answerText.setText(answer);

        faqItem.setOnClickListener(v -> {
            if (answerText.getVisibility() == View.GONE) {
                animateExpand(answerText);
            } else {
                animateCollapse(answerText);
            }
        });

        faqContainer.addView(faqItem);
    }

    private void animateExpand(View view) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f);
        fadeIn.setDuration(250);
        view.startAnimation(fadeIn);
    }

    private void animateCollapse(View view) {
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f , 0.0f);
        fadeOut.setDuration(250);
        view.startAnimation(fadeOut);
        view.postDelayed(() -> view.setVisibility(View.GONE), 250);
    }
}
