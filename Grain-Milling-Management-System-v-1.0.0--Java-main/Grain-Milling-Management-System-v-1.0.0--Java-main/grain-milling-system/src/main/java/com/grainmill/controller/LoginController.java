package com.grainmill.controller;

import com.grainmill.database.DatabaseConnection;
import com.grainmill.main.MainApp;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    
    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(
            "CUSTOMER", "OPERATOR", "ADMIN"
        ));
        roleComboBox.getSelectionModel().selectFirst();
        loginButton.defaultButtonProperty().bind(loginButton.focusedProperty());
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM users WHERE username = ? AND role = ? AND is_active = TRUE";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, role);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String dbPassword = rs.getString("password");
                
                if (dbPassword.equals(password)) {
                    String userRole = rs.getString("role");
                    int userId = rs.getInt("id");
                    String fullName = rs.getString("full_name");
                    
                    UserSession.getInstance().setUserId(userId);
                    UserSession.getInstance().setUsername(username);
                    UserSession.getInstance().setRole(userRole);
                    UserSession.getInstance().setFullName(fullName);
                    
                    redirectToDashboard(userRole);
                    
                } else {
                    showError("Invalid username or password");
                }
            } else {
                showError("Invalid username or password");
            }
            
            rs.close();
            pstmt.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Database error: " + e.getMessage());
        }
    }
    
    private void redirectToDashboard(String role) {
        try {
            Stage stage = MainApp.getPrimaryStage();
            Parent root;
            String title;
            
            switch (role) {
                case "CUSTOMER":
                    root = FXMLLoader.load(Objects.requireNonNull(
                        getClass().getResource("/fxml/CustomerDashboard.fxml")));
                    title = "Grain Milling System - Customer Dashboard";
                    break;
                case "OPERATOR":
                    root = FXMLLoader.load(Objects.requireNonNull(
                        getClass().getResource("/fxml/OperatorDashboard.fxml")));
                    title = "Grain Milling System - Operator Dashboard";
                    break;
                case "ADMIN":
                    root = FXMLLoader.load(Objects.requireNonNull(
                        getClass().getResource("/fxml/AdminDashboard.fxml")));
                    title = "Grain Milling System - Admin Dashboard";
                    break;
                default:
                    throw new IllegalArgumentException("Unknown role: " + role);
            }
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/css/style.css")).toExternalForm());
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setMaximized(true);
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading dashboard: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}