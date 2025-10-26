package com.supermed;

import com.supermed.entities.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ManagerController implements Initializable {

    private static final String BASE_URL = "http://localhost:4567";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    // Элементы интерфейса
    @FXML private TableView<Statistics> statisticsTable;
    @FXML private TableColumn<Statistics, String> colDoctorName;
    @FXML private TableColumn<Statistics, String> colSpecialization;
    @FXML private TableColumn<Statistics, String> colBranch;
    @FXML private TableColumn<Statistics, Integer> colAppointmentCount;
    @FXML private Label statsCount;
    @FXML private Button loadStatsBtn;

    @FXML private TableView<Schedule> scheduleTable;
    @FXML private TableColumn<Schedule, Integer> colScheduleId;
    @FXML private TableColumn<Schedule, String> colScheduleDoctor;
    @FXML private TableColumn<Schedule, String> colScheduleDay;
    @FXML private TableColumn<Schedule, String> colScheduleStart;
    @FXML private TableColumn<Schedule, String> colScheduleEnd;
    @FXML private TableColumn<Schedule, Integer> colScheduleHours;
    @FXML private TableColumn<Schedule, String> colScheduleActions;
    @FXML private Label scheduleCount;
    @FXML private Button refreshScheduleBtn;
    @FXML private Button addScheduleBtn;

    @FXML private Label statusLabel;
    @FXML private Label userInfoLabel;

    // Данные для таблиц
    private ObservableList<Statistics> statisticsData = FXCollections.observableArrayList();
    private ObservableList<Schedule> scheduleData = FXCollections.observableArrayList();
    private ObservableList<Doctor> doctorsData = FXCollections.observableArrayList();

    // Текущий пользователь
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupStatisticsTable();
        setupScheduleTable();
        updateStatus("Готов к работе", "success");
        loadDoctors();
        refreshSchedules();

        // Показываем информацию о пользователе
        if (currentUser != null) {
            userInfoLabel.setText("Пользователь: " + currentUser.getUsername() + " (" + getUserTypeDisplayName(currentUser.getUserType()) + ")");
        }
    }

    // Метод для установки текущего пользователя (вызывается из LoginController)
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (userInfoLabel != null && user != null) {
            userInfoLabel.setText("Пользователь: " + user.getUsername() + " (" + getUserTypeDisplayName(user.getUserType()) + ")");
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

    private void setupStatisticsTable() {
        colDoctorName.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colSpecialization.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colBranch.setCellValueFactory(new PropertyValueFactory<>("branch"));
        colAppointmentCount.setCellValueFactory(new PropertyValueFactory<>("appointmentCount"));

        // Подсветка ячеек с количеством записей
        colAppointmentCount.setCellFactory(column -> new TableCell<Statistics, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item > 15) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (item > 8) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    }
                }
            }
        });

        statisticsTable.setItems(statisticsData);
    }

    private void setupScheduleTable() {
        colScheduleId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colScheduleDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colScheduleDay.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
        colScheduleStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colScheduleEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colScheduleHours.setCellValueFactory(new PropertyValueFactory<>("workingHours"));

        // Подсветка рабочих часов
        colScheduleHours.setCellFactory(column -> new TableCell<Schedule, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item > 8) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-background-color: #ffcccc;");
                    } else if (item == 8) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Колонка действий
        colScheduleActions.setCellFactory(column -> new TableCell<Schedule, String>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 6;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 6;");

                editBtn.setOnAction(event -> {
                    Schedule schedule = getTableView().getItems().get(getIndex());
                    showEditScheduleDialog(schedule);
                });

                deleteBtn.setOnAction(event -> {
                    Schedule schedule = getTableView().getItems().get(getIndex());
                    deleteSchedule(schedule);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });

        scheduleTable.setItems(scheduleData);
    }

    private void loadDoctors() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/doctors"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Type doctorListType = new TypeToken<List<Doctor>>(){}.getType();
                List<Doctor> doctors = gson.fromJson(response.body(), doctorListType);
                doctorsData.clear();
                doctorsData.addAll(doctors);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка при загрузке списка врачей: " + e.getMessage());
        }
    }

    @FXML
    private void loadStatistics() {
        // Проверяем права доступа
        if (currentUser != null && !currentUser.getUserType().equals("MANAGER")) {
            showAlert("Ошибка доступа", "Только менеджеры могут просматривать статистику", "ERROR");
            return;
        }

        updateStatus("Загрузка статистики...", "info");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/statistics"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Type statisticsListType = new TypeToken<List<Statistics>>(){}.getType();
                List<Statistics> statistics = gson.fromJson(response.body(), statisticsListType);

                statisticsData.clear();
                statisticsData.addAll(statistics);

                statsCount.setText("Врачей в статистике: " + statistics.size());
                updateStatus("Загружена статистика по " + statistics.size() + " врачам", "success");

                showAlert("Успех", "Загружена статистика по " + statistics.size() + " врачам", "INFO");
            } else {
                updateStatus("Ошибка загрузки: " + response.statusCode(), "error");
                showAlert("Ошибка", "Не удалось загрузить статистику", "ERROR");
            }
        } catch (IOException | InterruptedException e) {
            updateStatus("Ошибка подключения", "error");
            showAlert("Ошибка", "Не удалось подключиться к серверу", "ERROR");
        }
    }

    @FXML
    private void refreshSchedules() {
        updateStatus("Загрузка расписания...", "info");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/schedules"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Type scheduleListType = new TypeToken<List<Schedule>>(){}.getType();
                List<Schedule> schedules = gson.fromJson(response.body(), scheduleListType);

                scheduleData.clear();
                scheduleData.addAll(schedules);

                scheduleCount.setText("Записей в расписании: " + schedules.size());
                updateStatus("Загружено " + schedules.size() + " записей расписания", "success");
            } else {
                updateStatus("Ошибка загрузки: " + response.statusCode(), "error");
            }
        } catch (IOException | InterruptedException e) {
            updateStatus("Ошибка подключения", "error");
        }
    }

    @FXML
    private void showAddScheduleDialog() {
        // Проверяем права доступа
        if (currentUser != null && !currentUser.getUserType().equals("MANAGER")) {
            showAlert("Ошибка доступа", "Только менеджеры могут управлять расписанием", "ERROR");
            return;
        }

        Dialog<Schedule> dialog = new Dialog<>();
        dialog.setTitle("Добавление расписания");
        dialog.setHeaderText("Добавьте новое расписание врача");

        // Создаем форму
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Doctor> doctorCombo = new ComboBox<>(doctorsData);
        doctorCombo.setPromptText("Выберите врача");
        doctorCombo.setCellFactory(param -> new ListCell<Doctor>() {
            @Override
            protected void updateItem(Doctor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - " + item.getSpecialization() + " (" + item.getBranch() + ")");
                }
            }
        });

        doctorCombo.setButtonCell(new ListCell<Doctor>() {
            @Override
            protected void updateItem(Doctor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - " + item.getSpecialization() + " (" + item.getBranch() + ")");
                }
            }
        });

        ComboBox<String> dayCombo = new ComboBox<>();
        dayCombo.getItems().addAll("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
        dayCombo.setPromptText("День недели");

        TextField startTimeField = new TextField();
        startTimeField.setPromptText("09:00");

        TextField endTimeField = new TextField();
        endTimeField.setPromptText("17:00");

        grid.add(new Label("Врач:"), 0, 0);
        grid.add(doctorCombo, 1, 0);
        grid.add(new Label("День недели:"), 0, 1);
        grid.add(dayCombo, 1, 1);
        grid.add(new Label("Начало работы:"), 0, 2);
        grid.add(startTimeField, 1, 2);
        grid.add(new Label("Конец работы:"), 0, 3);
        grid.add(endTimeField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Кнопки
        ButtonType addButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Результат
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (doctorCombo.getValue() != null && dayCombo.getValue() != null &&
                        !startTimeField.getText().isEmpty() && !endTimeField.getText().isEmpty()) {

                    Schedule schedule = new Schedule();
                    schedule.setDoctorId(doctorCombo.getValue().getId());
                    schedule.setDayOfWeek(dayCombo.getValue());
                    schedule.setStartTime(startTimeField.getText());
                    schedule.setEndTime(endTimeField.getText());

                    return schedule;
                }
            }
            return null;
        });

        Optional<Schedule> result = dialog.showAndWait();
        result.ifPresent(this::addSchedule);
    }

    private void showEditScheduleDialog(Schedule schedule) {
        // Проверяем права доступа
        if (currentUser != null && !currentUser.getUserType().equals("MANAGER")) {
            showAlert("Ошибка доступа", "Только менеджеры могут управлять расписанием", "ERROR");
            return;
        }

        Dialog<Schedule> dialog = new Dialog<>();
        dialog.setTitle("Редактирование расписания");
        dialog.setHeaderText("Редактируйте расписание врача");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Label doctorLabel = new Label(schedule.getDoctorName());
        Label dayLabel = new Label(schedule.getDayOfWeek());

        TextField startTimeField = new TextField(schedule.getStartTime());
        TextField endTimeField = new TextField(schedule.getEndTime());

        grid.add(new Label("Врач:"), 0, 0);
        grid.add(doctorLabel, 1, 0);
        grid.add(new Label("День недели:"), 0, 1);
        grid.add(dayLabel, 1, 1);
        grid.add(new Label("Начало работы:"), 0, 2);
        grid.add(startTimeField, 1, 2);
        grid.add(new Label("Конец работы:"), 0, 3);
        grid.add(endTimeField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                schedule.setStartTime(startTimeField.getText());
                schedule.setEndTime(endTimeField.getText());
                return schedule;
            }
            return null;
        });

        Optional<Schedule> result = dialog.showAndWait();
        result.ifPresent(this::updateSchedule);
    }

    private void addSchedule(Schedule schedule) {
        try {
            String json = gson.toJson(schedule);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/schedules"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                updateStatus("Расписание добавлено успешно", "success");
                refreshSchedules(); // Обновляем таблицу
                showAlert("Успех", "Расписание успешно добавлено", "INFO");
            } else {
                String errorMessage = "Не удалось добавить расписание";
                if (response.body().contains("8 часов")) {
                    errorMessage = "Рабочий день не может превышать 8 часов!";
                }
                updateStatus("Ошибка добавления", "error");
                showAlert("Ошибка", errorMessage, "ERROR");
            }
        } catch (IOException | InterruptedException e) {
            updateStatus("Ошибка подключения", "error");
            showAlert("Ошибка", "Не удалось подключиться к серверу", "ERROR");
        }
    }

    private void updateSchedule(Schedule schedule) {
        try {
            String json = gson.toJson(schedule);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/schedules/" + schedule.getId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                updateStatus("Расписание обновлено успешно", "success");
                refreshSchedules(); // Обновляем таблицу
                showAlert("Успех", "Расписание успешно обновлено", "INFO");
            } else {
                String errorMessage = "Не удалось обновить расписание";
                if (response.body().contains("8 часов")) {
                    errorMessage = "Рабочий день не может превышать 8 часов!";
                }
                updateStatus("Ошибка обновления", "error");
                showAlert("Ошибка", errorMessage, "ERROR");
            }
        } catch (IOException | InterruptedException e) {
            updateStatus("Ошибка подключения", "error");
            showAlert("Ошибка", "Не удалось подключиться к серверу", "ERROR");
        }
    }

    private void deleteSchedule(Schedule schedule) {
        // Проверяем права доступа
        if (currentUser != null && !currentUser.getUserType().equals("MANAGER")) {
            showAlert("Ошибка доступа", "Только менеджеры могут управлять расписанием", "ERROR");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Подтверждение удаления");
        confirmation.setHeaderText("Удаление расписания");
        confirmation.setContentText("Вы уверены, что хотите удалить расписание для врача " +
                schedule.getDoctorName() + " на " + schedule.getDayOfWeek() + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/schedules/" + schedule.getId()))
                        .DELETE()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    updateStatus("Расписание удалено успешно", "success");
                    refreshSchedules(); // Обновляем таблицу
                    showAlert("Успех", "Расписание успешно удалено", "INFO");
                } else {
                    updateStatus("Ошибка удаления", "error");
                    showAlert("Ошибка", "Не удалось удалить расписание", "ERROR");
                }
            } catch (IOException | InterruptedException e) {
                updateStatus("Ошибка подключения", "error");
                showAlert("Ошибка", "Не удалось подключиться к серверу", "ERROR");
            }
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Закрываем текущее окно
            Stage currentStage = (Stage) statusLabel.getScene().getWindow();

            // Открываем окно входа
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login_view.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("SuperMed - Вход в систему");
            loginStage.setScene(new Scene(root, 500, 700));
            loginStage.setMinWidth(450);
            loginStage.setMinHeight(600);
            loginStage.show();

            // Закрываем главное окно
            currentStage.close();

        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось выполнить выход", "ERROR");
        }
    }

    private void updateStatus(String message, String type) {
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
    }

    private void showAlert(String title, String message, String alertType) {
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
        dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 10; -fx-background-radius: 10;");

        alert.showAndWait();
    }
}