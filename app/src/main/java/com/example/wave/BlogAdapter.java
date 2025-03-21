package com.example.wave;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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

        Glide.with(context)
                .load(blog.getImage())
                .placeholder(R.drawable.blog_placeholder)
                .into(holder.image);

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
