package com.example.snugbites.model;

public class Seller {

    private String sellerId;
    private String username;
    private String fullName;
    private String dpUrl;
    private String bio;

    public Seller() {
    }

    public Seller(String sellerId, String username, String fullName, String dpUrl, String bio) {
        this.sellerId = sellerId;
        this.username = username;
        this.fullName = fullName;
        this.dpUrl = dpUrl;
        this.bio = bio;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDpUrl() {
        return dpUrl;
    }

    public void setDpUrl(String dpUrl) {
        this.dpUrl = dpUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
