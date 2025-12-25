package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

import com.bumptech.glide.Glide;
import com.example.myapplication.apis.ProductApi;
import com.example.myapplication.apis.RetrofitProductClient;
import com.example.myapplication.models.Product;
import com.example.myapplication.models.Client;
import com.example.myapplication.models.Review;
import com.example.myapplication.models.ReviewRequest;
import com.example.myapplication.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {

    private int productId;
    private String productName;
    private ImageView productImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        // Toolbar setup with back action
        MaterialToolbar toolbar = findViewById(R.id.review_toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        productId = getIntent().getIntExtra("product_id", -1);
        productName = getIntent().getStringExtra("product_name");

        if (productId == -1) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SessionManager sessionManager = new SessionManager(this);
        Client client = sessionManager.getClient();
        if (client == null) {
            Toast.makeText(this, "Please login to submit a review", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView title = findViewById(R.id.review_product_title);
        productImage = findViewById(R.id.review_product_image);
        RatingBar ratingBar = findViewById(R.id.review_rating_bar);
        EditText comment = findViewById(R.id.review_comment);
        Button submit = findViewById(R.id.review_submit);

        if (!TextUtils.isEmpty(productName)) {
            title.setText("Review for " + productName);
        }

        // Load full product details for image/title if needed
        loadProductDetails(title);

        submit.setOnClickListener(v -> {
            int stars = Math.round(ratingBar.getRating());
            String content = comment.getText().toString().trim();

            if (stars < 1) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
                return;
            }

            ReviewRequest request = new ReviewRequest(productId, client.getId(), content, stars);
            ProductApi api = RetrofitProductClient.getInstance().create(ProductApi.class);
            submit.setEnabled(false);

            api.createReview(request).enqueue(new Callback<Review>() {
                @Override
                public void onResponse(Call<Review> call, Response<Review> response) {
                    submit.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(ReviewActivity.this, "Review submitted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ReviewActivity.this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Review> call, Throwable t) {
                    submit.setEnabled(true);
                    Toast.makeText(ReviewActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadProductDetails(TextView title) {
        ProductApi api = RetrofitProductClient.getInstance().create(ProductApi.class);
        api.getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product p = response.body();
                    if (!TextUtils.isEmpty(p.getName())) {
                        title.setText(p.getName());
                    }
                    if (p.getImage() != null && !p.getImage().isEmpty()) {
                        Glide.with(ReviewActivity.this)
                                .load(p.getImage())
                                .placeholder(R.drawable.logo)
                                .error(R.drawable.logo)
                                .into(productImage);
                    } else {
                        productImage.setImageResource(R.drawable.logo);
                    }
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                // Fallback: keep default image/title
            }
        });
    }
}
