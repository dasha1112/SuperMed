package com.supermed;

import com.supermed.entities.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Model {

    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Doctor doctor = new Doctor(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("branch")
                );
                doctors.add(doctor);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении врачей: " + e.getMessage());
        }
        return doctors;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, d.name as doctor_name FROM appointments a " +
                "JOIN doctors d ON a.doctor_id = d.id";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Appointment appointment = new Appointment(
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getInt("doctor_id"),
                        rs.getString("appointment_time"),
                        rs.getString("secret_id"),
                        rs.getString("status"),
                        rs.getString("doctor_name")
                );
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении записей: " + e.getMessage());
        }
        return appointments;
    }

    public boolean createAppointment(Appointment appointment) {
        String sql = "INSERT INTO appointments (patient_name, doctor_id, appointment_time, secret_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, appointment.getPatientName());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setString(3, appointment.getAppointmentTime());
            pstmt.setString(4, appointment.getSecretId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при создании записи: " + e.getMessage());
            return false;
        }
    }

    public List<Statistics> getStatistics() {
        List<Statistics> stats = new ArrayList<>();
        String sql = "SELECT d.name, d.specialization, d.branch, COUNT(a.id) as appointment_count " +
                "FROM doctors d LEFT JOIN appointments a ON d.id = a.doctor_id " +
                "GROUP BY d.id, d.name, d.specialization, d.branch";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Statistics stat = new Statistics(
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("branch"),
                        rs.getInt("appointment_count")
                );
                stats.add(stat);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении статистики: " + e.getMessage());
        }
        return stats;
    }
    // Методы для работы с расписанием
    public List<Schedule> getAllSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT s.*, d.name as doctor_name FROM schedules s " +
                "JOIN doctors d ON s.doctor_id = d.id " +
                "ORDER BY d.name, s.day_of_week";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Schedule schedule = new Schedule(
                        rs.getInt("id"),
                        rs.getInt("doctor_id"),
                        rs.getString("doctor_name"),
                        rs.getString("day_of_week"),
                        rs.getString("start_time"),
                        rs.getString("end_time")
                );
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении расписания: " + e.getMessage());
        }
        return schedules;
    }

    public boolean updateSchedule(Schedule schedule) {
        // Проверяем, что рабочий день не превышает 8 часов
        if (schedule.getWorkingHours() > 8) {
            System.err.println("Ошибка: рабочий день не может превышать 8 часов");
            return false;
        }

        String sql = "UPDATE schedules SET start_time = ?, end_time = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, schedule.getStartTime());
            pstmt.setString(2, schedule.getEndTime());
            pstmt.setInt(3, schedule.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении расписания: " + e.getMessage());
            return false;
        }
    }

    public boolean addSchedule(Schedule schedule) {
        // Проверяем, что рабочий день не превышает 8 часов
        if (schedule.getWorkingHours() > 8) {
            System.err.println("Ошибка: рабочий день не может превышать 8 часов");
            return false;
        }

        String sql = "INSERT INTO schedules (doctor_id, day_of_week, start_time, end_time) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, schedule.getDoctorId());
            pstmt.setString(2, schedule.getDayOfWeek());
            pstmt.setString(3, schedule.getStartTime());
            pstmt.setString(4, schedule.getEndTime());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении расписания: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSchedule(int scheduleId) {
        String sql = "DELETE FROM schedules WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, scheduleId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении расписания: " + e.getMessage());
            return false;
        }
    }
}