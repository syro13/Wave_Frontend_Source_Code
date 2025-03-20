package com.example.wave;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.PodcastViewHolder> {

    private final Context context;
    private final List<PodcastsResponse> podcasts;

    public PodcastAdapter(Context context, List<PodcastsResponse> podcasts) {
        this.context = context;
        this.podcasts = podcasts;
    }

    @NonNull
    @Override
    public PodcastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.podcast_item_card, parent, false);
        return new PodcastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PodcastViewHolder holder, int position) {
        PodcastsResponse podcast = podcasts.get(position);

        // Set the podcast title
        holder.title.setText(podcast.getTitle());

        // Set the duration in minutes
        holder.duration.setText(podcast.getDuration());

        // Handle the play button click
        holder.playButton.setOnClickListener(v -> {
            String audioUrl = podcast.getLink();
            if (audioUrl != null && !audioUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(audioUrl));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

    static class PodcastViewHolder extends RecyclerView.ViewHolder {
        TextView title, duration;
        ImageButton playButton;

        public PodcastViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.podcastTitle);
            duration = itemView.findViewById(R.id.podcastDuration);
            playButton = itemView.findViewById(R.id.podcastPlayCircle);
        }
    }
    public void updateData(List<PodcastsResponse> newPodcasts) {
        podcasts.clear();
        podcasts.addAll(newPodcasts);
        notifyDataSetChanged(); // This forces RecyclerView to refresh
    }

}
