package com.example.myapplication.utils;

public final class ImageUtils {
    // Point this to the HTTP path that serves product images from your backend.
    // If your backend exposes the uploads folder under /uploads/products, this will work as-is.
    private static final String IMAGE_BASE_URL = "http://192.168.1.9:8080/uploads/products/";

    private ImageUtils() { }

    public static String buildImageUrl(String imagePath) {
        if (imagePath == null) {
            return null;
        }
        String trimmed = imagePath.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        return IMAGE_BASE_URL + trimmed;
    }
}
