package com.example.myapplication.apis;

import com.example.myapplication.models.Product;
import com.example.myapplication.models.Review;
import com.example.myapplication.models.ReviewRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
import retrofit2.http.Path;
import retrofit2.http.POST;

public interface ProductApi {

    @GET("products")
    Call<List<Product>> getAllProducts();

    @GET("products/{id}")
    Call<Product> getProductById(@Path("id") int productId);

    @GET("reviews/products/{id}")
    Call<List<Review>> getProductReviews(@Path("id") int productId);

    @POST("reviews")
    Call<Review> createReview(@Body ReviewRequest request);
}
