package com.supermed;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//  Класс для работы с базой данных
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

                // Таблица филиалов
                String sqlBranches = "CREATE TABLE IF NOT EXISTS branches (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "address TEXT NOT NULL);";

                // Таблица врачей
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
                        "appointment_date TEXT NOT NULL, " +
                        "start_time TEXT NOT NULL, " +
                        "end_time TEXT NOT NULL, " +
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

                // ТАБЛИЦА СООБЩЕНИЙ - ДОБАВЛЯЕМ
                String sqlMessages = "CREATE TABLE IF NOT EXISTS messages (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "sender_username TEXT NOT NULL, " +
                        "receiver_username TEXT NOT NULL, " +
                        "message_text TEXT NOT NULL, " +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(sender_username) REFERENCES users(username), " +
                        "FOREIGN KEY(receiver_username) REFERENCES users(username));";

                String sqlDoctorUsers = "CREATE TABLE IF NOT EXISTS doctor_users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "doctor_id INTEGER NOT NULL, " +
                        "user_id INTEGER NOT NULL, " +
                        "FOREIGN KEY(doctor_id) REFERENCES doctors(id), " +
                        "FOREIGN KEY(user_id) REFERENCES users(id), " +
                        "UNIQUE(doctor_id, user_id));";

                stmt.execute(sqlDoctorUsers);
                stmt.execute(sqlUsers);
                stmt.execute(sqlBranches);
                stmt.execute(sqlDoctors);
                stmt.execute(sqlAppointments);
                stmt.execute(sqlSchedules);
                stmt.execute(sqlMessages); // ДОБАВЛЯЕМ ВЫПОЛНЕНИЕ СОЗДАНИЯ ТАБЛИЦЫ

                // Тестовые данные
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
                if (rs.getInt(1) == 0) {
                    // Создаем тестового менеджера (для входа в систему)
                    String managerPassword = hashPassword("manager123");
                    stmt.execute("INSERT INTO users (username, password, user_type, created_at) VALUES " +
                            "('m.shemelova', '" + managerPassword + "', 'MANAGER', datetime('now'))");

                    // Создаем тестового врача (для входа в систему)
                    String doctorPassword = hashPassword("doctor123");
                    stmt.execute("INSERT INTO users (username, password, user_type, created_at) VALUES " +
                            "('d.ivanov', '" + doctorPassword + "', 'DOCTOR', datetime('now'))");
                    //stmt.execute("INSERT INTO doctors (name, specialization, branch_id) VALUES " +
                    //        "('d.ivanov', 'Кардиолог', 1)");

                    // Создаем тестовых пациентов (для входа в систему)
                    String patientPassword = hashPassword("patient123");
                    stmt.execute("INSERT INTO users (username, password, user_type, created_at) VALUES " +
                            "('p.kotova', '" + patientPassword + "', 'PATIENT', datetime('now')), " +
                            "('m.maskov', '" + patientPassword + "', 'PATIENT', datetime('now')), " +
                            "('a.smirnova', '" + patientPassword + "', 'PATIENT', datetime('now')), " +
                            "('v.petrov', '" + patientPassword + "', 'PATIENT', datetime('now'))");

                    // Тестовые филиалы
                    stmt.execute("INSERT INTO branches (name, address) VALUES " +
                            "('Центральный филиал', 'г. Нижний Новогород, ул. Пушкина, д. 25'), " +
                            "('Северный филиал', 'г. Нижний Новогород, ул. Горького, д. 120'), " +
                            "('Южный филиал', 'г. Нижний Новогород, ул. Ленина, д. 85')");

                    // Тестовые врачи
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

                    // ОБНОВЛЕННЫЕ ТЕСТОВЫЕ ЗАПИСИ К ВРАЧАМ - ДОБАВЛЯЕМ СВЕЖИЕ ДАТЫ
                    stmt.execute("INSERT INTO appointments (patient_username, doctor_id, appointment_date, start_time, end_time, secret_id, status) VALUES " +
                            "('p.kotova', 1, '2025-01-20', '10:00', '10:30', 'SEC001', 'scheduled'), " +
                            "('m.maskov', 1, '2025-01-20', '11:00', '11:30', 'SEC002', 'scheduled'), " +
                            "('a.smirnova', 1, '2025-01-20', '12:00', '12:30', 'SEC003', 'scheduled'), " +
                            "('p.kotova', 1, '2025-12-15', '10:00', '10:30', 'SEC001', 'scheduled'), " +
                            "('m.maskov', 1, '2025-12-8', '11:00', '11:30', 'SEC002', 'scheduled'), " +
                            "('a.smirnova', 1, '2025-12-10', '12:00', '12:30', 'SEC003', 'scheduled'), " +
                            "('p.kotova', 1, '2025-12-17', '10:00', '10:30', 'SEC001', 'scheduled'), " +
                            "('m.maskov', 1, '2025-12-22', '11:00', '11:30', 'SEC002', 'scheduled'), " +
                            "('a.smirnova', 1, '2025-12-10', '12:00', '12:30', 'SEC003', 'scheduled'), " +
                            "('v.petrov', 1, '2025-01-24', '09:00', '09:30', 'SEC004', 'scheduled'), " +
                            "('p.kotova', 2, '2025-01-21', '14:00', '14:30', 'SEC005', 'scheduled'), " +
                            "('m.maskov', 2, '2025-01-22', '15:00', '15:30', 'SEC006', 'scheduled'), " +
                            "('a.smirnova', 2, '2025-01-22', '16:00', '16:30', 'SEC007', 'scheduled'), " +
                            "('v.petrov', 3, '2025-01-23', '10:00', '10:30', 'SEC008', 'scheduled'), " +
                            "('p.kotova', 3, '2025-01-23', '11:00', '11:30', 'SEC009', 'scheduled'), " +
                            "('m.maskov', 4, '2025-01-24', '14:00', '14:30', 'SEC010', 'scheduled'), " +
                            "('a.smirnova', 4, '2025-01-24', '15:00', '15:30', 'SEC011', 'scheduled'), " +
                            "('v.petrov', 5, '2025-01-25', '09:00', '09:30', 'SEC012', 'scheduled')");

                    // ТЕСТОВЫЕ СООБЩЕНИЯ - ДОБАВЛЯЕМ
                    stmt.execute("INSERT INTO messages (sender_username, receiver_username, message_text, timestamp) VALUES " +
                            "('p.kotova', 'd.ivanov', 'Здравствуйте, доктор! У меня вопрос по рецепту, который вы выписали в прошлый раз.', '2025-01-15 10:15:00'), " +
                            "('d.ivanov', 'p.kotova', 'Здравствуйте, Алексей. Какой именно у вас вопрос?', '2025-01-15 10:20:00'), " +
                            "('p.kotova', 'd.ivanov', 'Можно ли заменить препарат на аналог? В аптеке не было того, что вы прописали.', '2025-01-15 10:22:00'), " +
                            "('m.maskov', 'd.ivanov', 'Спасибо за консультацию!', '2025-01-14 16:30:00'), " +
                            "('a.smirnova', 'd.ivanov', 'Когда можно записаться на прием?', '2025-01-16 09:45:00'), " +
                            "('d.ivanov', 'a.smirnova', 'На следующей неделе есть свободные слоты во вторник и четверг.', '2025-01-16 10:00:00'), " +
                            "('v.petrov', 'd.ivanov', 'Добрый день! Нужна консультация по результатам анализов.', '2025-01-17 14:20:00'), " +
                            "('d.ivanov', 'v.petrov', 'Здравствуйте! Присылайте результаты, посмотрю.', '2025-01-17 14:30:00')");
                }
                String linkDoctorUser = "INSERT OR IGNORE INTO doctor_users (doctor_id, user_id) VALUES (" +
                        "1, (SELECT id FROM users WHERE username = 'd.ivanov'))";
                stmt.execute(linkDoctorUser);

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