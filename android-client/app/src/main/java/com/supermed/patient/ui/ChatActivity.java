package com.supermed.patient.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.supermed.patient.R;
import com.supermed.patient.api.ApiClient;
import com.supermed.patient.api.ApiService;
import com.supermed.patient.model.Message;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private ListView messageListView;
    private EditText etMessage;
    private Button btnSend;
    private ProgressBar progressBar;
    private TextView tvReceiver;
    private ApiService apiService;
    private MessageAdapter adapter;
    private String currentUsername;      // например, "p.kotova"
    private String receiverUsername;     // например, "d.ivanov"
    private static final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiverUsername = getIntent().getStringExtra("receiver");
        if (receiverUsername == null) {
            Toast.makeText(this, "Ошибка: врач не выбран", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        currentUsername = prefs.getString("username", null);
        if (currentUsername == null) {
            Toast.makeText(this, "Вы не авторизованы", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализация UI
        tvReceiver = findViewById(R.id.tv_receiver);
        messageListView = findViewById(R.id.list_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        progressBar = findViewById(R.id.progress_bar);
        tvReceiver.setText("Врач: " + receiverUsername);

        Log.d(TAG, "Инициализация чата: currentUsername=" + currentUsername + ", receiverUsername=" + receiverUsername);

        apiService = ApiClient.getService();
        adapter = new MessageAdapter(this, 0, new ArrayList<>());
        messageListView.setAdapter(adapter);

        loadMessages();
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        Log.d(TAG, "Загрузка сообщений между patient: " + currentUsername + " и doctor: " + receiverUsername);
        progressBar.setVisibility(View.VISIBLE);

        // Правильный порядок: первый параметр - doctor, второй - patient
        apiService.getMessages(receiverUsername, currentUsername).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Ответ от сервера: код=" + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<Message> messages = response.body();
                    Log.d(TAG, "Получено сообщений: " + messages.size());

                    for (Message msg : messages) {
                        Log.d(TAG, "Сообщение: " + msg.getMessageText() +
                                " от: " + msg.getSenderUsername() +
                                " время: " + msg.getTimestamp());
                    }

                    adapter.clear();
                    adapter.addAll(messages);
                    messageListView.smoothScrollToPosition(adapter.getCount());
                } else {
                    Log.e(TAG, "Ошибка загрузки: " + response.code() + " " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Тело ошибки: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка чтения тела ошибки", e);
                    }
                    Toast.makeText(ChatActivity.this, "Не удалось загрузить сообщения", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Сетевая ошибка", t);
                Toast.makeText(ChatActivity.this, "Ошибка подключения: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        Log.d(TAG, "Отправка сообщения: " + text);

        Message msg = new Message();
        msg.setSenderUsername(currentUsername);
        msg.setReceiverUsername(receiverUsername);
        msg.setMessageText(text);

        etMessage.setText("");
        adapter.add(msg); // Добавляем локально для немедленного отображения
        messageListView.smoothScrollToPosition(adapter.getCount());

        apiService.sendMessage(msg).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Сообщение успешно отправлено");
                    // Обновляем сообщение с серверным ID и временем
                    int lastIndex = adapter.getCount() - 1;
                    if (lastIndex >= 0) {
                        Message updatedMsg = response.body();
                        adapter.remove(adapter.getItem(lastIndex));
                        adapter.insert(updatedMsg, lastIndex);
                    }
                } else {
                    Log.e(TAG, "Ошибка отправки: " + response.code());
                    // Удаляем неотправленное сообщение
                    adapter.remove(msg);
                    Toast.makeText(ChatActivity.this, "Не удалось отправить сообщение", Toast.LENGTH_SHORT).show();
                }
                loadMessages(); // Обновляем список для синхронизации
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                Log.e(TAG, "Ошибка сети при отправке", t);
                // Удаляем неотправленное сообщение
                adapter.remove(msg);
                Toast.makeText(ChatActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}