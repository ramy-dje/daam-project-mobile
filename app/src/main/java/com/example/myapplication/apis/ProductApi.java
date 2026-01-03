package com.example.myapplication.apis;

import com.example.myapplication.models.Product;
import com.example.myapplication.models.Review;
import com.example.myapplication.models.ReviewRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

public interface ProductApi {

    @GET("products")
    Call<List<Product>> getAllProducts();

    @GET("products/{id}")
    Call<Product> getProductById(@Path("id") int productId);

    @GET("reviews/products/{id}")
    Call<List<Review>> getProductReviews(@Path("id") int productId);

    @POST("reviews")
    Call<Review> createReview(@Body ReviewRequest request);

    @Multipart
    @POST("reviews/with-image")
    Call<Review> createReviewWithImage(
            @Part("productId") RequestBody productId,
            @Part("clientId") RequestBody clientId,
            @Part("content") RequestBody content,
            @Part("stars") RequestBody stars,
            @Part MultipartBody.Part image
    );
}
