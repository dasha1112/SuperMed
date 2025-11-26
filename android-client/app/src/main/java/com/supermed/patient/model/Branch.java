package com.supermed.patient.model;

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

    // Геттеры
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    // Сеттеры
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
