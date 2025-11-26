package com.supermed.patient.model;

public class User {
    private int id;
    private String username;
    private String userType;
    private String createdAt;

    public User() {}

    public User(int id, String username, String userType, String createdAt) {
        this.id = id;
        this.username = username;
        this.userType = userType;
        this.createdAt = createdAt;
    }

    // Геттеры
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getUserType() { return userType; }
    public String getCreatedAt() { return createdAt; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setUserType(String userType) { this.userType = userType; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
