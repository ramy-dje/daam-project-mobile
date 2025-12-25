package com.example.myapplication.models;

import java.io.Serializable;

public class ReviewRequest implements Serializable {
    private int productId;
    private int clientId;
    private String content;
    private int stars;

    public ReviewRequest(int productId, int clientId, String content, int stars) {
        this.productId = productId;
        this.clientId = clientId;
        this.content = content;
        this.stars = stars;
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
}
