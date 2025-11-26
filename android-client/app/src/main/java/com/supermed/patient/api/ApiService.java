package com.supermed.patient.api;

import com.supermed.patient.model.*;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // === Аутентификация ===
    @POST("/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("/auth/register")
    Call<AuthResponse> register(@Body AuthRequest request);

    // === Основные данные ===
    @GET("/branches")
    Call<List<Branch>> getBranches();

    @GET("/doctors")
    Call<List<Doctor>> getDoctors();

    @GET("/schedules")
    Call<List<Schedule>> getSchedules();

    // === Записи (Appointments) ===
    @GET("/appointments")
    Call<List<Appointment>> getAppointments();

    @POST("/appointments")
    Call<Void> createAppointment(@Body Appointment appointment);

    // === Статистика (не используется у пациента, но есть в API) ===
    @GET("/statistics")
    Call<List<Statistic>> getStatistics(
            @Query("doctorId") Integer doctorId,
            @Query("branchId") Integer branchId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    // === Управление расписанием (только для менеджера, не используется у пациента) ===
    @PUT("/schedules/{id}")
    Call<Void> updateSchedule(@Path("id") int id, @Body Schedule schedule);

    @POST("/schedules")
    Call<Void> addSchedule(@Body Schedule schedule);

    @DELETE("/schedules/{id}")
    Call<Void> deleteSchedule(@Path("id") int id);

    // === Дополнительно (если понадобится позже) ===
    // GET /branches/{id} — получение филиала по ID
    @GET("/branches/{id}")
    Call<Branch> getBranchById(@Path("id") int id);
}