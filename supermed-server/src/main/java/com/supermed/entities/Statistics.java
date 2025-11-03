package com.supermed.entities;

public class Statistics {
    private String doctorName;
    private String specialization;
    private String branchName;
    private String branchAddress;
    private int appointmentCount;

    public Statistics() {}

    public Statistics(String doctorName, String specialization, String branchName, String branchAddress, int appointmentCount) {
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.branchName = branchName;
        this.branchAddress = branchAddress;
        this.appointmentCount = appointmentCount;
    }

    // Геттеры и сеттеры
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getBranchName() { return branchName; } // ИЗМЕНЕНО
    public void setBranchName(String branchName) { this.branchName = branchName; } // ИЗМЕНЕНО
    public String getBranchAddress() { return branchAddress; } // ДОБАВЛЕНО
    public void setBranchAddress(String branchAddress) { this.branchAddress = branchAddress; } // ДОБАВЛЕНО
    public int getAppointmentCount() { return appointmentCount; }
    public void setAppointmentCount(int appointmentCount) { this.appointmentCount = appointmentCount; }
}