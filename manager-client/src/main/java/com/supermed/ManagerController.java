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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

public class ManagerController implements Initializable {

    private static final String BASE_URL = "http://localhost:4567";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    // Элементы интерфейса

    // Элементы для вкладки статистика
    @FXML private TableView<Statistic> statisticsTable;
    @FXML private TableColumn<Appointment, String> colAppointmentDate;
    @FXML private TableColumn<Appointment, String> colStartTime;
    @FXML private TableColumn<Appointment, String> colEndTime;
    @FXML private TableColumn<Statistic, String> colDoctorName;
    @FXML private TableColumn<Statistic, String> colBranchName;
    @FXML private TableColumn<Statistic, String> colDoctorSpecialization;
    @FXML private TableColumn<Statistic, String> colDoctorSchedule;
    @FXML private ComboBox<Doctor> doctorFilterComboBox;
    @FXML private ComboBox<Branch> branchFilterComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label statsCount;
    @FXML private Button loadStatsBtn;

    // Элемнты для вкладки расписание врачей
    @FXML private TableView<Schedule> scheduleTable;
    @FXML private TableColumn<Schedule, String> colScheduleDoctor;
    @FXML private TableColumn<Schedule, String> colScheduleDay;
    @FXML private TableColumn<Schedule, String> colScheduleStart;
    @FXML private TableColumn<Schedule, String> colScheduleEnd;
    @FXML private TableColumn<Schedule, Integer> colScheduleHours;
    @FXML private TableColumn<Schedule, String> colScheduleActions;
    @FXML private Label scheduleCount;
    @FXML private Button addScheduleBtn;

    // Используется для демонстрации информации о текущем пользователе
    @FXML private Label statusLabel;
    @FXML private Label userInfoLabel;


    // Данные для таблиц
    private ObservableList<Statistic> allAppointmentsData = FXCollections.observableArrayList();
    private ObservableList<Doctor> doctorsFilterData = FXCollections.observableArrayList();
    private ObservableList<Branch> branchesFilterData = FXCollections.observableArrayList();
    private ObservableList<Schedule> scheduleData = FXCollections.observableArrayList();
    private ObservableList<Doctor> doctorsData = FXCollections.observableArrayList();

