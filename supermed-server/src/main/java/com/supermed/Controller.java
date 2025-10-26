package com.supermed;

import com.google.gson.Gson;
import com.supermed.entities.*;
import java.util.List;
import static spark.Spark.*;

public class Controller {
    private static final Gson gson = new Gson();
    private static final Model model = new Model();

    public static void initRoutes() {
        // Получить всех врачей
        get("/doctors", (req, res) -> {
            List<Doctor> doctors = model.getAllDoctors();
            return gson.toJson(doctors);
        });

        // Получить все записи
        get("/appointments", (req, res) -> {
            List<Appointment> appointments = model.getAllAppointments();
            return gson.toJson(appointments);
        });

        // Создать новую запись
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

        // Получить статистику
        get("/statistics", (req, res) -> {
            List<Statistics> stats = model.getStatistics();
            return gson.toJson(stats);
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
                return "{\"error\": \"Не удалось обновить расписание. Проверьте, что рабочий день не превышает 8 часов\"}";
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
                return "{\"error\": \"Не удалось добавить расписание. Проверьте, что рабочий день не превышает 8 часов\"}";
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
    }
}