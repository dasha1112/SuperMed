package com.supermed.patient.model;

public class Doctor {
    private int id;
    private String name;
    private String specialization;
    private int branchId;
    private String branchName;      // ← добавлено на сервере при JOIN
    private String branchAddress;   // ← добавлено на сервере при JOIN

    public Doctor() {}

    // Основной конструктор (как в серверной модели)
    public Doctor(int id, String name, String specialization, int branchId, String branchName, String branchAddress) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.branchId = branchId;
        this.branchName = branchName;
        this.branchAddress = branchAddress;
    }

    // Геттеры
    public int getId() { return id; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public int getBranchId() { return branchId; }
    public String getBranchName() { return branchName; }
    public String getBranchAddress() { return branchAddress; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setBranchId(int branchId) { this.branchId = branchId; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public void setBranchAddress(String branchAddress) { this.branchAddress = branchAddress; }
}
