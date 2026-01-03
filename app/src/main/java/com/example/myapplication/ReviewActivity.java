package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.google.android.material.appbar.MaterialToolbar;

import com.bumptech.glide.Glide;
import com.example.myapplication.apis.ProductApi;
import com.example.myapplication.apis.RetrofitProductClient;
import com.example.myapplication.models.Product;
import com.example.myapplication.models.Client;
import com.example.myapplication.models.Review;
import com.example.myapplication.models.ReviewRequest;
import com.example.myapplication.session.SessionManager;
import com.example.myapplication.utils.ImageUtils;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReviewActivity extends AppCompatActivity {

    private static final String TAG = "ReviewActivity";

    private int productId;
    private String productName;
    private ImageView productImage;
    private ImageView reviewImagePreview;
    private ImageButton btnClear;
    private File selectedImageFile;
    private Uri currentPhotoUri;
    private File currentPhotoFile;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;
    private static final int CAMERA_PERMISSION_CODE = 102;
    private static final int STORAGE_PERMISSION_CODE = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_review);
            Log.d(TAG, "Layout set successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to set content view", e);
            Toast.makeText(this, "Error loading review screen layout", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
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

            // Initialize all views
            TextView title = findViewById(R.id.review_product_title);
            productImage = findViewById(R.id.review_product_image);
            reviewImagePreview = findViewById(R.id.review_image_preview);
            RatingBar ratingBar = findViewById(R.id.review_rating_bar);
            EditText comment = findViewById(R.id.review_comment);
            Button submit = findViewById(R.id.review_submit);
            Button btnCamera = findViewById(R.id.btn_camera);
            Button btnGallery = findViewById(R.id.btn_gallery);
            btnClear = findViewById(R.id.btn_clear_image);

            Log.d(TAG, "Views initialized");

            if (title == null || productImage == null || reviewImagePreview == null || ratingBar == null || 
                comment == null || submit == null || btnCamera == null || btnGallery == null || btnClear == null) {
                Log.e(TAG, "One or more views are null. Check layout IDs.");
                Toast.makeText(this, "Error: Missing UI elements. Check layout file.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (!TextUtils.isEmpty(productName)) {
                title.setText("Review for " + productName);
            }

            // Load full product details for image/title if needed
            loadProductDetails(title);

            // Camera button
            btnCamera.setOnClickListener(v -> {
                if (checkCameraPermission()) {
                    openCamera();
                } else {
                    requestCameraPermission();
                }
            });

            // Gallery button
            btnGallery.setOnClickListener(v -> {
                if (checkStoragePermission()) {
                    openGallery();
                } else {
                    requestStoragePermission();
                }
            });

            // Clear button
            btnClear.setOnClickListener(v -> clearImage());

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

                submit.setEnabled(false);

                if (selectedImageFile != null) {
                    submitReviewWithImage(productId, client.getId(), content, stars, selectedImageFile, submit);
                } else {
                    submitReviewWithoutImage(productId, client.getId(), content, stars, submit);
                }
            });

            Log.d(TAG, "ReviewActivity created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                currentPhotoFile = photoFile;
                currentPhotoUri = FileProvider.getUriForFile(this, 
                    getApplicationContext().getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, GALLERY_REQUEST_CODE);
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "REVIEW_" + timeStamp + ".jpg";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs();
            }
            return new File(storageDir, imageFileName);
        } catch (Exception e) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE && currentPhotoFile != null) {
                // Compress camera image before using it
                selectedImageFile = compressImage(currentPhotoFile);
                if (selectedImageFile != null) {
                    displayImagePreview(Uri.fromFile(selectedImageFile));
                } else {
                    Toast.makeText(this, "Error compressing image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    selectedImageFile = copyUriToFile(selectedImageUri);
                    displayImagePreview(selectedImageUri);
                }
            }
        }
    }

    private File copyUriToFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File outputFile = createImageFile();
            if (outputFile == null) return null;
            
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();
            return outputFile;
        } catch (Exception e) {
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void displayImagePreview(Uri imageUri) {
        reviewImagePreview.setImageURI(null); // Clear cache
        reviewImagePreview.setImageURI(imageUri);
        reviewImagePreview.setVisibility(android.view.View.VISIBLE);
        btnClear.setVisibility(android.view.View.VISIBLE);
        Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
    }

    private void clearImage() {
        selectedImageFile = null;
        currentPhotoUri = null;
        currentPhotoFile = null;
        reviewImagePreview.setImageURI(null);
        reviewImagePreview.setVisibility(android.view.View.GONE);
        btnClear.setVisibility(android.view.View.GONE);
        Toast.makeText(this, "Image cleared", Toast.LENGTH_SHORT).show();
    }

    private File compressImage(File originalFile) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(originalFile));
            
            // Calculate scaling to keep image under 1MB
            int maxWidth = 1920;
            int maxHeight = 1920;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            
            float scale = Math.min(((float)maxWidth / width), ((float)maxHeight / height));
            if (scale < 1.0f) {
                width = (int)(width * scale);
                height = (int)(height * scale);
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }
            
            // Save compressed image
            File compressedFile = createImageFile();
            if (compressedFile != null) {
                FileOutputStream fos = new FileOutputStream(compressedFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.close();
                bitmap.recycle();
                
                // Delete original uncompressed file to save space
                if (originalFile.exists() && !originalFile.equals(compressedFile)) {
                    originalFile.delete();
                }
                
                return compressedFile;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error compressing image", e);
        }
        return originalFile; // Return original if compression fails
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CAMERA_PERMISSION_CODE) {
                openCamera();
            } else if (requestCode == STORAGE_PERMISSION_CODE) {
                openGallery();
            }
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitReviewWithImage(int productId, int clientId, String content, int stars, File imageFile, Button submit) {
        ProductApi api = RetrofitProductClient.getInstance().create(ProductApi.class);
        
        // Use a generic image mime type to avoid issues if the picked file is png/webp
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

        // Send text parts as RequestBody to avoid multipart parsing issues on backend
        RequestBody productIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(productId));
        RequestBody clientIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(clientId));
        RequestBody contentBody = RequestBody.create(MediaType.parse("text/plain"), content);
        RequestBody starsBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(stars));

        api.createReviewWithImage(productIdBody, clientIdBody, contentBody, starsBody, imagePart)
                .enqueue(new Callback<Review>() {
                    @Override
                    public void onResponse(Call<Review> call, Response<Review> response) {
                        submit.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(ReviewActivity.this, "Review submitted with image", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception ignored) { }
                            Log.e(TAG, "Submit review failed: code=" + response.code() + " body=" + errorBody);
                            Toast.makeText(ReviewActivity.this, "Failed to submit review (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Review> call, Throwable t) {
                        submit.setEnabled(true);
                        Log.e(TAG, "Submit review error", t);
                        Toast.makeText(ReviewActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void submitReviewWithoutImage(int productId, int clientId, String content, int stars, Button submit) {
        ReviewRequest request = new ReviewRequest(productId, clientId, content, stars);
        ProductApi api = RetrofitProductClient.getInstance().create(ProductApi.class);

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
                    String imageUrl = ImageUtils.buildImageUrl(p.getImage());
                    if (imageUrl != null) {
                        Glide.with(ReviewActivity.this)
                                .load(imageUrl)
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
