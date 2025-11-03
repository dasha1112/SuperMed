package com.supermed.entities;

// Класс расписание врача
public class Schedule {
    private int id;
    private int doctorId;
    private String doctorName;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private int workingHours;
    private String branchName;

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
            // Разделяем время на часы и минуты
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");

            int startHours = Integer.parseInt(startParts[0]);
            int startMinutes = Integer.parseInt(startParts[1]);
            int endHours = Integer.parseInt(endParts[0]);
            int endMinutes = Integer.parseInt(endParts[1]);

            // Переводим все в минуты
            int startTotalMinutes = startHours * 60 + startMinutes;
            int endTotalMinutes = endHours * 60 + endMinutes;

            // Вычисляем разницу в минутах
            int differenceMinutes = endTotalMinutes - startTotalMinutes;

            // Переводим в часы (дробное число)
            double hours = differenceMinutes / 60.0;

            // Округляем до целых часов для отображения
            this.workingHours = (int) Math.round(hours);

        } catch (Exception e) {
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
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
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