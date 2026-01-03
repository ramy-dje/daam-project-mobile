package com.example.myapplication.models;

public class Review {

    private int id;
    private Client client;
    private Product product;
    private String content;
    private int stars;
    private String image;

    public Review() {
    }

    public Review(int id, Client client, Product product, String content, int stars) {
        this.id = id;
        this.client = client;
        this.product = product;
        this.content = content;
        this.stars = stars;
    }

    public Review(int id, Client client, Product product, String content, int stars, String image) {
        this.id = id;
        this.client = client;
        this.product = product;
        this.content = content;
        this.stars = stars;
        this.image = image;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
