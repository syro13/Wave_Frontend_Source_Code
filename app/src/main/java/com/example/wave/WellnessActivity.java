package com.example.wave;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

        setupPodcastsRecyclerView();
        setupBlogsRecyclerView();
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

    private void setupPodcastsRecyclerView() {
        RecyclerView podcastRecyclerView = findViewById(R.id.podcastRecyclerView);
        podcastRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Podcast> podcasts = new ArrayList<>();
        podcasts.add(new Podcast("Stress day relaxation", "15 min"));
        podcasts.add(new Podcast("Evening meditation to relax", "12 min"));

        PodcastAdapter podcastAdapter = new PodcastAdapter(podcasts);
        podcastRecyclerView.setAdapter(podcastAdapter);
    }
    private void setupBlogsRecyclerView() {
        RecyclerView blogRecyclerView = findViewById(R.id.blogRecyclerView);
        blogRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Blog> blogs = new ArrayList<>();
        blogs.add(new Blog("Feeling Overwhelmed? Try the RAIN Meditation", "By Tara Brach"));
        blogs.add(new Blog("Struggling with burnout? 10 recovery tips", "By Dr. Chris"));

        BlogAdapter blogAdapter = new BlogAdapter(blogs);
        blogRecyclerView.setAdapter(blogAdapter);
    }

}

