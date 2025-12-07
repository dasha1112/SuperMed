package com.supermed.patient.ui;

import android.content.Context;
import android.content.Intent;
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
import com.supermed.patient.model.Doctor;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorActivity extends AppCompatActivity {

    private ListView listView;
    private ProgressBar progressBar;
    private TextView tvBranchName;
    private ApiService apiService;
    private DoctorAdapter adapter;
    private int selectedBranchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_with_header);

        // Получаем данные из Intent
        selectedBranchId = getIntent().getIntExtra("branchId", -1);
        String branchName = getIntent().getStringExtra("branchName");

        if (selectedBranchId == -1 || branchName == null) {
            Toast.makeText(this, "Ошибка: не указан филиал", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Привязка UI
        tvBranchName = findViewById(R.id.tv_header);
        listView = findViewById(R.id.list_view);
        progressBar = findViewById(R.id.progress_bar);

        tvBranchName.setText("Врачи филиала: " + branchName);

        apiService = ApiClient.getService();
        adapter = new DoctorAdapter(this, R.layout.item_doctor);
        listView.setAdapter(adapter);

        loadDoctors();
    }

    private void loadDoctors() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getDoctors().enqueue(new Callback<List<Doctor>>() {
            @Override
            public void onResponse(Call<List<Doctor>> call, Response<List<Doctor>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Doctor> allDoctors = response.body();
                    List<Doctor> filtered = new ArrayList<>();
                    for (Doctor d : allDoctors) {
                        if (d.getBranchId() == selectedBranchId) {
                            filtered.add(d);
                        }
                    }
                    adapter.clear();
                    adapter.addAll(filtered);
                } else {
                    Toast.makeText(DoctorActivity.this, "Не удалось загрузить врачей", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Doctor>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DoctorActivity.this, "Нет подключения к серверу", Toast.LENGTH_SHORT).show();
            }
        });

        // Нажатие на врача - расписание
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Doctor doctor = adapter.getItem(position);
            Intent intent = new Intent(DoctorActivity.this, ScheduleActivity.class);
            intent.putExtra("doctorId", doctor.getId());
            intent.putExtra("doctorName", doctor.getName());
            startActivity(intent);
        });
    }
}

