package com.supermed;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.supermed.entities.Appointment;
import com.supermed.entities.Conversation;
import com.supermed.entities.Message;
import com.supermed.entities.Schedule;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DoctorController implements Initializable {

    private static final String BASE_URL = "http://localhost:4567";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML private Label userInfoLabel;
    @FXML private Label statusLabel;

    // Вкладка "Мое расписание"
    @FXML private Tab scheduleTab;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> viewTypeComboBox;
    @FXML private TableView<Appointment> scheduleTableView;
    @FXML private TableColumn<Appointment, String> scheduleDateCol;
    @FXML private TableColumn<Appointment, String> scheduleTimeCol;
    @FXML private TableColumn<Appointment, String> schedulePatientCol;
    @FXML private TableColumn<Appointment, String> scheduleTypeCol;
    @FXML private TableColumn<Appointment, String> scheduleStatusCol;
    @FXML private TableColumn<Appointment, Void> scheduleActionsCol;
    @FXML private Label scheduleStatsLabel;
    @FXML private Button todayButton;
    @FXML private Button weekButton;
    @FXML private Button refreshScheduleButton;

    // Вкладка "Рабочее расписание" (НОВАЯ - вместо "Общее расписание")
    @FXML private TableView<Schedule> workScheduleTable;
    @FXML private TableColumn<Schedule, String> colDayOfWeek;
    @FXML private TableColumn<Schedule, String> colWorkHours;
    @FXML private TableColumn<Schedule, String> colBranch;
    @FXML private Label workScheduleLabel;
    @FXML private Button refreshWorkScheduleButton;

    // Элементы для чата
    @FXML private ListView<Conversation> conversationListView;
    @FXML private Label currentConversationLabel;
    @FXML private TextArea messageHistoryArea;
    @FXML private TextField messageInput;

    private User currentUser;
    private ObservableList<Appointment> doctorAppointmentsData = FXCollections.observableArrayList();
    private ObservableList<Schedule> doctorWorkScheduleData = FXCollections.observableArrayList();
    private ObservableList<Conversation> conversationsData = FXCollections.observableArrayList();
    private Conversation selectedConversation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupScheduleTab();
        setupWorkScheduleTable();
        setupConversationList();
        updateStatus("Готов к работе", "info");

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

        messageInput.disableProperty().bind(conversationListView.getSelectionModel().selectedItemProperty().isNull());
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (userInfoLabel != null && user != null) {
            userInfoLabel.setText("Пользователь: " + user.getUsername() + " (" + getUserTypeDisplayName(user.getUserType()) + ")");
            refreshSchedule();
            loadWorkSchedule();
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

    // --- Методы для вкладки "Мое расписание" ---
    private void setupScheduleTab() {
        datePicker.setValue(LocalDate.now());
        viewTypeComboBox.getItems().addAll("День", "Неделя", "Месяц", "Все");
        viewTypeComboBox.setValue("День");

        scheduleDateCol.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        scheduleTimeCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getStartTime() + " - " + cellData.getValue().getEndTime()
                ));
        schedulePatientCol.setCellValueFactory(new PropertyValueFactory<>("patientUsername"));
        scheduleTypeCol.setCellValueFactory(cellData -> {
            Appointment app = cellData.getValue();
            String type = "Консультация";
            if (app.getSecretId() != null && app.getSecretId().startsWith("SURG")) {
                type = "Операция";
            } else if (app.getSecretId() != null && app.getSecretId().startsWith("CHK")) {
                type = "Осмотр";
            }
            return new javafx.beans.property.SimpleStringProperty(type);
        });
        scheduleStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        scheduleActionsCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Appointment, Void> call(final TableColumn<Appointment, Void> param) {
                return new TableCell<>() {
                    private final Button completeButton = new Button("Завершить");
                    private final Label statusLabel = new Label();

                    {
                        completeButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
                        completeButton.setOnAction(event -> {
                            Appointment appointment = getTableView().getItems().get(getIndex());
                            completeAppointment(appointment);
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
                                completeButton.setText("Завершить");
                                completeButton.setDisable(false);
                                setGraphic(completeButton);
                            } else if ("completed".equals(appointment.getStatus())) {
                                statusLabel.setText("✓ Завершено");
                                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                                setGraphic(statusLabel);
                            } else if ("cancelled".equals(appointment.getStatus())) {
                                statusLabel.setText("✗ Отменено");
                                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                                setGraphic(statusLabel);
                            } else {
                                statusLabel.setText(appointment.getStatus());
                                setGraphic(statusLabel);
                            }
                        }
                    }
                };
            }
        });

        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> filterSchedule());
        viewTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterSchedule());

        todayButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        weekButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshScheduleButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    // --- Методы для вкладки "Рабочее расписание" (НОВЫЕ) ---
    private void setupWorkScheduleTable() {
        colDayOfWeek.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
        colWorkHours.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getWorkHours()
                ));
        colBranch.setCellValueFactory(new PropertyValueFactory<>("branchName"));

        // Добавим цветовое кодирование для дней недели
        colDayOfWeek.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String day, boolean empty) {
                super.updateItem(day, empty);
                if (empty || day == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(day);
                    // Разные цвета для разных дней
                    switch (day.toLowerCase()) {
                        case "понедельник":
                            setStyle("-fx-background-color: #e8f4f8; -fx-font-weight: bold;");
                            break;
                        case "вторник":
                            setStyle("-fx-background-color: #f0f8e8; -fx-font-weight: bold;");
                            break;
                        case "среда":
                            setStyle("-fx-background-color: #f8f0e8; -fx-font-weight: bold;");
                            break;
                        case "четверг":
                            setStyle("-fx-background-color: #f8e8f0; -fx-font-weight: bold;");
                            break;
                        case "пятница":
                            setStyle("-fx-background-color: #e8f0f8; -fx-font-weight: bold;");
                            break;
                        case "суббота":
                            setStyle("-fx-background-color: #f8f8e8; -fx-font-weight: bold; -fx-text-fill: #d35400;");
                            break;
                        case "воскресенье":
                            setStyle("-fx-background-color: #f8e8e8; -fx-font-weight: bold; -fx-text-fill: #c0392b;");
                            break;
                        default:
                            setStyle("-fx-font-weight: bold;");
                    }
                }
            }
        });

        workScheduleTable.setItems(doctorWorkScheduleData);
        refreshWorkScheduleButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    @FXML
    private void loadWorkSchedule() {    // <- ЗДЕСЬ МЕТОД
        if (currentUser == null || !currentUser.getUserType().equals("DOCTOR")) {
            return;
        }

        updateStatus("Загрузка рабочего расписания...", "info");
        try {
            // Используем endpoint для рабочего расписания
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/doctor/" + currentUser.getUsername() + "/work-schedule"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Type scheduleListType = new TypeToken<List<Schedule>>(){}.getType();
                List<Schedule> schedules = gson.fromJson(response.body(), scheduleListType);

                doctorWorkScheduleData.clear();
                doctorWorkScheduleData.addAll(schedules);

                workScheduleLabel.setText("Рабочие дни: " + schedules.size() + " дней в неделю");
                updateStatus("Рабочее расписание загружено", "success");

            } else if (response.statusCode() == 404) {
                updateStatus("Рабочее расписание не найдено", "warning");
                showAlert("Информация", "Ваше рабочее расписание еще не настроено менеджером.", "INFO");
            } else {
                updateStatus("Ошибка загрузки расписания", "error");
            }
        } catch (IOException | InterruptedException e) {
            updateStatus("Ошибка подключения к серверу", "error");
            System.err.println("Ошибка при загрузке рабочего расписания: " + e.getMessage());
        }
    }

    private int getDayIndex(String day, String[] daysOrder) {
        for (int i = 0; i < daysOrder.length; i++) {
            if (daysOrder[i].equalsIgnoreCase(day)) {
                return i;
            }
        }
        return daysOrder.length; // Если день не найден, ставим в конец
    }

    @FXML
    private void refreshWorkSchedule() {
        loadWorkSchedule();
    }

    // --- Методы для фильтрации записей ---
    @FXML
    private void handleShowToday() {
        datePicker.setValue(LocalDate.now());
        viewTypeComboBox.setValue("День");
    }

    @FXML
    private void handleShowWeek() {
        datePicker.setValue(LocalDate.now());
        viewTypeComboBox.setValue("Неделя");
    }

    @FXML
    private void handleRefreshScheduleTab() {
        refreshSchedule();
        filterSchedule();
    }

    private void filterSchedule() {
        if (viewTypeComboBox.getValue() == null || datePicker.getValue() == null) {
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        String viewType = viewTypeComboBox.getValue();

        ObservableList<Appointment> filteredData = FXCollections.observableArrayList();

        for (Appointment appointment : doctorAppointmentsData) {
            try {
                LocalDate appointmentDate = LocalDate.parse(appointment.getAppointmentDate());

                switch (viewType) {
                    case "День":
                        if (appointmentDate.equals(selectedDate)) {
                            filteredData.add(appointment);
                        }
                        break;
                    case "Неделя":
                        LocalDate weekStart = selectedDate.minusDays(selectedDate.getDayOfWeek().getValue() - 1);
                        LocalDate weekEnd = weekStart.plusDays(6);
                        if (!appointmentDate.isBefore(weekStart) && !appointmentDate.isAfter(weekEnd)) {
                            filteredData.add(appointment);
                        }
                        break;
                    case "Месяц":
                        LocalDate monthStart = selectedDate.withDayOfMonth(1);
                        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                        if (!appointmentDate.isBefore(monthStart) && !appointmentDate.isAfter(monthEnd)) {
                            filteredData.add(appointment);
                        }
                        break;
                    case "Все":
                        if (!appointmentDate.isBefore(LocalDate.now())) {
                            filteredData.add(appointment);
                        }
                        break;
                }
            } catch (Exception e) {
                // Пропускаем
            }
        }

        filteredData.sort((a1, a2) -> {
            int dateCompare = a1.getAppointmentDate().compareTo(a2.getAppointmentDate());
            if (dateCompare != 0) return dateCompare;
            return a1.getStartTime().compareTo(a2.getStartTime());
        });

        scheduleTableView.setItems(filteredData);
        updateScheduleStats(filteredData);
    }

    private void updateScheduleStats(ObservableList<Appointment> appointments) {
        long scheduled = appointments.stream()
                .filter(a -> "scheduled".equals(a.getStatus()))
                .count();
        long completed = appointments.stream()
                .filter(a -> "completed".equals(a.getStatus()))
                .count();
        long cancelled = appointments.stream()
                .filter(a -> "cancelled".equals(a.getStatus()))
                .count();

        scheduleStatsLabel.setText(String.format(
                "Всего: %d | Запланировано: %d | Завершено: %d | Отменено: %d",
                appointments.size(), scheduled, completed, cancelled
        ));
    }

    // --- Методы для загрузки записей ---
    @FXML
    private void refreshSchedule() {
        if (currentUser == null || !currentUser.getUserType().equals("DOCTOR")) {
            showAlert("Ошибка доступа", "Только врачи могут просматривать свое расписание.", "ERROR");
            return;
        }

        updateStatus("Загрузка записей...", "info");
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

                updateStatus("Записи загружены успешно", "success");
                filterSchedule();

            } else {
                updateStatus("Ошибка загрузки записей: " + response.statusCode(), "error");
                showAlert("Ошибка", "Не удалось загрузить записи.", "ERROR");
            }
        } catch (IOException | InterruptedException e) {
            updateStatus("Ошибка подключения к серверу", "error");
            showAlert("Ошибка", "Не удалось подключиться к серверу.", "ERROR");
            System.err.println("Ошибка при загрузке записей: " + e.getMessage());
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
                    refreshSchedule();
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

    // --- Методы для чата ---
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

    @FXML
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
                0,
                currentUser.getUsername(),
                selectedConversation.getParticipant(),
                messageText,
                null
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