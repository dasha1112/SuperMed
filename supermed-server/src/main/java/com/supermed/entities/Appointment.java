package com.supermed.entities;

// Класс записей в мед учреждение
public class Appointment {
    private int id;
    private String patientUsername;
    private int doctorId;
    private String appointmentDate;
    private String startTime;
    private String endTime;
    private String secretId;
    private String status;
    private String doctorName;

    public Appointment() {}

    public Appointment(int id, String patientUsername, int doctorId, String appointmentDate,
                       String startTime, String endTime, String secretId, String status, String doctorName) {
        this.id = id;
        this.patientUsername = patientUsername;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.secretId = secretId;
        this.status = status;
        this.doctorName = doctorName;
    }

    // Добавить геттеры и сеттеры для новых полей
    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getAppointmentTime() {
        return appointmentDate + " " + startTime;
    }

    public String getPatientUsername() {
        return patientUsername;
    }

    public void setPatientUsername(String patientUsername) {
        this.patientUsername = patientUsername;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getStatus() {
        return status;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}