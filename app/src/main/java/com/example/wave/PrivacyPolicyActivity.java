package com.example.wave;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PrivacyPolicyActivity extends BaseActivity {
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue_variant));

        setContentView(R.layout.activity_privacy_policy);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> onBackPressed());
        setNoInternetOverlay(findViewById(R.id.noInternetOverlay));
        configureNoInternetOverlay();

        // Load the formatted privacy policy
        TextView content = findViewById(R.id.privacyContent);
        int headingColor = getResources().getColor(R.color.privacy_heading_light, getTheme());
        int textColor = getResources().getColor(R.color.privacy_text_light, getTheme());
        String headingHex = String.format("#%06X", (0xFFFFFF & headingColor));
        String textHex = String.format("#%06X", (0xFFFFFF & textColor));

        String html = "<h2><font color='" + headingHex + "'><b>WAVE Privacy Policy</b></font></h2>" +
                "<h3><font color='" + headingHex + "'>Overview</font></h3>" +
                "<p><font color='" + textHex + "'>This document provides information on how WAVE based in Dundalk, Co.Louth, Ireland will maintain your privacy and data when using WAVE.</font></p>" +
                "<p><font color='" + textHex + "'>Wave is a wellbeing application designed specifically to support college students who want help in managing various aspects of their overwhelming and busy lives. Not only recognizing this, but experiencing it, is what helped us to come up with this idea. Wave aims to help in a wide range of tasks from financial budgeting advice to task management down to general wellbeing, all in the aim of creating a better college experience and minimizing the possibility of burning out.</font></p>" +
                "<p><font color='" + textHex + "'>The application is a user-friendly mobile application with an emphasis on student consideration and ease of use. The mobile application will focus on providing the best experience possible for the users offering all the features that WAVE will provide for them. By combining practical real-world experiences with modern tools like AI, WAVE doesn’t just aim to make students more productive but help them not sink by helping them prioritize their wellbeing and mental health, making the rough sea of stress that is the college experience nothing more than a gentle wave.</font></p>" +

                "<h3><font color='" + headingHex + "'>Overview</font></h3>" +
                "<p><font color='" + textHex + "'>Our company collects the following data:</font></p>" +
                "<ul>" +
                "<li><font color='" + textHex + "'>Full name</font></li>" +
                "<li><font color='" + textHex + "'>Email address</font></li>" +
                "<li><font color='" + textHex + "'>Phone number</font></li>" +
                "<li><font color='" + textHex + "'>IP address</font></li>" +
                "<li><font color='" + textHex + "'>Passwords</font></li>" +
                "<li><font color='" + textHex + "'>User agents</font></li>" +
                "<li><font color='" + textHex + "'>Firebase installation IDs</font></li>" +
                "<li><font color='" + textHex + "'>Crashlytics Installation UUIDs</font></li>" +
                "<li><font color='" + textHex + "'>Crash traces</font></li>" +
                "<li><font color='" + textHex + "'>Break pad minidump data</font></li>" +
                "<li><font color='" + textHex + "'>Installation authentication tokens</font></li>" +
                "<li><font color='" + textHex + "'>All information provided by you for tasks, finance, food, daily moods and wellbeing</font></li>" +
                "</ul>" +

                "<h3><font color='" + headingHex + "'>How do we collect the data?</font></h3>" +
                "<p><font color='" + textHex + "'>Any data that is directly provided by you to our company we collect and process. This includes when:</font></p>" +
                "<ul>" +
                "<li><font color='" + textHex + "'>Registering an account</font></li>" +
                "<li><font color='" + textHex + "'>Using the budget planner</font></li>" +
                "<li><font color='" + textHex + "'>Using the school tasks feature</font></li>" +
                "<li><font color='" + textHex + "'>Using the house tasks feature</font></li>" +
                "<li><font color='" + textHex + "'>Using the wellbeing section</font></li>" +
                "</ul>" +

                "<h3><font color='" + headingHex + "'>What will the data be used for?</font></h3>" +
                "<p><font color='" + textHex + "'>We collect this data so that we can:</font></p>" +
                "<ul>" +
                "<li><font color='" + textHex + "'><b>Manage your account:</b> Your data will help us manage and maintain your account, ensuring all preferences and settings are up to date.</font></li>" +
                "<li><font color='" + textHex + "'><b>Notify you if an issue arises:</b> We will use your data to inform you about any issues related to your account, including data breaches or system updates.</font></li>" +
                "<li><font color='" + textHex + "'><b>Account recovery:</b> If you're unable to access your account, we will use your data to verify your identity and help recover access.</font></li>" +
                "</ul>" +

                "<p><font color='" + textHex + "'>If you agree, our team may share your data with the following third parties to provide the full WAVE service:</font></p>" +
                "<ul>" +
                "<li><font color='" + textHex + "'><b>OpenAI:</b> Data is shared when using AI prompts to enhance your experience.</font></li>" +
                "<li><font color='" + textHex + "'><b>Firebase:</b> Real-time data storage and management developed by Google.</font></li>" +
                "<li><font color='" + textHex + "'><b>Google Cloud Run:</b> Helps run containerized applications in secure environments.</font></li>" +
                "</ul>" +

                "<h3><font color='" + headingHex + "'>How will we store your data?</font></h3>" +
                "<p><font color='" + textHex + "'>Your data is securely stored in Firebase, which includes encryption for both transit and rest, real-time data sync, and access control. Firebase also provides multi-factor authentication (MFA) like SMS confirmation to ensure only authorized access.</font></p>" +
                "<p><font color='" + textHex + "'>Firebase complies with industry standards and regulations, giving users a secure environment to store data.</font></p>" +
                "<p><font color='" + textHex + "'>Data retention policies:</font></p>" +
                "<ul>" +
                "<li><font color='" + textHex + "'><b>Account data:</b> Retained while you use WAVE. If inactive for 12 months, we notify you. If no login within 30 days, your account will be deleted.</font></li>" +
                "<li><font color='" + textHex + "'><b>Budgeting:</b> Retained only while the budget is active.</font></li>" +
                "<li><font color='" + textHex + "'><b>Tasks:</b> Retained for the duration of each active task.</font></li>" +
                "</ul>" +

                "<h3><font color='" + headingHex + "'>What are your data protection rights?</font></h3>" +
                "<p><font color='" + textHex + "'>You have the right to:</font></p>" +
                "<ul>" +
                "<li><font color='" + textHex + "'><b>Be informed</b> – Know how your data is collected, used, and shared.</font></li>" +
                "<li><font color='" + textHex + "'><b>Access</b> – Request copies of your personal data.</font></li>" +
                "<li><font color='" + textHex + "'><b>Rectification</b> – Request corrections to inaccurate data.</font></li>" +
                "<li><font color='" + textHex + "'><b>Erasure</b> – Request that your data be deleted (\"right to be forgotten\").</font></li>" +
                "<li><font color='" + textHex + "'><b>Restrict processing</b> – Request limited use of your data.</font></li>" +
                "<li><font color='" + textHex + "'><b>Data portability</b> – Transfer your data to another organization or to yourself.</font></li>" +
                "<li><font color='" + textHex + "'><b>Object</b> – Object to WAVE’s processing of your data.</font></li>" +
                "<li><font color='" + textHex + "'><b>No automated decision-making</b> – You are not subject to decisions made solely by automated means.</font></li>" +
                "</ul>" +

                "<h3><font color='" + headingHex + "'>Changes to the Privacy Policy</font></h3>" +
                "<p><font color='" + textHex + "'>We keep our privacy policy under regular review. Changes will be communicated to users. <b>Effective Date: 28/10/2024</b></font></p>" +

                "<h3><font color='" + headingHex + "'>Contact</font></h3>" +
                "<p><font color='" + textHex + "'>If you have any questions about this policy, the data we hold on you, or if you want to exercise your rights or report a data breach, contact us at:</font></p>" +
                "<p><font color='" + textHex + "'><b>Email:</b> contactus.wave.zircon@gmail.com</font></p>";

        content.setText(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY));
    }
    @Override
    protected int getCurrentMenuItemId() {
        return -1; // No selection
    }
}