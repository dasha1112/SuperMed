package com.supermed.patient.model;

public class Statistic {
    private int id;
    private String patientUsername;
    private int doctorId;
    private String appointmentDate;
    private String startTime;
    private String endTime;
    private String secretId;
    private String status;
    private String doctorName;
    private String doctorSpecialization;
    private String branchName;
    private String branchAddress;
    private String doctorScheduleDayOfWeek;
    private String doctorScheduleStartTime;
    private String doctorScheduleEndTime;

    public Statistic() {}

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
    public String getDoctorSpecialization() { return doctorSpecialization; }
    public String getBranchName() { return branchName; }
    public String getBranchAddress() { return branchAddress; }
    public String getDoctorScheduleDayOfWeek() { return doctorScheduleDayOfWeek; }
    public String getDoctorScheduleStartTime() { return doctorScheduleStartTime; }
    public String getDoctorScheduleEndTime() { return doctorScheduleEndTime; }

   }