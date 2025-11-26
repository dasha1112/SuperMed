package com.supermed.patient.model;

public class Appointment {
    private int id;
    private String patientUsername;
    private int doctorId;
    private String appointmentDate; // формат: "YYYY-MM-DD"
    private String startTime;       // формат: "HH:mm"
    private String endTime;         // формат: "HH:mm"
    private String secretId;
    private String status;          // например: "scheduled", "completed"
    private String doctorName;      // ← добавлено сервером через JOIN

    public Appointment() {}

    public Appointment(int id, String patientUsername, int doctorId,
                       String appointmentDate, String startTime, String endTime,
                       String secretId, String status, String doctorName) {
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

    // Геттеры
    public int getId() { return id; }
    public String getPatientUsername() { return patientUsername; }
    public int getDoctorId() { return doctorId; }
    public String getAppointmentDate() { return appointmentDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getSecretId() { return secretId; }
    public String getStatus() { return status; }
    public String getDoctorName() { return doctorName; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setPatientUsername(String patientUsername) { this.patientUsername = patientUsername; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setSecretId(String secretId) { this.secretId = secretId; }
    public void setStatus(String status) { this.status = status; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    // Удобный метод для отображения даты + времени
    public String getDisplayTime() {
        return appointmentDate + " " + startTime;
    }
}
