package com.supermed.entities;

public class DetailedAppointment {
    private int id;
    private String patientUsername;
    private int doctorId;
    private String doctorSpecialization;
    private String appointmentDate;
    private String startTime; // Время начала приема
    private String endTime;   // Время окончания приема
    private String secretId;
    private String status;
    private String doctorName;
    private String branchName;
    private String branchAddress;
    // Поля из Schedule, относящиеся к дню приема
    private String doctorScheduleDayOfWeek; // День недели расписания
    private String doctorScheduleStartTime; // Начало рабочего дня врача
    private String doctorScheduleEndTime;   // Конец рабочего дня врача
    public DetailedAppointment() {}
    public DetailedAppointment(int id, String patientUsername, int doctorId, String appointmentDate,
                               String startTime, String endTime, String secretId, String status,
                               String doctorName, String doctorSpecialization, String branchName, String branchAddress, // Обновленный конструктор
                               String doctorScheduleDayOfWeek, String doctorScheduleStartTime, String doctorScheduleEndTime) {
        this.id = id;
        this.patientUsername = patientUsername;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.secretId = secretId;
        this.status = status;
        this.doctorName = doctorName;
        this.doctorSpecialization = doctorSpecialization; // Инициализация нового поля
        this.branchName = branchName;
        this.branchAddress = branchAddress;
        this.doctorScheduleDayOfWeek = doctorScheduleDayOfWeek;
        this.doctorScheduleStartTime = doctorScheduleStartTime;
        this.doctorScheduleEndTime = doctorScheduleEndTime;
    }

    public String getDoctorSpecialization() { return doctorSpecialization; } // НОВЫЙ ГЕТТЕР
    public void setDoctorSpecialization(String doctorSpecialization) { this.doctorSpecialization = doctorSpecialization; } // НОВЫЙ СЕТТЕР

    // --- Геттеры и Сеттеры для всех полей ---
    // (Я опущу их здесь для краткости, но они должны быть для всех полей,
    // включая новые doctorScheduleDayOfWeek, doctorScheduleStartTime, doctorScheduleEndTime)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPatientUsername() { return patientUsername; }
    public void setPatientUsername(String patientUsername) { this.patientUsername = patientUsername; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getSecretId() { return secretId; }
    public void setSecretId(String secretId) { this.secretId = secretId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getBranchAddress() { return branchAddress; }
    public void setBranchAddress(String branchAddress) { this.branchAddress = branchAddress; }
    public String getDoctorScheduleDayOfWeek() { return doctorScheduleDayOfWeek; }
    public void setDoctorScheduleDayOfWeek(String doctorScheduleDayOfWeek) { this.doctorScheduleDayOfWeek = doctorScheduleDayOfWeek; }
    public String getDoctorScheduleStartTime() { return doctorScheduleStartTime; }
    public void setDoctorScheduleStartTime(String doctorScheduleStartTime) { this.doctorScheduleStartTime = doctorScheduleStartTime; }
    public String getDoctorScheduleEndTime() { return doctorScheduleEndTime; }
    public void setDoctorScheduleEndTime(String doctorScheduleEndTime) { this.doctorScheduleEndTime = doctorScheduleEndTime; }
    // Метод для вывода расписания врача в удобном формате
    public String getFormattedDoctorSchedule() {
        if (doctorScheduleDayOfWeek != null && doctorScheduleStartTime != null && doctorScheduleEndTime != null) {
            return doctorScheduleDayOfWeek + " " + doctorScheduleStartTime + "-" + doctorScheduleEndTime;
        }
        return "Расписание не указано";
    }
    // Метод для получения времени приема (как раньше)
    public String getAppointmentTimeRange() {
        return getStartTime() + "-" + getEndTime();
    }
}
