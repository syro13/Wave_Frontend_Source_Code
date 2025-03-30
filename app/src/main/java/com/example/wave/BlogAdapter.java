package com.example.wave;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    private final Context context;
    private final List<Blogs_Response> blogs;

    public BlogAdapter(Context context, List<Blogs_Response> blogs) {
        this.context = context;
        this.blogs = blogs;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_item_card, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        Blogs_Response blog = blogs.get(position);

        // Bind blog data to the UI
        holder.title.setText(blog.getTitle());
        holder.tag.setText(blog.getTag() != null ? blog.getTag() : "No Tag"); // Fallback if tag is null
        holder.image.setVisibility(View.INVISIBLE);
        Glide.with(context)
                .load(blog.getImage())
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.image.setVisibility(View.VISIBLE); // Fail-safe to show something
                        return false; // Let Glide handle the error placeholder if any
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        holder.image.setVisibility(View.VISIBLE); // Show only when fully loaded
                        return false;
                    }
                })
                .into(holder.image);
    }


    @Override
    public int getItemCount() {
        return blogs.size();
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        TextView title, tag;
        ImageView image;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.blogTitle);
            tag = itemView.findViewById(R.id.blogTag);
            image = itemView.findViewById(R.id.blogImage);
        }
    }
    public void updateData(List<Blogs_Response> newBlogs) {
        blogs.clear();
        blogs.addAll(newBlogs);
        notifyDataSetChanged();
    }
}
