package com.supermed;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.supermed.entities.Appointment;
import com.supermed.entities.Conversation;
import com.supermed.entities.Message;
import com.supermed.entities.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DoctorController implements Initializable {

    private static final String BASE_URL = "http://localhost:4567";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @FXML private Label userInfoLabel;
    @FXML private Label statusLabel;
    @FXML private TableView<Appointment> doctorScheduleTable;
    @FXML private TableColumn<Appointment, String> colPatName;
    @FXML private TableColumn<Appointment, String> colAppDate;
    @FXML private TableColumn<Appointment, String> colAppTimeStart;
    @FXML private TableColumn<Appointment, String> colAppTimeEnd;
    @FXML private TableColumn<Appointment, String> colAppSecretId;
    @FXML private TableColumn<Appointment, String> colAppStatus;
    @FXML private TableColumn<Appointment, Void> colAppActions; // Для кнопки "Завершить прием"
    @FXML private Label scheduleCountLabel;

    // Элементы для чата
    @FXML private ListView<Conversation> conversationListView;
    @FXML private Label currentConversationLabel;
    @FXML private TextArea messageHistoryArea;
    @FXML private TextField messageInput;

    private User currentUser;
    private ObservableList<Appointment> doctorAppointmentsData = FXCollections.observableArrayList();
    private ObservableList<Conversation> conversationsData = FXCollections.observableArrayList();
    private Conversation selectedConversation; // Текущий выбранный диалог

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupScheduleTable();
        setupConversationList();
        updateStatus("Готов к работе", "info");

        // Привязываем слушателя к выбору диалога
        conversationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedConversation = newVal;
                currentConversationLabel.setText("Диалог с: " + selectedConversation.getParticipant());
                loadConversationMessages(currentUser.getUsername(), selectedConversation.getParticipant());
            } else {
                currentConversationLabel.setText("Выберите диалог");
                messageHistoryArea.clear();
                selectedConversation = null;
            }
        });

        // Деактивируем поле ввода сообщения и кнопку, пока не выбран диалог
        messageInput.disableProperty().bind(conversationListView.getSelectionModel().selectedItemProperty().isNull());
        messageInput.disableProperty().bind(conversationListView.getSelectionModel().selectedItemProperty().isNull()); // Кнопка отправки
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (userInfoLabel != null && user != null) {
            userInfoLabel.setText("Пользователь: " + user.getUsername() + " (" + getUserTypeDisplayName(user.getUserType()) + ")");
            // После установки пользователя загружаем его данные
            refreshSchedule();
            loadConversations();
        }
    }

    private String getUserTypeDisplayName(String userType) {
        switch (userType) {
            case "MANAGER": return "Менеджер";
            case "DOCTOR": return "Врач";
            case "PATIENT": return "Пациент";
            default: return "Пользователь";
        }
    }

    // --- Методы для вкладки "Расписание" ---
    private void setupScheduleTable() {
        colPatName.setCellValueFactory(new PropertyValueFactory<>("patientUsername"));
        colAppDate.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        colAppTimeStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colAppTimeEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colAppSecretId.setCellValueFactory(new PropertyValueFactory<>("secretId"));
        colAppStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colAppActions.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Appointment, Void> call(final TableColumn<Appointment, Void> param) {
                final TableCell<Appointment, Void> cell = new TableCell<>() {
                    private final Button completeButton = new Button("Завершить");
                    {
                        completeButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 3 6;");
                        completeButton.setOnAction(event -> {
                            Appointment appointment = getTableView().getItems().get(getIndex());
                            if ("scheduled".equals(appointment.getStatus())) {
                                completeAppointment(appointment);
                            } else {
                                showAlert("Информация", "Прием уже завершен или отменен.", "INFO");
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Appointment appointment = getTableView().getItems().get(getIndex());
                            if ("scheduled".equals(appointment.getStatus())) {
                                setGraphic(completeButton);
                            } else {
                                setGraphic(new Label(appointment.getStatus())); // Можно отображать статус
                            }
                        }
                    }
                };
                return cell;
            }
        });
        doctorScheduleTable.setItems(doctorAppointmentsData);
    }

    @FXML
    private void refreshSchedule() {
        if (currentUser == null || !currentUser.getUserType().equals("DOCTOR")) {
            showAlert("Ошибка доступа", "Только врачи могут просматривать свое расписание.", "ERROR");
            return;
        }

        updateStatus("Загрузка расписания...", "info");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/doctor/" + currentUser.getUsername() + "/schedule"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Type appointmentListType = new TypeToken<List<Appointment>>(){}.getType();
                List<Appointment> appointments = gson.fromJson(response.body(), appointmentListType);
                doctorAppointmentsData.clear();
                doctorAppointmentsData.addAll(appointments);
                scheduleCountLabel.setText("Всего записей: " + appointments.size());
                updateStatus("Расписание загружено успешно", "success");
            } else {
                updateStatus("Ошибка загрузки расписания: " + response.statusCode(), "error");
                showAlert("Ошибка", "Не удалось загрузить расписание.", "ERROR");
            }
        } catch (IOException | InterruptedException e) {
            updateStatus("Ошибка подключения к серверу", "error");
            showAlert("Ошибка", "Не удалось подключиться к серверу.", "ERROR");
            System.err.println("Ошибка при загрузке расписания доктора: " + e.getMessage());
        }
    }

    private void completeAppointment(Appointment appointment) {
        if (currentUser == null || !currentUser.getUserType().equals("DOCTOR")) {
            showAlert("Ошибка доступа", "Только врачи могут завершать приемы.", "ERROR");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Подтверждение завершения приема");
        confirmation.setHeaderText("Завершение приема");
        confirmation.setContentText("Вы уверены, что хотите завершить прием с пациентом " +
                appointment.getPatientUsername() + " на " + appointment.getAppointmentDate() + " в " + appointment.getStartTime() + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/appointment/" + appointment.getId() + "/complete"))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    updateStatus("Прием завершен успешно", "success");
                    refreshSchedule(); // Обновить расписание после завершения
                    showAlert("Успех", "Прием успешно завершен.", "INFO");
                } else {
                    updateStatus("Ошибка завершения приема: " + response.statusCode(), "error");
                    showAlert("Ошибка", "Не удалось завершить прием.", "ERROR");
                }
            } catch (IOException | InterruptedException e) {
                updateStatus("Ошибка подключения к серверу", "error");
                showAlert("Ошибка", "Не удалось подключиться к серверу.", "ERROR");
                System.err.println("Ошибка при завершении приема: " + e.getMessage());
            }
        }
    }

    // --- Методы для вкладки "Сообщения" ---
    private void setupConversationList() {
        conversationListView.setItems(conversationsData);
        conversationListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation conversation, boolean empty) {
                super.updateItem(conversation, empty);
                if (empty || conversation == null) {
                    setText(null);
                } else {
                    setText(conversation.getParticipant() + " (Последнее: " + conversation.getLastMessageTime() + ")");
                }
            }
        });
    }

    private void loadConversations() {
        if (currentUser == null || !currentUser.getUserType().equals("DOCTOR")) {
            return;
        }

        updateStatus("Загрузка диалогов...", "info");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/doctor/" + currentUser.getUsername() + "/conversations"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Type conversationListType = new TypeToken<List<Conversation>>(){}.getType();
                List<Conversation> conversations = gson.fromJson(response.body(), conversationListType);
                conversationsData.clear();
                conversationsData.addAll(conversations);
                updateStatus("Диалоги загружены", "success");
            } else {
                updateStatus("Ошибка загрузки диалогов: " + response.statusCode(), "error");
            }
        } catch (IOException | InterruptedException e) {
            updateStatus("Ошибка подключения к серверу", "error");
            System.err.println("Ошибка при загрузке диалогов доктора: " + e.getMessage());
        }
    }

    private void loadConversationMessages(String doctorUsername, String patientUsername) {
        updateStatus("Загрузка сообщений...", "info");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/messages/conversation?doctor=" + doctorUsername + "&patient=" + patientUsername))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Type messageListType = new TypeToken<List<Message>>(){}.getType();
                List<Message> messages = gson.fromJson(response.body(), messageListType);
                messageHistoryArea.clear();
                for (Message msg : messages) {
                    messageHistoryArea.appendText(String.format("[%s] %s: %s\n",
                            msg.getTimestamp(),
                            msg.getSenderUsername(),
                            msg.getMessageText()));
                }
                updateStatus("Сообщения загружены", "success");
            } else {
                updateStatus("Ошибка загрузки сообщений: " + response.statusCode(), "error");
            }
        } catch (IOException | InterruptedException e) {
            updateStatus("Ошибка подключения к серверу", "error");
            System.err.println("Ошибка при загрузке сообщений диалога: " + e.getMessage());
        }
    }

    @FXML
    private void sendMessage() {
        if (currentUser == null || selectedConversation == null || messageInput.getText().trim().isEmpty()) {
            return;
        }

        String messageText = messageInput.getText().trim();
        Message newMessage = new Message(
                0, // ID будет присвоен сервером
                currentUser.getUsername(),
                selectedConversation.getParticipant(),
                messageText,
                null // Timestamp будет присвоен сервером
        );

        try {
            String json = gson.toJson(newMessage);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/messages/send"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                // Если успешно, очищаем поле ввода и перезагружаем сообщения
                messageInput.clear();
                loadConversationMessages(currentUser.getUsername(), selectedConversation.getParticipant());
                updateStatus("Сообщение отправлено", "success");
            } else {
                updateStatus("Ошибка отправки сообщения: " + response.statusCode(), "error");
                showAlert("Ошибка", "Не удалось отправить сообщение.", "ERROR");
            }
        } catch (IOException | InterruptedException e) {
            updateStatus("Ошибка подключения к серверу", "error");
            showAlert("Ошибка", "Не удалось подключиться к серверу.", "ERROR");
            System.err.println("Ошибка при отправке сообщения: " + e.getMessage());
        }
    }

    // --- Общие методы ---
    @FXML
    private void handleLogout() {
        try {
            Stage currentStage = (Stage) statusLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login_view.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("SuperMed - Вход в систему");
            loginStage.setScene(new Scene(root, 500, 700));
            loginStage.setMinWidth(450);
            loginStage.setMinHeight(600);
            loginStage.show();
            currentStage.close();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось выполнить выход", "ERROR");
        }
    }

    private void updateStatus(String message, String type) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(message);
                switch (type) {
                    case "success":
                        statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        break;
                    case "error":
                        statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        break;
                    case "info":
                        statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                        break;
                    default:
                        statusLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void showAlert(String title, String message, String alertType) {
        Platform.runLater(() -> {
            Alert.AlertType type;
            switch (alertType) {
                case "ERROR":
                    type = Alert.AlertType.ERROR;
                    break;
                case "WARNING":
                    type = Alert.AlertType.WARNING;
                    break;
                default:
                    type = Alert.AlertType.INFORMATION;
            }
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;");
            alert.showAndWait();
        });
    }
}