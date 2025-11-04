package com.supermed.entities;

// Класс пользователя (Врач, менеджер или пациент)
public class User {
    private int id;
    private String username;
    private String password;
    private String userType; // MANAGER, DOCTOR, PATIENT
    private String createdAt;

    public User() {}

    public User(int id, String username, String userType, String createdAt) {
        this.id = id;
        this.username = username;
        this.userType = userType;
        this.createdAt = createdAt;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}