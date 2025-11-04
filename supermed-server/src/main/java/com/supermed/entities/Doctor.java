package com.supermed.entities;

// Класс врачей
public class Doctor {
    private int id;
    private String name;
    private String specialization;
    private int branchId;
    private String branchName;
    private String branchAddress;

    public Doctor() {}

    public Doctor(int id, String name, String specialization, int branchId) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.branchId = branchId;
    }

    public Doctor(int id, String name, String specialization, int branchId, String branchName, String branchAddress) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.branchId = branchId;
        this.branchName = branchName;
        this.branchAddress = branchAddress;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public int getBranchId() { return branchId; } // ИЗМЕНЕНО
    public void setBranchId(int branchId) { this.branchId = branchId; } // ИЗМЕНЕНО
    public String getBranchName() { return branchName; } // ДОБАВЛЕНО
    public void setBranchName(String branchName) { this.branchName = branchName; } // ДОБАВЛЕНО
    public String getBranchAddress() { return branchAddress; } // ДОБАВЛЕНО
    public void setBranchAddress(String branchAddress) { this.branchAddress = branchAddress; } // ДОБАВЛЕНО
}