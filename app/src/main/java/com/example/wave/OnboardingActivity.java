package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import android.view.View;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button nextButton;
    private TextView skipButton;
    private OnboardingAdapter onboardingAdapter;
    private Handler slideHandler = new Handler();

    // Indicator Views
    private View indicator1, indicator2, indicator3;

    private Button backButton; // Declare backButton at the top

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        nextButton = findViewById(R.id.nextButton);
        skipButton = findViewById(R.id.skipButton);
        backButton = findViewById(R.id.backButton); // Initialize backButton
        indicator1 = findViewById(R.id.dot1);
        indicator2 = findViewById(R.id.dot2);
        indicator3 = findViewById(R.id.dot3);

        setupOnboardingSlides();
        setupListeners();
        setupAutoSlide();
        updateIndicators(0); // Set initial indicator

        // Handle the back button functionality for backButton
        backButton.setOnClickListener(v -> {
            // Navigate to IntroActivity
            Intent intent = new Intent(OnboardingActivity.this, IntroActivity.class);
            startActivity(intent);
            finish(); // Close OnboardingActivity
        });
    }


    private void setupListeners() {
        // Action for Next button
        nextButton.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < onboardingAdapter.getItemCount() - 1) {
                viewPager.setCurrentItem(currentItem + 1, true); // Move to the next slide
            } else {
                finishOnboarding(); // Navigate to main activity on the last slide
            }
        });

        // Action for Skip button
        skipButton.setOnClickListener(v -> {
            finishOnboarding(); // Immediately skip to main activity
        });
    }

    private void setupAutoSlide() {
        // Start auto-slide
        slideHandler.postDelayed(slideRunnable, 8000);

        // Reset auto-slide timing on manual swipe
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position); // Update indicators
                slideHandler.removeCallbacks(slideRunnable);
                slideHandler.postDelayed(slideRunnable, 6000); // Reset auto-slide
            }
        });
    }

    private Runnable slideRunnable = new Runnable() {
        @Override
        public void run() {
            int nextSlide = (viewPager.getCurrentItem() + 1) % onboardingAdapter.getItemCount();
            viewPager.setCurrentItem(nextSlide, true);
            slideHandler.postDelayed(this, 8000);
        }
    };

    private void updateIndicators(int position) {
        // Reset all indicators to light blue
        indicator1.setBackgroundTintList(getResources().getColorStateList(R.color.light_blue, null));
        indicator2.setBackgroundTintList(getResources().getColorStateList(R.color.light_blue, null));
        indicator3.setBackgroundTintList(getResources().getColorStateList(R.color.light_blue, null));

        // Set the active indicator to dark blue
        switch (position) {
            case 0:
                indicator1.setBackgroundTintList(getResources().getColorStateList(R.color.dark_blue, null));
                break;
            case 1:
                indicator2.setBackgroundTintList(getResources().getColorStateList(R.color.dark_blue, null));
                break;
            case 2:
                indicator3.setBackgroundTintList(getResources().getColorStateList(R.color.dark_blue, null));
                break;
        }

        // Update Next button text on the last slide
        if (position == onboardingAdapter.getItemCount() - 1) {
            nextButton.setText("Get me started!");
        } else {
            nextButton.setText("Next");
        }
    }

    private void finishOnboarding() {
        // Navigate to LoginSignUpActivity
        Intent intent = new Intent(OnboardingActivity.this, LoginSignUpActivity.class);
        startActivity(intent);
        finish(); // Close OnboardingActivity
    }

    private void setupOnboardingSlides() {
        List<OnboardingSlide> slides = new ArrayList<>();
        slides.add(new OnboardingSlide(R.drawable.image1, "Manage your tasks", "Easily organize and keep track of all your school and household tasks with Wave."));
        slides.add(new OnboardingSlide(R.drawable.image2, "Track Your Budget", "Set weekly budgets and monitor your expenses effortlessly with our budget tracker."));
        slides.add(new OnboardingSlide(R.drawable.image3, "Focus on Well-being", "Access tools and resources for a balanced, stress-free life while you study."));

        onboardingAdapter = new OnboardingAdapter(slides);
        viewPager.setAdapter(onboardingAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto-slide when activity is not visible
        slideHandler.removeCallbacks(slideRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume auto-slide when activity becomes visible again
        slideHandler.postDelayed(slideRunnable, 6000);
    }

}
