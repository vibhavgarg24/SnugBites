package com.example.snugbites.model;

public class User {

    private String userId;
    private String username;
    private String fullName;
    private String dpUrl;
    private String bio;
    private boolean seller;
    private String deviceToken;

    public User() {
    }

    public User(String userId, String username, String fullName, String dpUrl, String bio, boolean seller, String deviceToken) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.dpUrl = dpUrl;
        this.bio = bio;
        this.seller = seller;
        this.deviceToken = deviceToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public boolean getSeller() {
        return seller;
    }

    public void setSeller(boolean seller) {
        this.seller = seller;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
