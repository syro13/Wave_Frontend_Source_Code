package com.example.wave;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.ImageView;

public class AboutUsActivity extends BaseActivity {

    private ImageView backButton;
    private ImageView facebookIcon, instagramIcon, twitterIcon, linkedinIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us); // Make sure your XML is named this

        setNoInternetOverlay(findViewById(R.id.noInternetOverlay));
        configureNoInternetOverlay();

        // Back Button
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Social Media Links (optional - open browser or activity)
        facebookIcon = findViewById(R.id.facebookIcon);
        instagramIcon = findViewById(R.id.instagramIcon);
        twitterIcon = findViewById(R.id.twitterIcon);
        linkedinIcon = findViewById(R.id.linkedinIcon);

        facebookIcon.setOnClickListener(v -> openLink("https://facebook.com"));
        instagramIcon.setOnClickListener(v -> openLink("https://instagram.com"));
        twitterIcon.setOnClickListener(v -> openLink("https://twitter.com"));
        linkedinIcon.setOnClickListener(v -> openLink("https://linkedin.com"));
    }
    @Override
    protected int getCurrentMenuItemId() {
        return -1; // No selection
    }
    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(android.net.Uri.parse(url));
        startActivity(intent);
    }
}

