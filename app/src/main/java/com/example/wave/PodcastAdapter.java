package com.example.wave;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.PodcastViewHolder> {

    private List<Podcast> podcasts;

    public PodcastAdapter(List<Podcast> podcasts) {
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
        Podcast podcast = podcasts.get(position);
        holder.title.setText(podcast.getTitle());
        holder.duration.setText(podcast.getDuration());
        holder.playButton.setOnClickListener(v -> {
            // Handle play button click
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
}
