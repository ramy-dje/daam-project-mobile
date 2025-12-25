package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myapplication.models.Product;

import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.VH> {

    public interface OnProductClickListener { 
        void onProductClick(Product product); 
        void onReviewClick(Product product);
    }

    private final List<Product> items;
    private final OnProductClickListener listener;

    public ProductsAdapter(List<Product> items, OnProductClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateProducts(List<Product> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = items.get(position);
        
        // Set name
        holder.title.setText(p.getName() != null ? p.getName() : "Unknown Product");
        
        // Set location
        holder.subtitle.setText(p.getLocationName() != null ? p.getLocationName() : "Unknown Location");
        
        // Set description
        holder.description.setText(p.getDescription() != null ? p.getDescription() : "No description available");
        
        // Set price
        holder.price.setText(String.format("$%.2f", p.getPrice()));
        
        // Load image with Glide - fallback to logo if URL is invalid/empty
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            Glide.with(holder.image.getContext())
                    .load(p.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.logo);
        }
        
        // Show review button only if canReview is true
        if (p.isCanReview()) {
            holder.reviewBtn.setVisibility(View.VISIBLE);
            holder.reviewBtn.setOnClickListener(v -> listener.onReviewClick(p));
        } else {
            holder.reviewBtn.setVisibility(View.GONE);
        }
        
        // Card click listener
        holder.card.setOnClickListener(v -> listener.onProductClick(p));
    }

    @Override
    public int getItemCount() { 
        return items.size(); 
    }

    static class VH extends RecyclerView.ViewHolder {
        CardView card;
        ImageView image;
        TextView title, subtitle, description, price;
        Button reviewBtn;
        
        VH(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            image = itemView.findViewById(R.id.product_image);
            title = itemView.findViewById(R.id.product_title);
            subtitle = itemView.findViewById(R.id.product_subtitle);
            description = itemView.findViewById(R.id.product_description);
            price = itemView.findViewById(R.id.product_price);
            reviewBtn = itemView.findViewById(R.id.btn_review);
        }
    }
}

