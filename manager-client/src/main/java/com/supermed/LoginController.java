package com.supermed;

import com.supermed.entities.*;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;
import java.util.ResourceBundle;

//  Класс для окна входа
public class LoginController implements Initializable {
    private static final String BASE_URL = "http://localhost:4567";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> userTypeCombo;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Заполняем ComboBox типами пользователей
        userTypeCombo.setItems(FXCollections.observableArrayList(
                "MANAGER", "DOCTOR", "PATIENT"
        ));
        userTypeCombo.setValue("MANAGER");

        // Очищаем статус при изменении полей
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> clearStatus());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearStatus());
    }

    //  Вход (логгирование)
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String userType = userTypeCombo.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Заполните все поля", "error");
            return;
        }

        performAuth("/auth/login", username, password, userType, "входа");
    }


    // Регистрация нового пользователя
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String userType = userTypeCombo.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Заполните все поля", "error");
            return;
        }

        if (password.length() < 6) {
            showStatus("Пароль должен содержать минимум 6 символов", "error");
            return;
        }

        performAuth("/auth/register", username, password, userType, "регистрации");
    }

    //  Авторизация и переход к главному окну (в данном случае к окну менеджера)
    private void performAuth(String endpoint, String username, String password, String userType, String actionType) {
        try {
            AuthRequest authRequest = new AuthRequest(username, password, userType);
            String json = gson.toJson(authRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            showStatus("Выполняется " + actionType + "...", "info");

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            AuthResponse authResponse = gson.fromJson(response.body(), AuthResponse.class);

            if (authResponse.isSuccess()) {
                showStatus(authResponse.getMessage(), "success");
                // Переходим к главному экрану
                openMainWindow(authResponse.getUser());
            } else {
                showStatus(authResponse.getMessage(), "error");
            }
        } catch (IOException | InterruptedException e) {
            showStatus("Ошибка подключения к серверу", "error");
        }
    }

    // Метод для перехода к окну менеджера
    private void openMainWindow(User user) {
        try {
            // Закрываем текущее окно
            Stage currentStage = (Stage) usernameField.getScene().getWindow();

            // Загружаем главное окно
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_view.fxml"));
            Parent root = loader.load();

            // Передаем данные пользователя в главный контроллер
            ManagerController mainController = loader.getController();
            mainController.setCurrentUser(user);

            Stage mainStage = new Stage();
            mainStage.setTitle("SuperMed - " + getUserTypeDisplayName(user.getUserType()));
            mainStage.setScene(new Scene(root, 1000, 700));
            mainStage.setMinWidth(900);
            mainStage.setMinHeight(600);
            mainStage.show();

            // Закрываем окно входа
            currentStage.close();

        } catch (IOException e) {
            showStatus("Ошибка загрузки главного окна", "error");
        }
    }

    private String getUserTypeDisplayName(String userType) {
        switch (userType) {
            case "MANAGER": return "Панель менеджера";
            case "DOCTOR": return "Панель врача";
            case "PATIENT": return "Панель пациента";
            default: return "Панель управления";
        }
    }

    private void showStatus(String message, String type) {
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

    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.setStyle("");
    }
}