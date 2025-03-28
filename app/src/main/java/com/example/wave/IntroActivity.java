package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
// Inject dynamic Lottie animation for theme
        LottieAnimationView lottieView = findViewById(R.id.lottieWaves);
        int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            lottieView.setAnimation(R.raw.intro_animation_dark); // dark theme animation
        } else {
            lottieView.setAnimation(R.raw.intro_wave_animation); // default light theme animation
        }
        // Bind "Get Started" button
        Button getStartedButton = findViewById(R.id.getStartedButton);

        // Navigate to OnboardingActivity on button click
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroActivity.this, OnboardingActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); // Smooth transition
                finish(); // Close IntroActivity
            }
        });
    }
}
