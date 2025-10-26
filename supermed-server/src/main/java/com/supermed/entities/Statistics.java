package com.supermed.entities;

public class Statistics {
    private String doctorName;
    private String specialization;
    private String branch;
    private int appointmentCount;

    public Statistics() {}

    public Statistics(String doctorName, String specialization, String branch, int appointmentCount) {
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.branch = branch;
        this.appointmentCount = appointmentCount;
    }

    // Геттеры и сеттеры
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public int getAppointmentCount() { return appointmentCount; }
    public void setAppointmentCount(int appointmentCount) { this.appointmentCount = appointmentCount; }
}