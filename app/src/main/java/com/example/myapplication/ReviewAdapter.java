package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        // Set client name
        String clientName = review.getClient().getFirstName() + " " + review.getClient().getLastName();
        holder.clientName.setText(clientName);

        // Set review content
        holder.reviewContent.setText(review.getContent());

        // Set rating
        holder.ratingBar.setRating(review.getStars());
        holder.ratingText.setText(review.getStars() + "/5");
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView clientName, reviewContent, ratingText;
        RatingBar ratingBar;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            clientName = itemView.findViewById(R.id.review_client_name);
            reviewContent = itemView.findViewById(R.id.review_content);
            ratingBar = itemView.findViewById(R.id.review_rating_bar);
            ratingText = itemView.findViewById(R.id.review_rating_text);
        }
    }
}
