package com.supermed.entities;

public class Appointment {
    private int id;
    private String patientName;
    private int doctorId;
    private String appointmentTime;
    private String secretId;
    private String status;
    private String doctorName;

    public Appointment() {}

    public Appointment(int id, String patientName, int doctorId, String appointmentTime,
                       String secretId, String status, String doctorName) {
        this.id = id;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.appointmentTime = appointmentTime;
        this.secretId = secretId;
        this.status = status;
        this.doctorName = doctorName;
    }

    // Геттеры и сеттеры для всех полей
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
    public String getSecretId() { return secretId; }
    public void setSecretId(String secretId) { this.secretId = secretId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
}