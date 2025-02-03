package com.example.wave;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private List<OnboardingSlide> onboardingSlides;

    public OnboardingAdapter(List<OnboardingSlide> onboardingSlides) {
        this.onboardingSlides = onboardingSlides;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.onboarding_slide, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.bind(onboardingSlides.get(position));
    }

    @Override
    public int getItemCount() {
        return onboardingSlides.size();
    }

    // Making the OnboardingViewHolder class public for better visibility
    public static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView slideImage;
        final TextView slideTitle;
        final TextView slideDescription;

        public OnboardingViewHolder(View itemView) {
            super(itemView);
            slideImage = itemView.findViewById(R.id.slideImage);
            slideTitle = itemView.findViewById(R.id.slideTitle);
            slideDescription = itemView.findViewById(R.id.slideDescription);
        }

        void bind(OnboardingSlide slide) {
            slideImage.setImageResource(slide.getImageRes());
            slideTitle.setText(slide.getTitle());
            slideDescription.setText(slide.getDescription());
        }
    }
}
