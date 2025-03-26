package com.example.wave;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PrivacyPolicyActivity extends BaseActivity {
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_privacy_policy);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> onBackPressed());
        // For edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setNoInternetOverlay(findViewById(R.id.noInternetOverlay));
        configureNoInternetOverlay();

        // Load the formatted privacy policy
        TextView content = findViewById(R.id.privacyContent);

        String html = "<h2><font color='#007BFF'><b>WAVE Privacy Policy</b></font></h2>" +
                "<h3><font color='#007BFF'>Overview</font></h3>" +
                "<p>This document provides information on how WAVE based in Dundalk, Co.Louth, Ireland will maintain your privacy and data when using WAVE.</p>" +
                "<p>Wave is a wellbeing application designed specifically to support college students who want help in managing various aspects of their overwhelming and busy lives. Not only recognizing this, but experiencing it, is what helped us to come up with this idea. Wave aims to help in a wide range of tasks from financial budgeting advice to task management down to general wellbeing, all in the aim of creating a better college experience and minimizing the possibility of burning out.</p>" +
                "<p>The application is a user-friendly mobile application with an emphasis on student consideration and ease of use. The mobile application will focus on providing the best experience possible for the users offering all the features that WAVE will provide for them. By combining practical real-world experiences with modern tools like AI, WAVE doesn’t just aim to make students more productive but help them not sink by helping them prioritize their wellbeing and mental health, making the rough sea of stress that is the college experience nothing more than a gentle wave.</p>" +

                "<h3><font color='#007BFF'>What data do we collect?</font></h3>" +
                "<p>Our company collects the following data:</p>" +
                "<ul>" +
                "<li>Full name</li>" +
                "<li>Email address</li>" +
                "<li>Phone number</li>" +
                "<li>IP address</li>" +
                "<li>Passwords</li>" +
                "<li>User agents</li>" +
                "<li>Firebase installation IDs</li>" +
                "<li>Crashlytics Installation UUIDs</li>" +
                "<li>Crash traces</li>" +
                "<li>Break pad minidump data</li>" +
                "<li>Installation authentication tokens</li>" +
                "<li>All information provided by you for tasks, finance, food, daily moods and wellbeing</li>" +
                "</ul>" +

                "<h3><font color='#007BFF'>How do we collect the data?</font></h3>" +
                "<p>Any data that is directly provided by you to our company we collect and process. This includes when:</p>" +
                "<ul>" +
                "<li>Registering an account</li>" +
                "<li>Using the budget planner</li>" +
                "<li>Using the school tasks feature</li>" +
                "<li>Using the house tasks feature</li>" +
                "<li>Using the wellbeing section</li>" +
                "</ul>" +

                "<h3><font color='#007BFF'>What will the data be used for?</font></h3>" +
                "<p>We collect this data so that we can:</p>" +
                "<ul>" +
                "<li><b>Manage your account:</b> Your data will help us manage and maintain your account, ensuring all preferences and settings are up to date.</li>" +
                "<li><b>Notify you if an issue arises:</b> We will use your data to inform you about any issues related to your account, including data breaches or system updates.</li>" +
                "<li><b>Account recovery:</b> If you're unable to access your account, we will use your data to verify your identity and help recover access.</li>" +
                "</ul>" +
                "<p>If you agree, our team may share your data with the following third parties to provide the full WAVE service:</p>" +
                "<ul>" +
                "<li><b>OpenAI:</b> Data is shared when using AI prompts to enhance your experience.</li>" +
                "<li><b>Firebase:</b> Real-time data storage and management developed by Google.</li>" +
                "<li><b>Google Cloud Run:</b> Helps run containerized applications in secure environments.</li>" +
                "</ul>" +

                "<h3><font color='#007BFF'>How will we store your data?</font></h3>" +
                "<p>Your data is securely stored in Firebase, which includes encryption for both transit and rest, real-time data sync, and access control. Firebase also provides multi-factor authentication (MFA) like SMS confirmation to ensure only authorized access.</p>" +
                "<p>Firebase complies with industry standards and regulations, giving users a secure environment to store data.</p>" +
                "<p>Data retention policies:</p>" +
                "<ul>" +
                "<li><b>Account data:</b> Retained while you use WAVE. If inactive for 12 months, we notify you. If no login within 30 days, your account will be deleted.</li>" +
                "<li><b>Budgeting:</b> Retained only while the budget is active.</li>" +
                "<li><b>Tasks:</b> Retained for the duration of each active task.</li>" +
                "</ul>" +

                "<h3><font color='#007BFF'>What are your data protection rights?</font></h3>" +
                "<p>You have the right to:</p>" +
                "<ul>" +
                "<li><b>Be informed</b> – Know how your data is collected, used, and shared.</li>" +
                "<li><b>Access</b> – Request copies of your personal data.</li>" +
                "<li><b>Rectification</b> – Request corrections to inaccurate data.</li>" +
                "<li><b>Erasure</b> – Request that your data be deleted (\"right to be forgotten\").</li>" +
                "<li><b>Restrict processing</b> – Request limited use of your data.</li>" +
                "<li><b>Data portability</b> – Transfer your data to another organization or to yourself.</li>" +
                "<li><b>Object</b> – Object to WAVE’s processing of your data.</li>" +
                "<li><b>No automated decision-making</b> – You are not subject to decisions made solely by automated means.</li>" +
                "</ul>" +

                "<h3><font color='#007BFF'>Changes to the Privacy Policy</font></h3>" +
                "<p>We keep our privacy policy under regular review. Changes will be communicated to users. <b>Effective Date: 28/10/2024</b></p>" +

                "<h3><font color='#007BFF'>Contact</font></h3>" +
                "<p>If you have any questions about this policy, the data we hold on you, or if you want to exercise your rights or report a data breach, contact us at:</p>" +
                "<p><b>Email:</b> contactus.wave.zircon@gmail.com</p>";


        content.setText(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY));
    }
    @Override
    protected int getCurrentMenuItemId() {
        return -1; // No selection
    }
}