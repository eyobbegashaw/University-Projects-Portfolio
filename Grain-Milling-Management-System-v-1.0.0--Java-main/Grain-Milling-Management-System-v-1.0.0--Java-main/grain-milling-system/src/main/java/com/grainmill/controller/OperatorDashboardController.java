package com.grainmill.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class OperatorDashboardController {
    
    @FXML private Label welcomeLabel;
    @FXML private StackPane contentArea;
    
    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        welcomeLabel.setText("Welcome, " + session.getFullName());
    }
    
    @FXML
    private void showDailyTransactions() {
        loadContent("/fxml/operator/DailyTransactions.fxml");
    }
    
    @FXML
    private void showOnlineOrders() {
        loadContent("/fxml/operator/OnlineOrders.fxml");
    }
    
    @FXML
    private void showProductionRecords() {
        loadContent("/fxml/operator/ProductionRecords.fxml");
    }
    
    @FXML
    private void showInventory() {
        loadContent("/fxml/operator/Inventory.fxml");
    }
    
    @FXML
    private void showCustomerAccounts() {
        loadContent("/fxml/operator/CustomerAccounts.fxml");
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