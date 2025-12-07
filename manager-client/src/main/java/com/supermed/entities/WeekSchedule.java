package com.supermed.entities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Класс для представления недельного расписания
public class WeekSchedule {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private List<ScheduleSlot> scheduleSlots;
    private String doctorUsername;

    public WeekSchedule() {}

    public WeekSchedule(LocalDate weekStart, LocalDate weekEnd,
                        List<ScheduleSlot> scheduleSlots, String doctorUsername) {
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.scheduleSlots = scheduleSlots;
        this.doctorUsername = doctorUsername;
    }

    // Геттеры и сеттеры
    public LocalDate getWeekStart() { return weekStart; }
    public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }

    public LocalDate getWeekEnd() { return weekEnd; }
    public void setWeekEnd(LocalDate weekEnd) { this.weekEnd = weekEnd; }

    public List<ScheduleSlot> getScheduleSlots() { return scheduleSlots; }
    public void setScheduleSlots(List<ScheduleSlot> scheduleSlots) { this.scheduleSlots = scheduleSlots; }

    public String getDoctorUsername() { return doctorUsername; }
    public void setDoctorUsername(String doctorUsername) { this.doctorUsername = doctorUsername; }

    // Метод для получения форматированной строки недели
    public String getWeekRangeFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return String.format("%s - %s",
                weekStart.format(formatter),
                weekEnd.format(formatter));
    }

    // Метод для получения названия недели
    public String getWeekTitle() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        return String.format("Неделя: %s - %s %d",
                weekStart.format(formatter),
                weekEnd.format(formatter),
                weekStart.getYear());
    }
}