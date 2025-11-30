package com.supermed.patient.ui;

import android.content.Context;
import android.content.Intent;
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
import com.supermed.patient.model.Doctor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationsActivity extends AppCompatActivity {
    private ListView listView;
    private ProgressBar progressBar;
    private ApiService apiService;
    private ConversationAdapter adapter;
    private String currentUsername;

    // Маппинг doctor.id -> username (временное решение для демо)
    private static final Map<Integer, String> DOCTOR_USERNAMES = new HashMap<>();
    static {
        DOCTOR_USERNAMES.put(1, "d.ivanov");      // Иванов Иван Алексеевич
        DOCTOR_USERNAMES.put(2, "e.petrova");     // Петрова Елена Васильевна
        DOCTOR_USERNAMES.put(3, "a.sidorov");     // Сидоров Александр Викторович
        DOCTOR_USERNAMES.put(4, "o.kuznetsova");  // Кузнецова Ольга Сергеевна
        DOCTOR_USERNAMES.put(5, "d.nikolaev");    // Николаев Дмитрий Викторович
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_with_header);
        TextView header = findViewById(R.id.tv_header);
        header.setText("Сообщения");
        listView = findViewById(R.id.list_view);
        progressBar = findViewById(R.id.progress_bar);
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        currentUsername = prefs.getString("username", null);
        if (currentUsername == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        apiService = ApiClient.getService();
        adapter = new ConversationAdapter(this, R.layout.item_conversation);
        listView.setAdapter(adapter);
        loadConversations();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Doctor doctor = adapter.getItem(position);
            if (doctor != null) {
                String doctorUsername = getDoctorUsernameByDoctorId(doctor.getId());
                if (doctorUsername == null) {
                    Toast.makeText(this, "Не удалось определить врача для переписки", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(ConversationsActivity.this, ChatActivity.class);
                intent.putExtra("receiver", doctorUsername);
                startActivity(intent);
            }
        });
    }

    private void loadConversations() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getAppointments().enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> responseAppointments) {
                if (!responseAppointments.isSuccessful() || responseAppointments.body() == null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ConversationsActivity.this, "Не удалось загрузить записи", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<Appointment> allAppointments = responseAppointments.body();
                Set<Integer> doctorIds = new HashSet<>();
                for (Appointment a : allAppointments) {
                    if (currentUsername.equals(a.getPatientUsername())) {
                        doctorIds.add(a.getDoctorId());
                    }
                }
                if (doctorIds.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    adapter.clear();
                    Toast.makeText(ConversationsActivity.this, "У вас нет записей к врачам", Toast.LENGTH_SHORT).show();
                    return;
                }
                apiService.getDoctors().enqueue(new Callback<List<Doctor>>() {
                    @Override
                    public void onResponse(Call<List<Doctor>> call, Response<List<Doctor>> responseDoctors) {
                        progressBar.setVisibility(View.GONE);
                        if (responseDoctors.isSuccessful() && responseDoctors.body() != null) {
                            List<Doctor> filteredDoctors = new ArrayList<>();
                            for (Doctor d : responseDoctors.body()) {
                                if (doctorIds.contains(d.getId()) && DOCTOR_USERNAMES.containsKey(d.getId())) {
                                    filteredDoctors.add(d);
                                }
                            }
                            if (filteredDoctors.isEmpty()) {
                                Toast.makeText(ConversationsActivity.this, "Нет доступных врачей для переписки", Toast.LENGTH_SHORT).show();
                            }
                            adapter.clear();
                            adapter.addAll(filteredDoctors);
                        } else {
                            Toast.makeText(ConversationsActivity.this, "Не удалось загрузить врачей", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Doctor>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ConversationsActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ConversationsActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getDoctorUsernameByDoctorId(int doctorId) {
        return DOCTOR_USERNAMES.get(doctorId);
    }

    public static class ConversationAdapter extends ArrayAdapter<Doctor> {
        public ConversationAdapter(@NonNull Context context, int resource) {
            super(context, resource, new ArrayList<>());
        }
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_conversation, parent, false);
            }
            Doctor doctor = getItem(position);
            if (doctor != null) {
                TextView tvParticipant = convertView.findViewById(R.id.tv_participant);
                TextView tvTime = convertView.findViewById(R.id.tv_last_time);
                tvParticipant.setText(doctor.getName());
                tvTime.setText("Врач, к которому вы записаны");
            }
            return convertView;
        }
    }
}