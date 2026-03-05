package com.grainmill.main;

import com.grainmill.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MainApp extends Application {
    
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;
            
            // Test database connection
            System.out.println("Testing database connection...");
            if (!DatabaseConnection.testConnection()) {
                showErrorAlert("Database Error", "Cannot connect to database. Please check if MySQL is running.");
                return;
            }
            
            showLoginScreen();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Startup Error", "Failed to start application: " + e.getMessage());
        }
    }
    
    public static void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            primaryStage.setTitle("Grain Milling System - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("UI Error", "Failed to load login screen: " + e.getMessage());
        }
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    @Override
    public void stop() {
        // Close database connection when application closes
        DatabaseConnection.closeConnection();
        System.out.println("Application stopped");
    }
    
    private static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        // For Codespace, we need to setup database first
        System.out.println("Starting Grain Milling System...");
        
        // Initialize database
        try {
            Runtime.getRuntime().exec(new String[]{
                "mysql", "-u", "root", "-proot", "-e", 
                "CREATE DATABASE IF NOT EXISTS grain_milling_system;"
            }).waitFor();
            
            Runtime.getRuntime().exec(new String[]{
                "mysql", "-u", "root", "-proot", "grain_milling_system", 
                "-e", "SOURCE database/grain_milling_system.sql"
            }).waitFor();
            
            System.out.println("Database initialized successfully");
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
        
        // Launch JavaFX application
        launch(args);
    }
}