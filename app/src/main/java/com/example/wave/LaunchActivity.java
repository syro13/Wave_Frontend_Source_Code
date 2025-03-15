package com.example.wave;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Looper;
import android.util.Log;

public class LaunchActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    private static final int LAUNCH_DELAY = 3000; // 3 seconds delay before transition

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);


        mediaPlayer = MediaPlayer.create(this, R.raw.wave_sound);
        mediaPlayer.start();


        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimation);


        lottieAnimationView.setRepeatCount(ValueAnimator.INFINITE);
        lottieAnimationView.playAnimation();


        // Delay before checking session and transitioning
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAppFlow, LAUNCH_DELAY);
    }
    /**
     * Checks if the user should go to onboarding, login/signup, or dashboard.
     */
    private void checkAppFlow() {
        SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean onboardingCompleted = preferences.getBoolean("onboarding_completed", false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        Intent intent;

        if (!onboardingCompleted) {
            // First-time user → Go to Get Started Screen
            intent = new Intent(this, IntroActivity.class);
        } else if (currentUser != null) {
            // Refresh token before navigating to Dashboard
            refreshAuthToken(currentUser);
            // User is already signed in → Go to Dashboard
            intent = new Intent(this, DashboardActivity.class);
        } else {
            // Onboarding done, but user is not signed in → Go to Login/Signup
            intent = new Intent(this, LoginSignUpActivity.class);
        }

        fadeOutSoundAndTransition(intent);
    }

    /**
     * Refresh Firebase Auth Token to ensure a valid session.
     */
    private void refreshAuthToken(FirebaseUser user) {
        user.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String newToken = task.getResult().getToken();
                Log.d("SESSION", "New Token: " + newToken);
            } else {
                Log.e("SESSION", "Failed to refresh token", task.getException());
            }
        });
    }
    private void fadeOutSoundAndTransition(Intent nextIntent) {
        final int FADE_DURATION = 1000; // 1 second fade duration
        final int FADE_INTERVAL = 100; // Update every 100ms
        final float MAX_VOLUME = 1.0f; // Full volume
        final float MIN_VOLUME = 0.0f; // No volume

        final Handler handler = new Handler(Looper.getMainLooper());
        final int steps = FADE_DURATION / FADE_INTERVAL;
        final float deltaVolume = MAX_VOLUME / steps;

        handler.post(new Runnable() {
            float currentVolume = MAX_VOLUME;

            @Override
            public void run() {
                if (currentVolume > MIN_VOLUME) {
                    currentVolume -= deltaVolume;
                    mediaPlayer.setVolume(currentVolume, currentVolume);
                    handler.postDelayed(this, FADE_INTERVAL);
                } else {

                    mediaPlayer.stop();
                    mediaPlayer.release();

                    // Transition to the next activity
                    startActivity(nextIntent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
