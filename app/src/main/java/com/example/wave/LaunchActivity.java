package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Looper;

public class LaunchActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);


        mediaPlayer = MediaPlayer.create(this, R.raw.wave_sound);
        mediaPlayer.start();


        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimation);


        lottieAnimationView.setRepeatCount(ValueAnimator.INFINITE);
        lottieAnimationView.playAnimation();


        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            fadeOutSoundAndTransition();
        }, 3000); // 3-second delay
    }

    private void fadeOutSoundAndTransition() {
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

                    // Transition to MainActivity
                    Intent intent = new Intent(LaunchActivity.this, IntroActivity.class);
                    startActivity(intent);
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
