package com.supermed.patient.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.supermed.patient.R;
import com.supermed.patient.api.ApiClient;
import com.supermed.patient.api.ApiService;
import com.supermed.patient.model.Appointment;
import com.supermed.patient.model.Schedule;
import com.supermed.patient.model.TimeSlot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleActivity extends AppCompatActivity {

    private TextView tvDoctorName;
    private ListView listView;
    private ProgressBar progressBar;
    private ApiService apiService;
    private SlotAdapter adapter;
    private int doctorId;
    private String doctorName;
    private String patientUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_with_header);

        doctorId = getIntent().getIntExtra("doctorId", -1);
        doctorName = getIntent().getStringExtra("doctorName");

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        patientUsername = prefs.getString("username", null);

        if (doctorId == -1 || doctorName == null || patientUsername == null) {
            Toast.makeText(this, "Ошибка данных", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // UI
        tvDoctorName = findViewById(R.id.tv_header);
        listView = findViewById(R.id.list_view);
        progressBar = findViewById(R.id.progress_bar);
        tvDoctorName.setText("Запись к: " + doctorName);

        apiService = ApiClient.getService();
        adapter = new SlotAdapter(this, R.layout.item_slot);
        listView.setAdapter(adapter);

        loadScheduleAndAppointments();

        // Нажатие на слот → запись
        listView.setOnItemClickListener((parent, view, position, id) -> {
            TimeSlot slot = adapter.getItem(position);
            if (slot != null) {
                createAppointment(slot);
            }
        });
    }

    private void loadScheduleAndAppointments() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getSchedules().enqueue(new Callback<List<Schedule>>() {
            @Override
            public void onResponse(Call<List<Schedule>> call, Response<List<Schedule>> responseSchedules) {
                apiService.getAppointments().enqueue(new Callback<List<Appointment>>() {
                    @Override
                    public void onResponse(Call<List<Appointment>> call2, Response<List<Appointment>> responseAppointments) {
                        progressBar.setVisibility(View.GONE);

                        if (responseSchedules.isSuccessful() && responseAppointments.isSuccessful()
                                && responseSchedules.body() != null && responseAppointments.body() != null) {

                            List<Schedule> doctorSchedules = new ArrayList<>();
                            for (Schedule s : responseSchedules.body()) {
                                if (s.getDoctorId() == doctorId) {
                                    doctorSchedules.add(s);
                                }
                            }

                            List<Appointment> doctorAppointments = new ArrayList<>();
                            for (Appointment a : responseAppointments.body()) {
                                if (a.getDoctorId() == doctorId) {
                                    doctorAppointments.add(a);
                                }
                            }

                            List<TimeSlot> freeSlots = generateFreeSlots(doctorSchedules, doctorAppointments);
                            adapter.clear();
                            adapter.addAll(freeSlots);
                        } else {
                            Toast.makeText(ScheduleActivity.this, "Не удалось загрузить данные", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Appointment>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ScheduleActivity.this, "Ошибка загрузки записей", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Schedule>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ScheduleActivity.this, "Ошибка загрузки расписания", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<TimeSlot> generateFreeSlots(List<Schedule> schedules, List<Appointment> appointments) {
        List<TimeSlot> slots = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();

        for (int dayOffset = 0; dayOffset <= 7; dayOffset++) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, dayOffset);
            String currentDate = sdf.format(cal.getTime());
            String dayOfWeek = getDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));

            for (Schedule s : schedules) {
                if (s.getDayOfWeek() != null && s.getDayOfWeek().equals(dayOfWeek)) {
                    List<String> occupied = new ArrayList<>();
                    for (Appointment a : appointments) {
                        if (a.getAppointmentDate() != null && a.getAppointmentDate().equals(currentDate)) {
                            occupied.add(a.getStartTime());
                        }
                    }

                    String start = s.getStartTime();
                    String end = s.getEndTime();

                    if (start == null || end == null) continue;

                    try {
                        Calendar slotTime = Calendar.getInstance();
                        slotTime.setTime(sdf.parse(currentDate));
                        String[] startParts = start.split(":");
                        slotTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startParts[0]));
                        slotTime.set(Calendar.MINUTE, Integer.parseInt(startParts[1]));

                        Calendar endTime = Calendar.getInstance();
                        endTime.setTime(sdf.parse(currentDate));
                        String[] endParts = end.split(":");
                        endTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endParts[0]));
                        endTime.set(Calendar.MINUTE, Integer.parseInt(endParts[1]));

                        while (slotTime.before(endTime)) {
                            String slotStr = String.format("%02d:%02d",
                                    slotTime.get(Calendar.HOUR_OF_DAY),
                                    slotTime.get(Calendar.MINUTE)
                            );

                            if (!occupied.contains(slotStr)) {
                                slots.add(new TimeSlot(currentDate, slotStr));
                            }

                            slotTime.add(Calendar.MINUTE, 30);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return slots;
    }

    private String getDayOfWeek(int day) {
        switch (day) {
            case Calendar.MONDAY: return "Понедельник";
            case Calendar.TUESDAY: return "Вторник";
            case Calendar.WEDNESDAY: return "Среда";
            case Calendar.THURSDAY: return "Четверг";
            case Calendar.FRIDAY: return "Пятница";
            case Calendar.SATURDAY: return "Суббота";
            case Calendar.SUNDAY: return "Воскресенье";
            default: return "";
        }
    }

    private void createAppointment(TimeSlot slot) {
        String secretId = "SEC" + System.currentTimeMillis();

        Appointment appointment = new Appointment();
        appointment.setPatientUsername(patientUsername);
        appointment.setDoctorId(doctorId);
        appointment.setAppointmentDate(slot.date);
        appointment.setStartTime(slot.time);
        appointment.setEndTime(add30Minutes(slot.time));
        appointment.setSecretId(secretId);
        appointment.setStatus("scheduled");

        apiService.createAppointment(appointment).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ScheduleActivity.this, "Запись создана!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ScheduleActivity.this, "Слот уже занят", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ScheduleActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String add30Minutes(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int min = Integer.parseInt(parts[1]) + 30;
        if (min >= 60) {
            hour += 1;
            min -= 60;
        }
        return String.format("%02d:%02d", hour, min);
    }
}