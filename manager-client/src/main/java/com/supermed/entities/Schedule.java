package com.supermed.entities;

public class Schedule {
    private int id;
    private int doctorId;
    private String doctorName;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private int workingHours;

    public Schedule() {}

    public Schedule(int id, int doctorId, String dayOfWeek, String startTime, String endTime) {
        this.id = id;
        this.doctorId = doctorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        calculateWorkingHours();
    }

    public Schedule(int id, int doctorId, String doctorName, String dayOfWeek, String startTime, String endTime) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        calculateWorkingHours();
    }

    private void calculateWorkingHours() {
        try {
            int start = Integer.parseInt(startTime.replace(":", ""));
            int end = Integer.parseInt(endTime.replace(":", ""));
            this.workingHours = (end - start) / 100;
        } catch (NumberFormatException e) {
            this.workingHours = 0;
        }
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
        calculateWorkingHours();
    }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
        calculateWorkingHours();
    }
    public int getWorkingHours() { return workingHours; }
}