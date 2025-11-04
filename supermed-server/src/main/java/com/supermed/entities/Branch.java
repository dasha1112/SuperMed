package com.supermed.entities;

// Класс филиалов
public class Branch {
    private int id;
    private String name;
    private String address;

    public Branch() {}

    public Branch(int id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}