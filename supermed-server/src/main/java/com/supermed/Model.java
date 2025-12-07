package com.supermed;

import com.supermed.entities.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Класс, описывающий большую часть функционала
public class Model {

    // Метод для входа в систему (логирование)
    public AuthResponse loginUser(String username, String password, String userType) {
        String hashedPassword = DatabaseManager.hashPassword(password);
        String sql = "SELECT id, username, user_type, created_at FROM users WHERE username = ? AND password = ? AND user_type = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, userType);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("user_type"),
                        rs.getString("created_at")
                );
                return new AuthResponse(true, "Вход выполнен успешно", user);
            } else {
                return new AuthResponse(false, "Неверное имя пользователя, пароль или тип пользователя");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при входе пользователя: " + e.getMessage());
            return new AuthResponse(false, "Ошибка сервера при входе");
        }
    }

    // Метод регистрации нового пользователя
    public AuthResponse registerUser(String username, String password, String userType) {
        // Проверяем, не занято ли имя пользователя
        if (isUsernameTaken(username)) {
            return new AuthResponse(false, "Имя пользователя уже занято");
        }

        String hashedPassword = DatabaseManager.hashPassword(password);
        String sql = "INSERT INTO users (username, password, user_type, created_at) VALUES (?, ?, ?, datetime('now'))";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, userType);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Получаем созданного пользователя
                return loginUser(username, password, userType);
            } else {
                return new AuthResponse(false, "Ошибка при регистрации пользователя");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при регистрации пользователя: " + e.getMessage());
            return new AuthResponse(false, "Ошибка сервера при регистрации");
        }
    }

    // Метод, который проверяет, что имя пользователя еще не занято
    private boolean isUsernameTaken(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            System.err.println("Ошибка при проверке имени пользователя: " + e.getMessage());
            return true; // В случае ошибки считаем имя занятым
        }
    }

    // Метод чтобы получить всех врачей из БД
    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT d.*, b.name as branch_name, b.address as branch_address " +
                "FROM doctors d " +
                "JOIN branches b ON d.branch_id = b.id " +
                "ORDER BY d.name";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Doctor doctor = new Doctor(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getInt("branch_id"),
                        rs.getString("branch_name"),
                        rs.getString("branch_address")
                );
                doctors.add(doctor);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении врачей: " + e.getMessage());
        }
        return doctors;
    }

    // Метод чтобы получить все записи из БД
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
                        rs.getString("patient_username"),
                        rs.getInt("doctor_id"),
                        rs.getString("appointment_date"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
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

    // Метод для статистики, который показывает все записи у врачей и фильтрует их (используется только менеджером!)
    public List<Statistic> getDetailedAppointmentsReport(Integer doctorId, Integer branchId, String startDate, String endDate) {
        List<Statistic> statistics = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT a.id, a.patient_username, a.doctor_id, a.appointment_date, " +
                        "a.start_time, a.end_time, a.secret_id, a.status, " +
                        "d.name AS doctor_name, d.specialization AS doctor_specialization, " +
                        "b.name AS branch_name, b.address AS branch_address, " +
                        "s.day_of_week AS doctor_schedule_day_of_week, " +
                        "s.start_time AS doctor_schedule_start_time, " +
                        "s.end_time AS doctor_schedule_end_time " +
                        "FROM appointments a " +
                        "JOIN doctors d ON a.doctor_id = d.id " +
                        "JOIN branches b ON d.branch_id = b.id " +
                        "LEFT JOIN schedules s ON d.id = s.doctor_id AND s.day_of_week = " +
                        "    CASE strftime('%w', a.appointment_date) " +
                        "        WHEN '0' THEN 'Воскресенье' " +
                        "        WHEN '1' THEN 'Понедельник' " +
                        "        WHEN '2' THEN 'Вторник' " +
                        "        WHEN '3' THEN 'Среда' " +
                        "        WHEN '4' THEN 'Четверг' " +
                        "        WHEN '5' THEN 'Пятница' " +
                        "        WHEN '6' THEN 'Суббота' " +
                        "    END "
        );
        List<Object> params = new ArrayList<>();
        boolean hasWhere = false;
        // Фильтр по врачу
        if (doctorId != null) {
            sqlBuilder.append(" WHERE d.id = ?");
            params.add(doctorId);
            hasWhere = true;
        }
        // Фильтр по филиалу
        if (branchId != null) {
            if (!hasWhere) {
                sqlBuilder.append(" WHERE ");
                hasWhere = true;
            } else {
                sqlBuilder.append(" AND ");
            }
            sqlBuilder.append("b.id = ?");
            params.add(branchId);
        }
        // Фильтр по датам
        if (startDate != null && !startDate.trim().isEmpty()) {
            if (!hasWhere) {
                sqlBuilder.append(" WHERE ");
                hasWhere = true;
            } else {
                sqlBuilder.append(" AND ");
            }
            sqlBuilder.append("a.appointment_date >= ?");
            params.add(startDate.trim());
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            if (!hasWhere) {
                sqlBuilder.append(" WHERE ");
                hasWhere = true;
            } else {
                sqlBuilder.append(" AND ");
            }
            sqlBuilder.append("a.appointment_date <= ?");
            params.add(endDate.trim());
        }
        sqlBuilder.append(" ORDER BY a.appointment_date DESC, a.start_time DESC");
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Statistic statistic = new Statistic(
                            rs.getInt("id"),
                            rs.getString("patient_username"),
                            rs.getInt("doctor_id"),
                            rs.getString("appointment_date"),
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            rs.getString("secret_id"),
                            rs.getString("status"),
                            rs.getString("doctor_name"),
                            rs.getString("doctor_specialization"),
                            rs.getString("branch_name"),
                            rs.getString("branch_address"),
                            rs.getString("doctor_schedule_day_of_week"),
                            rs.getString("doctor_schedule_start_time"),
                            rs.getString("doctor_schedule_end_time")
                    );
                    statistics.add(statistic);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении детального отчета по записям: " + e.getMessage());
        }
        return statistics;
    }

    // Метод для создпния записи к врачу
    public boolean createAppointment(Appointment appointment) {
        String sql = "INSERT INTO appointments (patient_username, doctor_id, appointment_date, start_time, end_time, secret_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, appointment.getPatientUsername());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setString(3, appointment.getAppointmentDate());
            pstmt.setString(4, appointment.getStartTime());
            pstmt.setString(5, appointment.getEndTime());
            pstmt.setString(6, appointment.getSecretId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при создании записи: " + e.getMessage());
            return false;
        }
    }

    // Метод для получения записей пациента
    public List<Appointment> getPatientAppointments(String username) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, d.name as doctor_name FROM appointments a " +
                "JOIN doctors d ON a.doctor_id = d.id " +
                "WHERE a.patient_username = ? " +
                "ORDER BY a.appointment_date DESC, a.start_time DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // ИЗМЕНИТЬ создание объекта Appointment с новыми полями
                Appointment appointment = new Appointment(
                        rs.getInt("id"),
                        rs.getString("patient_username"),
                        rs.getInt("doctor_id"),
                        rs.getString("appointment_date"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("secret_id"),
                        rs.getString("status"),
                        rs.getString("doctor_name")
                );
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении записей пациента: " + e.getMessage());
        }
        return appointments;
    }

    // Метод для получения всех имеющихся филиалов
    public List<Branch> getAllBranches() {
        List<Branch> branches = new ArrayList<>();
        String sql = "SELECT * FROM branches ORDER BY name";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Branch branch = new Branch(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("address")
                );
                branches.add(branch);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении филиалов: " + e.getMessage());
        }
        return branches;
    }

    // Метод чтобы получить филиал по его id
    public Branch getBranchById(int branchId) {
        String sql = "SELECT * FROM branches WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, branchId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Branch(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("address")
                );
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Ошибка при получении филиала: " + e.getMessage());
            return null;
        }
    }

    // Метод для получения расписания врача
    public List<Schedule> getAllSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT s.*, d.name as doctor_name, b.name as branch_name " +
                "FROM schedules s " +
                "JOIN doctors d ON s.doctor_id = d.id " +
                "JOIN branches b ON d.branch_id = b.id " +
                "ORDER BY b.name, d.name, s.day_of_week";

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
                schedule.setBranchName(rs.getString("branch_name"));
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении расписания: " + e.getMessage());
        }
        return schedules;
    }

    // Метод для редактирования расписания врача (используется только менеджером!)
    public boolean updateSchedule(Schedule schedule) {

        String sql = "UPDATE schedules SET day_of_week = ?, start_time = ?, end_time = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, schedule.getDayOfWeek());
            pstmt.setString(2, schedule.getStartTime());
            pstmt.setString(3, schedule.getEndTime());
            pstmt.setInt(4, schedule.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении расписания: " + e.getMessage());
            return false;
        }
    }

    // Метод для добавления расписания врача (используется только менеджером!)
    public boolean addSchedule(Schedule schedule) {

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

    // Метод для удаления расписания врача (используется только менеджером!)
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

    // НОВЫЕ МЕТОДЫ ДЛЯ ФУНКЦИОНАЛА ВРАЧА - ДОБАВЛЯЕМ

    // Получение расписания врача - ОБНОВЛЕННЫЙ МЕТОД
    public List<Appointment> getDoctorSchedule(String username) {
        List<Appointment> appointments = new ArrayList<>();

        // Получаем ID врача через таблицу связей
        String doctorIdQuery = "SELECT d.id FROM doctors d " +
                "JOIN doctor_users du ON d.id = du.doctor_id " +
                "JOIN users u ON du.user_id = u.id " +
                "WHERE u.username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(doctorIdQuery)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int doctorId = rs.getInt("id");

                // Получаем расписание врача
                String sql = "SELECT a.*, d.name as doctor_name " +
                        "FROM appointments a " +
                        "JOIN doctors d ON a.doctor_id = d.id " +
                        "WHERE a.doctor_id = ? " +
                        "ORDER BY a.appointment_date, a.start_time";

                try (PreparedStatement scheduleStmt = conn.prepareStatement(sql)) {
                    scheduleStmt.setInt(1, doctorId);
                    ResultSet scheduleRs = scheduleStmt.executeQuery();

                    while (scheduleRs.next()) {
                        Appointment appointment = new Appointment(
                                scheduleRs.getInt("id"),
                                scheduleRs.getString("patient_username"),
                                scheduleRs.getInt("doctor_id"),
                                scheduleRs.getString("appointment_date"),
                                scheduleRs.getString("start_time"),
                                scheduleRs.getString("end_time"),
                                scheduleRs.getString("secret_id"),
                                scheduleRs.getString("status"),
                                scheduleRs.getString("doctor_name")
                        );
                        appointments.add(appointment);
                    }
                }
            } else {
                System.err.println("Врач с username '" + username + "' не найден в таблице связей");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении расписания врача (новый метод): " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }


    // Завершение приема
    public boolean completeAppointment(int appointmentId) {
        String sql = "UPDATE appointments SET status = 'completed' WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appointmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при завершении приема: " + e.getMessage());
            return false;
        }
    }

    // Получение сообщений врача
    public List<Message> getDoctorMessages(String username) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE receiver_username = ? OR sender_username = ? " +
                "ORDER BY timestamp";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Message message = new Message(
                        rs.getInt("id"),
                        rs.getString("sender_username"),
                        rs.getString("receiver_username"),
                        rs.getString("message_text"),
                        rs.getString("timestamp")
                );
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении сообщений врача: " + e.getMessage());
        }
        return messages;
    }

    // Отправка сообщения
    public boolean sendMessage(Message message) {
        String sql = "INSERT INTO messages (sender_username, receiver_username, message_text) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, message.getSenderUsername());
            pstmt.setString(2, message.getReceiverUsername());
            pstmt.setString(3, message.getMessageText());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при отправке сообщения: " + e.getMessage());
            return false;
        }
    }

    // Получение диалогов врача
    public List<Conversation> getDoctorConversations(String username) {
        List<Conversation> conversations = new ArrayList<>();
        String sql = "SELECT DISTINCT " +
                "CASE WHEN sender_username = ? THEN receiver_username ELSE sender_username END as participant, " +
                "MAX(timestamp) as last_message_time " +
                "FROM messages " +
                "WHERE sender_username = ? OR receiver_username = ? " +
                "GROUP BY participant " +
                "ORDER BY last_message_time DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, username);
            pstmt.setString(3, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Conversation conversation = new Conversation(
                        rs.getString("participant"),
                        rs.getString("last_message_time")
                );
                conversations.add(conversation);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении диалогов врача: " + e.getMessage());
        }
        return conversations;
    }

    // НОВЫЙ МЕТОД: Получение сообщений конкретного диалога
    public List<Message> getConversationMessages(String doctorUsername, String patientUsername) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages " +
                "WHERE (sender_username = ? AND receiver_username = ?) " +
                "OR (sender_username = ? AND receiver_username = ?) " +
                "ORDER BY timestamp";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, doctorUsername);
            pstmt.setString(2, patientUsername);
            pstmt.setString(3, patientUsername);
            pstmt.setString(4, doctorUsername);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Message message = new Message(
                        rs.getInt("id"),
                        rs.getString("sender_username"),
                        rs.getString("receiver_username"),
                        rs.getString("message_text"),
                        rs.getString("timestamp")
                );
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении сообщений диалога: " + e.getMessage());
        }
        return messages;
    }

    // НОВЫЙ МЕТОД для получения расписания врача с исправленным запросом
    public List<Appointment> getDoctorScheduleFixed(String username) {
        List<Appointment> appointments = new ArrayList<>();

        // Исправленный запрос: находим врача по username пользователя
        String doctorIdQuery = "SELECT d.id FROM doctors d " +
                "JOIN users u ON d.name = (SELECT name FROM users WHERE username = ? AND user_type = 'DOCTOR' LIMIT 1)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(doctorIdQuery)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int doctorId = rs.getInt("id");

                // Получаем расписание для этого врача
                String sql = "SELECT a.*, d.name as doctor_name " +
                        "FROM appointments a " +
                        "JOIN doctors d ON a.doctor_id = d.id " +
                        "WHERE a.doctor_id = ? " +
                        "AND a.appointment_date >= date('now') " +
                        "ORDER BY a.appointment_date, a.start_time";

                try (PreparedStatement scheduleStmt = conn.prepareStatement(sql)) {
                    scheduleStmt.setInt(1, doctorId);
                    ResultSet scheduleRs = scheduleStmt.executeQuery();

                    while (scheduleRs.next()) {
                        Appointment appointment = new Appointment(
                                scheduleRs.getInt("id"),
                                scheduleRs.getString("patient_username"),
                                scheduleRs.getInt("doctor_id"),
                                scheduleRs.getString("appointment_date"),
                                scheduleRs.getString("start_time"),
                                scheduleRs.getString("end_time"),
                                scheduleRs.getString("secret_id"),
                                scheduleRs.getString("status"),
                                scheduleRs.getString("doctor_name")
                        );
                        appointments.add(appointment);
                    }
                }
            } else {
                System.err.println("Врач не найден для username: " + username);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении расписания врача: " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }
    // Альтернативный метод для получения расписания врача (без изменения структуры БД)
    public List<Appointment> getDoctorScheduleByUsername(String username) {
        List<Appointment> appointments = new ArrayList<>();

        System.out.println("[DEBUG] Поиск расписания для пользователя: " + username);

        // Запрос через связь users.name -> doctors.name
        String sql = "SELECT a.*, d.name as doctor_name " +
                "FROM appointments a " +
                "JOIN doctors d ON a.doctor_id = d.id " +
                "WHERE d.name = (SELECT name FROM users WHERE username = ? AND user_type = 'DOCTOR') " +
                "ORDER BY a.appointment_date, a.start_time";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                Appointment appointment = new Appointment(
                        rs.getInt("id"),
                        rs.getString("patient_username"),
                        rs.getInt("doctor_id"),
                        rs.getString("appointment_date"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("secret_id"),
                        rs.getString("status"),
                        rs.getString("doctor_name")
                );
                appointments.add(appointment);
                count++;
            }

            System.out.println("[DEBUG] Найдено записей: " + count);

        } catch (SQLException e) {
            System.err.println("Ошибка при получении расписания врача: " + e.getMessage());
            e.printStackTrace();
        }

        return appointments;
    }
    // Метод для проверки связи между users и doctors
    public Map<String, String> checkDoctorData(String username) {
        Map<String, String> result = new HashMap<>();

        String sql = "SELECT u.username, u.name as user_name, u.user_type, " +
                "d.id as doctor_id, d.name as doctor_name " +
                "FROM users u " +
                "LEFT JOIN doctors d ON u.name = d.name " +
                "WHERE u.username = ? AND u.user_type = 'DOCTOR'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                result.put("user_exists", "true");
                result.put("username", rs.getString("username"));
                result.put("user_name", rs.getString("user_name"));
                result.put("user_type", rs.getString("user_type"));
                result.put("doctor_id", String.valueOf(rs.getInt("doctor_id")));
                result.put("doctor_name", rs.getString("doctor_name"));
                result.put("match", rs.getString("user_name") != null &&
                        rs.getString("user_name").equals(rs.getString("doctor_name")) ? "true" : "false");
            } else {
                result.put("user_exists", "false");
            }

        } catch (SQLException e) {
            result.put("error", e.getMessage());
        }

        return result;
    }
}