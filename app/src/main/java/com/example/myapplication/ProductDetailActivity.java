package com.example.myapplication;

import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.apis.ProductApi;
import com.example.myapplication.apis.RetrofitProductClient;
import com.example.myapplication.models.Product;
import com.example.myapplication.models.Review;
import com.example.myapplication.models.Seller;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView productImage;
    private TextView productName, productDesc, productPrice, productLocation;
    private TextView sellerName, sellerEmail, sellerPhone;
    private RecyclerView reviewsRecycler;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Status bar styling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(
                        getWindow().getDecorView().getSystemUiVisibility() | android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            }
        }

        // Initialize views
        productImage = findViewById(R.id.detail_product_image);
        productName = findViewById(R.id.detail_product_name);
        productDesc = findViewById(R.id.detail_product_description);
        productPrice = findViewById(R.id.detail_product_price);
        productLocation = findViewById(R.id.detail_product_location);
        sellerName = findViewById(R.id.detail_seller_name);
        sellerEmail = findViewById(R.id.detail_seller_email);
        sellerPhone = findViewById(R.id.detail_seller_phone);
        reviewsRecycler = findViewById(R.id.reviews_recycler);

        // Back button
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Setup reviews recycler
        reviewsRecycler.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviewList);
        reviewsRecycler.setAdapter(reviewAdapter);

        // Get product ID from intent
        int productId = getIntent().getIntExtra("product_id", -1);
        if (productId != -1) {
            loadProductDetails(productId);
            loadProductReviews(productId);
        } else {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadProductDetails(int productId) {
        // Fetch complete product details from API
        ProductApi api = RetrofitProductClient.getInstance().create(ProductApi.class);
        
        api.getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body();
                    
                    // Display product info
                    productName.setText(product.getName());
                    productDesc.setText(product.getDescription());
                    productPrice.setText(String.format("$%.2f", product.getPrice()));
                    productLocation.setText(product.getLocationName() != null ? product.getLocationName() : "Unknown Location");

                    // Load product image
                    if (product.getImage() != null && !product.getImage().isEmpty()) {
                        Glide.with(ProductDetailActivity.this)
                                .load(product.getImage())
                                .placeholder(R.drawable.logo)
                                .error(R.drawable.logo)
                                .into(productImage);
                    } else {
                        productImage.setImageResource(R.drawable.logo);
                    }

                    // Display seller info from nested Seller object
                    Seller seller = product.getSeller();
                    if (seller != null) {
                        String sellerName = (seller.getFirstName() != null ? seller.getFirstName() : "") + " " + 
                                          (seller.getLastName() != null ? seller.getLastName() : "");
                        ProductDetailActivity.this.sellerName.setText(sellerName.trim());
                        ProductDetailActivity.this.sellerEmail.setText(seller.getEmail() != null ? seller.getEmail() : "N/A");
                        ProductDetailActivity.this.sellerPhone.setText(seller.getPhoneNumber() != null ? seller.getPhoneNumber() : "N/A");
                    } else {
                        // Fallback to legacy seller fields if nested object is null
                        if (product.getSellerId() > 0) {
                            String sellerName = (product.getSellerFirstName() != null ? product.getSellerFirstName() : "") + " " + 
                                              (product.getSellerLastName() != null ? product.getSellerLastName() : "");
                            ProductDetailActivity.this.sellerName.setText(sellerName.trim());
                            ProductDetailActivity.this.sellerEmail.setText(product.getSellerEmail() != null ? product.getSellerEmail() : "N/A");
                            ProductDetailActivity.this.sellerPhone.setText(product.getSellerPhoneNumber() != null ? product.getSellerPhoneNumber() : "N/A");
                        }
                    }
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Failed to load product details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(ProductDetailActivity.this, "Error loading product: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductReviews(int productId) {
        ProductApi api = RetrofitProductClient.getInstance().create(ProductApi.class);

        api.getProductReviews(productId).enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reviewList.clear();
                    reviewList.addAll(response.body());
                    reviewAdapter.notifyDataSetChanged();
                    Toast.makeText(ProductDetailActivity.this, "Reviews loaded", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Failed to load reviews", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(ProductDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
