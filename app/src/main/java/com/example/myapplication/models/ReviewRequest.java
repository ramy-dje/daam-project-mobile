package com.example.myapplication.models;

import java.io.File;
import java.io.Serializable;

public class ReviewRequest implements Serializable {
    private int productId;
    private int clientId;
    private String content;
    private int stars;
    private transient File imageFile; // Not serialized, used only for multipart upload

    public ReviewRequest(int productId, int clientId, String content, int stars) {
        this.productId = productId;
        this.clientId = clientId;
        this.content = content;
        this.stars = stars;
    }

    public ReviewRequest(int productId, int clientId, String content, int stars, File imageFile) {
        this.productId = productId;
        this.clientId = clientId;
        this.content = content;
        this.stars = stars;
        this.imageFile = imageFile;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }
}
