package com.grainmill.controller.customer;

import com.grainmill.controller.UserSession;
import com.grainmill.database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class NewOrderController {
    
    @FXML private ComboBox<String> grainTypeCombo;
    @FXML private TextField weightField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField notesField;
    @FXML private Label millingCostLabel;
    @FXML private Label transportCostLabel;
    @FXML private Label totalCostLabel;
    @FXML private ToggleGroup serviceGroup;
    @FXML private ToggleGroup paymentGroup;
    
    @FXML
    public void initialize() {
        loadGrainTypes();
        
        weightField.textProperty().addListener((observable, oldValue, newValue) -> {
            calculateCost();
        });
        
        serviceGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            calculateCost();
        });
    }
    
    private void loadGrainTypes() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT name FROM grain_types WHERE is_available = TRUE";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                grainTypeCombo.getItems().add(rs.getString("name"));
            }
            
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load grain types: " + e.getMessage());
        }
    }
    
    @FXML
    private void calculateCost() {
        try {
            String grainType = grainTypeCombo.getValue();
            String weightText = weightField.getText();
            
            if (grainType == null || weightText.isEmpty()) {
                return;
            }
            
            double weight = Double.parseDouble(weightText);
            String serviceType = ((RadioButton) serviceGroup.getSelectedToggle()).getUserData().toString();
            
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT current_price_per_kg FROM grain_types WHERE name = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, grainType);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                double pricePerKg = rs.getDouble("current_price_per_kg");
                double millingCost = weight * pricePerKg;
                
                String serviceSql = "SELECT base_cost FROM service_types WHERE name LIKE ?";
                PreparedStatement serviceStmt = conn.prepareStatement(serviceSql);
                serviceStmt.setString(1, serviceType.equals("PICKUP") ? "%ከቤቴ%" : "%ያምጡልኝ%");
                ResultSet serviceRs = serviceStmt.executeQuery();
                
                double transportCost = 0;
                if (serviceRs.next()) {
                    transportCost = serviceRs.getDouble("base_cost");
                }
                
                double totalCost = millingCost + transportCost;
                
                millingCostLabel.setText(String.format("%.2f ETB", millingCost));
                transportCostLabel.setText(String.format("%.2f ETB", transportCost));
                totalCostLabel.setText(String.format("%.2f ETB", totalCost));
                
                serviceRs.close();
                serviceStmt.close();
            }
            
            rs.close();
            pstmt.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void placeOrder() {
        try {
            if (!validateForm()) {
                return;
            }
            
            UserSession session = UserSession.getInstance();
            Connection conn = DatabaseConnection.getConnection();
            
            String grainSql = "SELECT id FROM grain_types WHERE name = ?";
            PreparedStatement grainStmt = conn.prepareStatement(grainSql);
            grainStmt.setString(1, grainTypeCombo.getValue());
            ResultSet grainRs = grainStmt.executeQuery();
            
            int grainTypeId = 0;
            if (grainRs.next()) {
                grainTypeId = grainRs.getInt("id");
            }
            grainRs.close();
            grainStmt.close();
            
            String serviceType = ((RadioButton) serviceGroup.getSelectedToggle()).getUserData().toString();
            String serviceSql = "SELECT id FROM service_types WHERE name LIKE ?";
            PreparedStatement serviceStmt = conn.prepareStatement(serviceSql);
            serviceStmt.setString(1, serviceType.equals("PICKUP") ? "%ከቤቴ%" : "%ያምጡልኝ%");
            ResultSet serviceRs = serviceStmt.executeQuery();
            
            int serviceTypeId = 0;
            if (serviceRs.next()) {
                serviceTypeId = serviceRs.getInt("id");
            }
            serviceRs.close();
            serviceStmt.close();
            
            String orderSql = "INSERT INTO orders (customer_id, grain_type_id, service_type_id, " +
                    "estimated_weight, total_cost, payment_method, delivery_address, phone, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement orderStmt = conn.prepareStatement(orderSql);
            orderStmt.setInt(1, session.getUserId());
            orderStmt.setInt(2, grainTypeId);
            orderStmt.setInt(3, serviceTypeId);
            orderStmt.setDouble(4, Double.parseDouble(weightField.getText()));
            orderStmt.setDouble(5, Double.parseDouble(totalCostLabel.getText().replace(" ETB", "")));
            orderStmt.setString(6, ((RadioButton) paymentGroup.getSelectedToggle()).getUserData().toString());
            orderStmt.setString(7, addressField.getText());
            orderStmt.setString(8, phoneField.getText());
            orderStmt.setString(9, notesField.getText());
            
            int affectedRows = orderStmt.executeUpdate();
            
            if (affectedRows > 0) {
                showAlert("Success", "Order placed successfully! Your order is being processed.");
                clearForm();
            }
            
            orderStmt.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to place order: " + e.getMessage());
        }
    }
    
    @FXML
    private void clearForm() {
        grainTypeCombo.getSelectionModel().clearSelection();
        weightField.clear();
        addressField.clear();
        phoneField.clear();
        notesField.clear();
        millingCostLabel.setText("0.00 ETB");
        transportCostLabel.setText("0.00 ETB");
        totalCostLabel.setText("0.00 ETB");
    }
    
    private boolean validateForm() {
        if (grainTypeCombo.getValue() == null) {
            showAlert("Validation Error", "Please select grain type");
            return false;
        }
        
        if (weightField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter estimated weight");
            return false;
        }
        
        try {
            double weight = Double.parseDouble(weightField.getText());
            if (weight <= 0) {
                showAlert("Validation Error", "Weight must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid weight");
            return false;
        }
        
        if (addressField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter delivery address");
            return false;
        }
        
        if (phoneField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter phone number");
            return false;
        }
        
        return true;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}