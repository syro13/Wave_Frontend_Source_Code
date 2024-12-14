package com.example.wave;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class WellnessActivity extends AppCompatActivity {

    private ImageView quoteImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellness);

        // Reference to the ImageView
        quoteImage = findViewById(R.id.quoteImage);

        // Update the image
        updateQuoteImage();
    }

    private void updateQuoteImage() {
        // Define an array of image resources
        int[] images = {
                R.drawable.quote1,
                R.drawable.quote2,
                R.drawable.quote3,
                R.drawable.quote4,
                R.drawable.quote5,
                R.drawable.quote6

        };

        // Get the day of the year to pick an image
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        int index = dayOfYear % images.length; // Rotate images daily

        // Set the image
        quoteImage.setImageResource(images[index]);
    }
}

