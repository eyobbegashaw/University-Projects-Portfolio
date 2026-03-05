package com.grainmill.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class AdminDashboardController {
    
    @FXML private Label welcomeLabel;
    @FXML private StackPane contentArea;
    
    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        welcomeLabel.setText("Welcome, " + session.getFullName());
        showDashboardOverview();
    }
    
    @FXML
    private void showDashboardOverview() {
        loadContent("/fxml/admin/DashboardOverview.fxml");
    }
    
    @FXML
    private void showFinancialReports() {
        loadContent("/fxml/admin/FinancialReports.fxml");
    }
    
    @FXML
    private void showPriceManagement() {
        loadContent("/fxml/admin/PriceManagement.fxml");
    }
    
    @FXML
    private void showAssetManagement() {
        loadContent("/fxml/admin/AssetManagement.fxml");
    }
    
    @FXML
    private void showUserManagement() {
        loadContent("/fxml/admin/UserManagement.fxml");
    }
    
    @FXML
    private void showSystemAnalytics() {
        loadContent("/fxml/admin/SystemAnalytics.fxml");
    }
    
    @FXML
    private void handleLogout() {
        UserSession.getInstance().cleanUserSession();
        MainApp.showLoginScreen();
    }
    
    private void loadContent(String fxmlPath) {
        try {
            Parent content = FXMLLoader.load(Objects.requireNonNull(
                getClass().getResource(fxmlPath)));
            contentArea.getChildren().setAll(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}