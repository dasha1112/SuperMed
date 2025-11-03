package com.supermed;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:supermed.db";

    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                Statement stmt = conn.createStatement();

                // Таблица пользователей
                String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT UNIQUE NOT NULL, " +
                        "password TEXT NOT NULL, " +
                        "user_type TEXT NOT NULL, " +
                        "created_at TEXT NOT NULL);";

                // ТАБЛИЦА ФИЛИАЛОВ (НОВАЯ)
                String sqlBranches = "CREATE TABLE IF NOT EXISTS branches (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "address TEXT NOT NULL);";

                // Таблица врачей (ОБНОВЛЯЕМ: branch -> branch_id)
                String sqlDoctors = "CREATE TABLE IF NOT EXISTS doctors (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "specialization TEXT NOT NULL, " +
                        "branch_id INTEGER NOT NULL, " +
                        "FOREIGN KEY(branch_id) REFERENCES branches(id));";

                // Таблица записей
                String sqlAppointments = "CREATE TABLE IF NOT EXISTS appointments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "patient_username TEXT NOT NULL, " +
                        "doctor_id INTEGER NOT NULL, " +
                        "appointment_time TEXT NOT NULL, " +
                        "secret_id TEXT NOT NULL, " +
                        "status TEXT NOT NULL DEFAULT 'scheduled', " +
                        "FOREIGN KEY(doctor_id) REFERENCES doctors(id));";

                // Таблица расписания
                String sqlSchedules = "CREATE TABLE IF NOT EXISTS schedules (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "doctor_id INTEGER NOT NULL, " +
                        "day_of_week TEXT NOT NULL, " +
                        "start_time TEXT NOT NULL, " +
                        "end_time TEXT NOT NULL, " +
                        "FOREIGN KEY(doctor_id) REFERENCES doctors(id));";

                stmt.execute(sqlUsers);
                stmt.execute(sqlBranches); // НОВАЯ ТАБЛИЦА
                stmt.execute(sqlDoctors);
                stmt.execute(sqlAppointments);
                stmt.execute(sqlSchedules);

                // Тестовые данные
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
                if (rs.getInt(1) == 0) {
                    // Создаем тестового менеджера
                    String managerPassword = hashPassword("manager123");
                    stmt.execute("INSERT INTO users (username, password, user_type, created_at) VALUES " +
                            "('m.shemelova', '" + managerPassword + "', 'MANAGER', datetime('now'))");

                    // Создаем тестового врача
                    String doctorPassword = hashPassword("doctor123");
                    stmt.execute("INSERT INTO users (username, password, user_type, created_at) VALUES " +
                            "('d.ivanov', '" + doctorPassword + "', 'DOCTOR', datetime('now'))");

                    // Создаем тестовых пациентов
                    String patientPassword = hashPassword("patient123");
                    stmt.execute("INSERT INTO users (username, password, user_type, created_at) VALUES " +
                            "('p.kotova', '" + patientPassword + "', 'PATIENT', datetime('now')), " +
                            "('m.maskov', '" + patientPassword + "', 'PATIENT', datetime('now')), " +
                            "('a.smirnova', '" + patientPassword + "', 'PATIENT', datetime('now')), " +
                            "('v.petrov', '" + patientPassword + "', 'PATIENT', datetime('now'))");

                    // ТЕСТОВЫЕ ФИЛИАЛЫ
                    stmt.execute("INSERT INTO branches (name, address) VALUES " +
                            "('Центральный филиал', 'г. Нижний Новогород, ул. Пушкина, д. 25'), " +
                            "('Северный филиал', 'г. Нижний Новогород, ул. Горького, д. 120'), " +
                            "('Южный филиал', 'г. Нижний Новогород, ул. Ленина, д. 85')");

                    // Тестовые врачи (теперь с branch_id)
                    stmt.execute("INSERT INTO doctors (name, specialization, branch_id) VALUES " +
                            "('Иванов Иван Алексеевич', 'Кардиолог', 1), " + // Центральный
                            "('Петрова Елена Васильевна', 'Невролог', 2), " + // Северный
                            "('Сидоров Александр Викторович', 'Терапевт', 3), " + // Южный
                            "('Кузнецова Ольга Сергеевна', 'Педиатр', 1), " + // Центральный
                            "('Николаев Дмитрий Викторович', 'Хирург', 2)"); // Северный

                    // Тестовое расписание
                    stmt.execute("INSERT INTO schedules (doctor_id, day_of_week, start_time, end_time) VALUES " +
                            "(1, 'Понедельник', '09:00', '17:00'), " +
                            "(1, 'Среда', '09:00', '17:00'), " +
                            "(2, 'Вторник', '10:00', '18:00'), " +
                            "(2, 'Четверг', '10:00', '18:00'), " +
                            "(3, 'Пятница', '08:00', '16:00'), " +
                            "(4, 'Понедельник', '08:00', '16:00'), " +
                            "(4, 'Вторник', '08:00', '16:00'), " +
                            "(5, 'Среда', '09:00', '17:00')");

                    // Тестовые записи к врачам
                    stmt.execute("INSERT INTO appointments (patient_username, doctor_id, appointment_time, secret_id, status) VALUES " +
                            "('p.kotova', 1, '2024-01-15 10:00:00', 'SEC001', 'completed'), " +
                            "('m.maskov', 1, '2024-01-15 11:30:00', 'SEC002', 'completed'), " +
                            "('a.smirnova', 1, '2024-01-17 14:00:00', 'SEC003', 'scheduled'), " +
                            "('v.petrov', 1, '2024-01-17 15:30:00', 'SEC004', 'scheduled'), " +
                            "('p.kotova', 2, '2024-01-16 10:00:00', 'SEC005', 'completed'), " +
                            "('m.maskov', 2, '2024-01-16 12:00:00', 'SEC006', 'cancelled'), " +
                            "('a.smirnova', 2, '2024-01-18 14:30:00', 'SEC007', 'scheduled'), " +
                            "('v.petrov', 2, '2024-01-18 16:00:00', 'SEC008', 'scheduled'), " +
                            "('p.kotova', 3, '2024-01-19 09:00:00', 'SEC009', 'scheduled'), " +
                            "('m.maskov', 3, '2024-01-19 10:30:00', 'SEC010', 'scheduled'), " +
                            "('a.smirnova', 3, '2024-01-19 12:00:00', 'SEC011', 'scheduled'), " +
                            "('v.petrov', 3, '2024-01-19 13:30:00', 'SEC012', 'scheduled'), " +
                            "('p.kotova', 4, '2024-01-22 09:30:00', 'SEC013', 'scheduled'), " +
                            "('m.maskov', 5, '2024-01-23 11:00:00', 'SEC014', 'scheduled'), " +
                            "('a.smirnova', 4, '2024-01-26 14:00:00', 'SEC015', 'scheduled')");
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

    // Метод для хеширования пароля
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка хеширования пароля", e);
        }
    }
}