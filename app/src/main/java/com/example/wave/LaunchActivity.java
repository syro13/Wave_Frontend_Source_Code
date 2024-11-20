package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import android.animation.ValueAnimator;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // Initialize the Lottie Animation View
        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimation);

        // Start the animation
        lottieAnimationView.setRepeatCount(ValueAnimator.INFINITE);
        lottieAnimationView.playAnimation();

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 5000); // 5-second delay
    }
}
