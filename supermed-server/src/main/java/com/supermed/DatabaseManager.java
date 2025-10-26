package com.supermed;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:supermed.db";

    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                Statement stmt = conn.createStatement();

                // Таблица врачей (обновляем - добавляем филиал)
                String sqlDoctors = "CREATE TABLE IF NOT EXISTS doctors (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "specialization TEXT NOT NULL, " +
                        "branch TEXT NOT NULL);";

                // Таблица записей
                String sqlAppointments = "CREATE TABLE IF NOT EXISTS appointments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "patient_name TEXT NOT NULL, " +
                        "doctor_id INTEGER NOT NULL, " +
                        "appointment_time TEXT NOT NULL, " +
                        "secret_id TEXT NOT NULL, " +
                        "status TEXT NOT NULL DEFAULT 'scheduled', " +
                        "FOREIGN KEY(doctor_id) REFERENCES doctors(id));";

                // Новая таблица расписания
                String sqlSchedules = "CREATE TABLE IF NOT EXISTS schedules (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "doctor_id INTEGER NOT NULL, " +
                        "day_of_week TEXT NOT NULL, " + // MONDAY, TUESDAY, etc.
                        "start_time TEXT NOT NULL, " + // HH:MM
                        "end_time TEXT NOT NULL, " +   // HH:MM
                        "FOREIGN KEY(doctor_id) REFERENCES doctors(id));";

                stmt.execute(sqlDoctors);
                stmt.execute(sqlAppointments);
                stmt.execute(sqlSchedules);

                // Тестовые данные
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM doctors");
                if (rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO doctors (name, specialization, branch) VALUES " +
                            "('Сергей Александрович Иванов', 'Кардиолог', 'Центральный филиал'), " +
                            "('Мария Михайловна Петрова', 'Невролог', 'Северный филиал'), " +
                            "('Алексей Васильевич Сидоров', 'Терапевт', 'Южный филиал')");

                    // Тестовое расписание
                    stmt.execute("INSERT INTO schedules (doctor_id, day_of_week, start_time, end_time) VALUES " +
                            "(1, 'MONDAY', '09:00', '17:00'), " +
                            "(1, 'WEDNESDAY', '09:00', '17:00'), " +
                            "(2, 'TUESDAY', '10:00', '18:00'), " +
                            "(2, 'THURSDAY', '10:00', '18:00'), " +
                            "(3, 'FRIDAY', '08:00', '16:00')");
                }

                System.out.println("База данных инициализирована успешно.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}