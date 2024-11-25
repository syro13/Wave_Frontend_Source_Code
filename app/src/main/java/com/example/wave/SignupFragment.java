package com.example.wave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SignupFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        // Bind toggle buttons
        TextView loginButton = view.findViewById(R.id.loginButton);
        TextView signupButton = view.findViewById(R.id.signupButton);

        // Set active button style for Login
        setActiveButton(loginButton, signupButton);

        // Handle Login Button Click
        loginButton.setOnClickListener(v -> {
            if (getActivity() instanceof LoginSignUpActivity) {
                ((LoginSignUpActivity) getActivity()).showLoginFragment();
            }
        });

        // Handle Sign Up Button Click (no action since already on SignupFragment)
        signupButton.setOnClickListener(v -> {
            // Do nothing, already on SignupFragment
        });

        return view;
    }
    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        // Active button style
        activeButton.setBackgroundResource(R.drawable.toggle_button_background);
        activeButton.setTextColor(getResources().getColor(android.R.color.white));

        // Inactive button style
        inactiveButton.setBackgroundResource(R.drawable.toggle_button_background);
        inactiveButton.setTextColor(getResources().getColor(R.color.blue));
    }
}
