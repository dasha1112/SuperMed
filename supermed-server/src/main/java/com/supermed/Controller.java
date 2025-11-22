package com.supermed;

import com.google.gson.Gson;
import com.supermed.entities.*;
import java.util.List;
import static spark.Spark.*;

public class Controller {
    private static final Gson gson = new Gson();
    private static final Model model = new Model();

    public static void initRoutes() {
        // Настройка CORS
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Content-Length, X-Requested-With");
        });

        options("/*", (req, res) -> {
            res.status(200);
            return "OK";
        });

        // Endpoint'ы аутентификации
        post("/auth/login", (req, res) -> {
            AuthRequest authRequest = gson.fromJson(req.body(), AuthRequest.class);
            AuthResponse response = model.loginUser(
                    authRequest.getUsername(),
                    authRequest.getPassword(),
                    authRequest.getUserType()
            );
            return gson.toJson(response);
        });

        post("/auth/register", (req, res) -> {
            RegisterRequest registerRequest = gson.fromJson(req.body(), RegisterRequest.class);
            AuthResponse response = model.registerUser(
                    registerRequest.getUsername(),
                    registerRequest.getPassword(),
                    registerRequest.getUserType()
            );
            return gson.toJson(response);
        });

        // Существующие endpoint'ы API
        get("/doctors", (req, res) -> {
            List<Doctor> doctors = model.getAllDoctors();
            return gson.toJson(doctors);
        });

        get("/appointments", (req, res) -> {
            List<Appointment> appointments = model.getAllAppointments();
            return gson.toJson(appointments);
        });

        post("/appointments", (req, res) -> {
            Appointment newAppointment = gson.fromJson(req.body(), Appointment.class);
            boolean success = model.createAppointment(newAppointment);
            if (success) {
                res.status(201);
                return "{\"message\": \"Запись создана успешно\"}";
            } else {
                res.status(400);
                return "{\"error\": \"Не удалось создать запись\"}";
            }
        });

        get("/statistics", (req, res) -> {
            res.type("application/json");
            // Получаем параметры запроса
            Integer doctorId = null;
            if (req.queryParams("doctorId") != null && !req.queryParams("doctorId").isEmpty()) {
                doctorId = Integer.parseInt(req.queryParams("doctorId"));
            }
            Integer branchId = null;
            if (req.queryParams("branchId") != null && !req.queryParams("branchId").isEmpty()) {
                branchId = Integer.parseInt(req.queryParams("branchId"));
            }
            String startDate = req.queryParams("startDate");
            String endDate = req.queryParams("endDate");
            List<Statistic> statistics = model.getDetailedAppointmentsReport(doctorId, branchId, startDate, endDate);
            return gson.toJson(statistics);
        });

        get("/schedules", (req, res) -> {
            List<Schedule> schedules = model.getAllSchedules();
            return gson.toJson(schedules);
        });

        put("/schedules/:id", (req, res) -> {
            int scheduleId = Integer.parseInt(req.params("id"));
            Schedule updatedSchedule = gson.fromJson(req.body(), Schedule.class);
            boolean success = model.updateSchedule(updatedSchedule);
            if (success) {
                return "{\"message\": \"Расписание обновлено успешно\"}";
            } else {
                res.status(400);
                return "{\"error\": \"Не удалось обновить расписание.\"}";
            }
        });

        post("/schedules", (req, res) -> {
            Schedule newSchedule = gson.fromJson(req.body(), Schedule.class);
            boolean success = model.addSchedule(newSchedule);
            if (success) {
                res.status(201);
                return "{\"message\": \"Расписание добавлено успешно\"}";
            } else {
                res.status(400);
                return "{\"error\": \"Не удалось добавить расписание.\"}";
            }
        });

        delete("/schedules/:id", (req, res) -> {
            int scheduleId = Integer.parseInt(req.params("id"));
            boolean success = model.deleteSchedule(scheduleId);
            if (success) {
                return "{\"message\": \"Расписание удалено успешно\"}";
            } else {
                res.status(400);
                return "{\"error\": \"Не удалось удалить расписание\"}";
            }
        });
        get("/branches", (req, res) -> {
            List<Branch> branches = model.getAllBranches();
            return gson.toJson(branches);
        });

        get("/branches/:id", (req, res) -> {
            int branchId = Integer.parseInt(req.params("id"));
            Branch branch = model.getBranchById(branchId);
            if (branch != null) {
                return gson.toJson(branch);
            } else {
                res.status(404);
                return "{\"error\": \"Филиал не найден\"}";
            }
        });

        // НОВЫЕ ЭНДПОИНТЫ ДЛЯ ВРАЧА - ДОБАВЛЯЕМ

        // Получение расписания врача
        get("/api/doctor/:username/schedule", (req, res) -> {
            String username = req.params("username");
            List<Appointment> schedule = model.getDoctorSchedule(username);
            return gson.toJson(schedule);
        });

        // Завершение приема
        post("/api/appointment/:id/complete", (req, res) -> {
            int appointmentId = Integer.parseInt(req.params("id"));
            boolean success = model.completeAppointment(appointmentId);
            if (success) {
                return "{\"message\": \"Прием завершен успешно\"}";
            } else {
                res.status(400);
                return "{\"error\": \"Не удалось завершить прием\"}";
            }
        });

        // Получение сообщений врача
        get("/api/doctor/:username/messages", (req, res) -> {
            String username = req.params("username");
            List<Message> messages = model.getDoctorMessages(username);
            return gson.toJson(messages);
        });

        // Отправка сообщения
        post("/api/messages/send", (req, res) -> {
            Message newMessage = gson.fromJson(req.body(), Message.class);
            boolean success = model.sendMessage(newMessage);
            if (success) {
                res.status(201);
                return "{\"message\": \"Сообщение отправлено успешно\"}";
            } else {
                res.status(400);
                return "{\"error\": \"Не удалось отправить сообщение\"}";
            }
        });

        // Получение диалогов врача
        get("/api/doctor/:username/conversations", (req, res) -> {
            String username = req.params("username");
            List<Conversation> conversations = model.getDoctorConversations(username);
            return gson.toJson(conversations);
        });

        // НОВЫЙ ЭНДПОИНТ: Получение сообщений конкретного диалога
        get("/api/messages/conversation", (req, res) -> {
            String doctorUsername = req.queryParams("doctor");
            String patientUsername = req.queryParams("patient");
            List<Message> messages = model.getConversationMessages(doctorUsername, patientUsername);
            return gson.toJson(messages);
        });
    }
}