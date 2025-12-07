package com.supermed.patient.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.supermed.patient.R;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnBranches, btnMyAppointments, btnLogout;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String username = prefs.getString("username", null);

        // Если не залогинен — отправляем на логин
        if (username == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Привязка UI
        tvWelcome = findViewById(R.id.tv_welcome);
        btnBranches = findViewById(R.id.btn_branches);
        btnMyAppointments = findViewById(R.id.btn_my_appointments);
        btnLogout = findViewById(R.id.btn_logout);

        tvWelcome.setText("Здравствуйте, " + username + "!");

        // Обработчики
        btnBranches.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BranchActivity.class));
        });

        btnMyAppointments.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MyAppointmentsActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        Button btnMessages = findViewById(R.id.btn_messages);
        btnMessages.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ConversationsActivity.class));
        });
    }
}
