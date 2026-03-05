package com.grainmill.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class CustomerDashboardController {
    
    @FXML private Label welcomeLabel;
    @FXML private StackPane contentArea;
    
    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        welcomeLabel.setText("Welcome, " + session.getFullName());
        showNewOrder();
    }
    
    @FXML
    private void showNewOrder() {
        loadContent("/fxml/customer/NewOrder.fxml");
    }
    
    @FXML
    private void showMyOrders() {
        loadContent("/fxml/customer/MyOrders.fxml");
    }
    
    @FXML
    private void showMyProfile() {
        loadContent("/fxml/customer/MyProfile.fxml");
    }
    
    @FXML
    private void showTrackOrder() {
        loadContent("/fxml/customer/TrackOrder.fxml");
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