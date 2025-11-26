package com.supermed.patient.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.supermed.patient.R;
import com.supermed.patient.api.ApiClient;
import com.supermed.patient.api.ApiService;
import com.supermed.patient.model.Appointment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAppointmentsActivity extends AppCompatActivity {

    private TextView tvTitle;
    private ListView listView;
    private ProgressBar progressBar;
    private ApiService apiService;
    private AppointmentAdapter adapter;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_with_header);

        // Получаем имя пользователя из сессии
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        currentUsername = prefs.getString("username", null);

        if (currentUsername == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Привязка UI
        tvTitle = findViewById(R.id.tv_header);
        listView = findViewById(R.id.list_view);
        progressBar = findViewById(R.id.progress_bar);

        tvTitle.setText("Мои записи");
        apiService = ApiClient.getService();
        adapter = new AppointmentAdapter(this, R.layout.item_appointment);
        listView.setAdapter(adapter);

        loadAppointments();
    }

    private void loadAppointments() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getAppointments().enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Appointment> allAppointments = response.body();
                    List<Appointment> myAppointments = new ArrayList<>();

                    for (Appointment a : allAppointments) {
                        if (currentUsername.equals(a.getPatientUsername())) {
                            myAppointments.add(a);
                        }
                    }

                    if (myAppointments.isEmpty()) {
                        Toast.makeText(MyAppointmentsActivity.this, "У вас пока нет записей", Toast.LENGTH_SHORT).show();
                    }

                    adapter.clear();
                    adapter.addAll(myAppointments);
                } else {
                    Toast.makeText(MyAppointmentsActivity.this, "Не удалось загрузить записи", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MyAppointmentsActivity.this, "Нет подключения к серверу", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