    // Текущий пользователь
    private User currentUser;

@Override
public void initialize(URL location, ResourceBundle resources) {
    setupStatisticsTable();
    setupScheduleTable();
    updateStatus("Готов к работе", "success");
    refreshSchedules();
    loadDoctors();
    // Загружаем данные для ComboBox'ов фильтрации
    loadDoctorsForFilter();
    loadBranchesForFilter();
    // Настройка ComboBox'ов для отображения объектов
    doctorFilterComboBox.setConverter(new StringConverter<Doctor>() {
        @Override
        public String toString(Doctor doctor) {
            return doctor != null ? doctor.getName() + " (" + doctor.getSpecialization() + ")" : "";
        }
        @Override
        public Doctor fromString(String string) {
            return null; // Не используется для этого ComboBox
        }
    });
    doctorFilterComboBox.setItems(doctorsFilterData);
    // Добавляем опцию "Все врачи"
    doctorsFilterData.add(0, null); // null будет представлять "Все врачи"
    branchFilterComboBox.setConverter(new StringConverter<Branch>() {
        @Override
        public String toString(Branch branch) {
            return branch != null ? branch.getName() + " (" + branch.getAddress() + ")" : "";
        }
        @Override
        public Branch fromString(String string) {
            return null; // Не используется
        }
    });
    branchFilterComboBox.setItems(branchesFilterData);
    // Добавляем опцию "Все филиалы"
    branchesFilterData.add(0, null); // null будет представлять "Все филиалы"
    // Добавляем слушателей для полей фильтрации, чтобы обновлять статистику
    doctorFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadStatistics());
    branchFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadStatistics());
    startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> loadStatistics());
    endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> loadStatistics());
    // Загружаем статистику при инициализации
    loadStatistics();
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

    //  Загрузка таблицы статистики (записей)
    private void setupStatisticsTable() {
        colAppointmentDate.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        colDoctorName.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDoctorSpecialization.setCellValueFactory(new PropertyValueFactory<>("doctorSpecialization"));
        colBranchName.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        colDoctorSchedule.setCellValueFactory(new PropertyValueFactory<>("formattedDoctorSchedule"));
        colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        statisticsTable.setItems(allAppointmentsData);
    }

    //  Загрузка всех врачей для фильтра по врачам
    private void loadDoctorsForFilter() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/doctors"))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Type doctorListType = new TypeToken<List<Doctor>>(){}.getType();
                List<Doctor> doctors = gson.fromJson(response.body(), doctorListType);
                doctorsFilterData.clear();
                doctorsFilterData.addAll(doctors);
                // Добавляем "Все врачи" после загрузки
                doctorsFilterData.add(0, null);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка при загрузке списка врачей для фильтрации: " + e.getMessage());
        }
    }
    // Загрузка всех филиалов для фильтра по филиалам
    private void loadBranchesForFilter() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/branches"))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Type branchListType = new TypeToken<List<Branch>>(){}.getType();
                List<Branch> branches = gson.fromJson(response.body(), branchListType);
                branchesFilterData.clear();
                branchesFilterData.addAll(branches);
                // Добавляем "Все филиалы" после загрузки
                branchesFilterData.add(0, null);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка при загрузке списка филиалов для фильтрации: " + e.getMessage());
        }
    }

    //  Метод по загрузке вкладки с расписанием врачей
    private void setupScheduleTable() {
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
                    setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
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

    // Метод для загрузки списка врачей при добавлении расписания
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

    // Добавляем методы для работы с филиалами
    private ObservableList<Branch> branchesData = FXCollections.observableArrayList();

    //  Метод для загрузки статистики
    @FXML
    private void loadStatistics() {
        if (currentUser != null && !currentUser.getUserType().equals("MANAGER")) {
            showAlert("Ошибка доступа", "Только менеджеры могут просматривать детальный отчет по приемам", "ERROR");
            return;
        }
        updateStatus("Загрузка детального отчета по приемам...", "info");
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/statistics?");
            // Параметр для врача
            Doctor selectedDoctor = doctorFilterComboBox.getSelectionModel().getSelectedItem();
            if (selectedDoctor != null) {
                urlBuilder.append("doctorId=").append(selectedDoctor.getId()).append("&");
            }
            // Параметр для филиала
            Branch selectedBranch = branchFilterComboBox.getSelectionModel().getSelectedItem();
            if (selectedBranch != null) {
                urlBuilder.append("branchId=").append(selectedBranch.getId()).append("&");
            }
            // Добавляем параметры диапазона дат
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (startDatePicker.getValue() != null) {
                urlBuilder.append("startDate=").append(startDatePicker.getValue().format(dateFormatter)).append("&");
            }
            if (endDatePicker.getValue() != null) {
                urlBuilder.append("endDate=").append(endDatePicker.getValue().format(dateFormatter)).append("&");
            }
            String finalUrl = urlBuilder.toString();
            if (finalUrl.endsWith("&")) {
                finalUrl = finalUrl.substring(0, finalUrl.length() - 1);
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(finalUrl))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Type detailedAppointmentListType = new TypeToken<List<Statistic>>(){}.getType();
                List<Statistic> statistics = gson.fromJson(response.body(), detailedAppointmentListType);
                allAppointmentsData.clear();
                allAppointmentsData.addAll(statistics);
                statsCount.setText("Всего записей: " + statistics.size());
                updateStatus("Загружен детальный отчет по " + statistics.size() + " записям", "success");
            } else {
                updateStatus("Ошибка загрузки: " + response.statusCode(), "error");
                showAlert("Ошибка", "Не удалось загрузить детальный отчет по приемам", "ERROR");
            }
        } catch (IOException | InterruptedException e) {
            updateStatus("Ошибка подключения", "error");
            showAlert("Ошибка", "Не удалось подключиться к серверу", "ERROR");
            System.err.println("Ошибка при загрузке детального отчета по приемам: " + e.getMessage());
        }
    }

    //  Метод для обновления расписания врачей
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

    // Метод для показа окна добавления расписания врача
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
                    setText(item.getName() + " - " + item.getSpecialization() + " (" + item.getBranchName() + ")");
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
                    setText(item.getName() + " - " + item.getSpecialization() + " (" + item.getBranchName() + ")");
                }
            }
        });

        ComboBox<String> dayCombo = new ComboBox<>();
        dayCombo.getItems().addAll("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье");
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
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, cancelButtonType);

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

    //  Метод для показа окна редактирования расписания врача
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
        Label branchLabel = new Label(schedule.getBranchName());

        ComboBox<String> dayCombo = new ComboBox<>();
        dayCombo.getItems().addAll("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье");
        dayCombo.setValue(schedule.getDayOfWeek());

        TextField startTimeField = new TextField(schedule.getStartTime());
        TextField endTimeField = new TextField(schedule.getEndTime());

        grid.add(new Label("Врач:"), 0, 0);
        grid.add(doctorLabel, 1, 0);
        grid.add(new Label("Филиал:"), 0, 1);
        grid.add(branchLabel, 1, 1);
        grid.add(new Label("День недели:"), 0, 2);
        grid.add(dayCombo, 1, 2);
        grid.add(new Label("Начало работы:"), 0, 3);
        grid.add(startTimeField, 1, 3);
        grid.add(new Label("Конец работы:"), 0, 4);
        grid.add(endTimeField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                schedule.setDayOfWeek(dayCombo.getValue());
                schedule.setStartTime(startTimeField.getText());
                schedule.setEndTime(endTimeField.getText());
                return schedule;
            }
            return null;
        });

        Optional<Schedule> result = dialog.showAndWait();
        result.ifPresent(this::updateSchedule);
    }

    //  Метод добавления расписания
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
                updateStatus("Ошибка добавления", "error");
                showAlert("Ошибка", errorMessage, "ERROR");
            }
        } catch (IOException | InterruptedException e) {
            updateStatus("Ошибка подключения", "error");
            showAlert("Ошибка", "Не удалось подключиться к серверу", "ERROR");
        }
    }

    // Метод редактирования расписания
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
                updateStatus("Ошибка обновления", "error");
                showAlert("Ошибка", errorMessage, "ERROR");
            }
        } catch (IOException | InterruptedException e) {
            updateStatus("Ошибка подключения", "error");
            showAlert("Ошибка", "Не удалось подключиться к серверу", "ERROR");
        }
    }

    //  Метод удаления расписания
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

    //  Метод выхода из главного окна и переход на окно входа (логгирования)
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