package com.supermed.patient.model;

public class Schedule {
    private int id;
    private int doctorId;
    private String doctorName;      // ← добавлено сервером через JOIN
    private String dayOfWeek;       // например: "Понедельник", "Вторник"
    private String startTime;       // например: "09:00"
    private String endTime;         // например: "17:00"
    private String branchName;      // ← добавлено сервером через JOIN

    public Schedule() {}

    public Schedule(int id, int doctorId, String doctorName, String dayOfWeek,
                    String startTime, String endTime) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Геттеры
    public int getId() { return id; }
    public int getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getBranchName() { return branchName; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
}
