package com.supermed.entities;

public class Doctor {
    private int id;
    private String name;
    private String specialization;
    private String branch;

    // Конструктор по умолчанию (важен для Gson)
    public Doctor() {}

    public Doctor(int id, String name, String specialization, String branch) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.branch = branch;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
}