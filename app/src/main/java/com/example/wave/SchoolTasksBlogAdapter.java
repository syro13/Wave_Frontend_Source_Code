package com.example.wave;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;

public class SchoolTasksBlogAdapter extends RecyclerView.Adapter<SchoolTasksBlogAdapter.SchoolBlogViewHolder> {

    private final Context context;
    private final List<BlogResponse> blogs;
    private final HashMap<Integer, Float> userRatings = new HashMap<>(); // Store user ratings by position

    public SchoolTasksBlogAdapter(Context context, List<BlogResponse> blogs) {
        this.context = context;
        this.blogs = blogs;
    }

    @NonNull
    @Override
    public SchoolBlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);
        return new SchoolBlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SchoolBlogViewHolder holder, int position) {
        BlogResponse blog = blogs.get(position);

        // Bind blog data to the UI
        holder.title.setText(blog.getTitle());
        Glide.with(context)
                .load(blog.getImageUrl())
                .placeholder(R.drawable.blog_placeholder) // Placeholder image
                .into(holder.image);

        // Restore the user's rating (if previously set)
        if (userRatings.containsKey(position)) {
            holder.ratingBar.setRating(userRatings.get(position));
        } else {
            holder.ratingBar.setRating(0); // Default rating
        }

        // Handle rating bar change
        holder.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                userRatings.put(position, rating); // Save the user's rating
            }
        });

        // Bookmark action (optional)
        holder.bookmarkIcon.setOnClickListener(v -> {
            // Handle bookmark action (optional)
        });

        // Set click listener to open the blog URL
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(blog.getLink()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return blogs.size();
    }

    public static class SchoolBlogViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image, bookmarkIcon;
        RatingBar ratingBar;

        public SchoolBlogViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.articleTitle);
            image = itemView.findViewById(R.id.articleImage);
            bookmarkIcon = itemView.findViewById(R.id.bookmarkIcon);
            ratingBar = itemView.findViewById(R.id.articleRatingBar);
        }
    }
}
