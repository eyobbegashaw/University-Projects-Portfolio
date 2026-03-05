package grain-milling-system.src.main.java.com.grainmill.controller.admin;

// package com.grainmill.controller.admin;

import com.grainmill.database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardOverviewController {
    
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label activeCustomersLabel;
    @FXML private Label lowStockLabel;
    @FXML private TableView<RecentOrder> recentOrdersTable;
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private PieChart grainDistributionChart;
    
    @FXML
    public void initialize() {
        loadMetrics();
        loadRecentOrders();
        loadCharts();
    }
    
    private void loadMetrics() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Total Revenue
            String revenueSql = "SELECT SUM(total_cost) as total_revenue FROM orders WHERE order_status = 'COMPLETED'";
            PreparedStatement revenueStmt = conn.prepareStatement(revenueSql);
            ResultSet revenueRs = revenueStmt.executeQuery();
            
            if (revenueRs.next()) {
                double revenue = revenueRs.getDouble("total_revenue");
                totalRevenueLabel.setText(String.format("%.2f ETB", revenue));
            }
            revenueRs.close();
            revenueStmt.close();
            
            // Total Orders
            String ordersSql = "SELECT COUNT(*) as total_orders FROM orders";
            PreparedStatement ordersStmt = conn.prepareStatement(ordersSql);
            ResultSet ordersRs = ordersStmt.executeQuery();
            
            if (ordersRs.next()) {
                totalOrdersLabel.setText(String.valueOf(ordersRs.getInt("total_orders")));
            }
            ordersRs.close();
            ordersStmt.close();
            
            // Active Customers
            String customersSql = "SELECT COUNT(DISTINCT customer_id) as active_customers FROM orders";
            PreparedStatement customersStmt = conn.prepareStatement(customersSql);
            ResultSet customersRs = customersStmt.executeQuery();
            
            if (customersRs.next()) {
                activeCustomersLabel.setText(String.valueOf(customersRs.getInt("active_customers")));
            }
            customersRs.close();
            customersStmt.close();
            
            // Low Stock Items
            String stockSql = "SELECT COUNT(*) as low_stock FROM inventory WHERE quantity_kg <= min_stock_level";
            PreparedStatement stockStmt = conn.prepareStatement(stockSql);
            ResultSet stockRs = stockStmt.executeQuery();
            
            if (stockRs.next()) {
                lowStockLabel.setText(String.valueOf(stockRs.getInt("low_stock")));
            }
            stockRs.close();
            stockStmt.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadRecentOrders() {
        try {
            TableColumn<RecentOrder, Integer> idCol = new TableColumn<>("Order ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
            
            TableColumn<RecentOrder, String> customerCol = new TableColumn<>("Customer");
            customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            
            TableColumn<RecentOrder, Double> amountCol = new TableColumn<>("Amount");
            amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
            
            TableColumn<RecentOrder, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
            
            recentOrdersTable.getColumns().setAll(idCol, customerCol, amountCol, statusCol);
            
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT o.id, u.full_name, o.total_cost, o.order_status " +
                        "FROM orders o " +
                        "JOIN users u ON o.customer_id = u.id " +
                        "ORDER BY o.created_at DESC LIMIT 10";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            ObservableList<RecentOrder> orders = FXCollections.observableArrayList();
            while (rs.next()) {
                orders.add(new RecentOrder(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getDouble("total_cost"),
                    rs.getString("order_status")
                ));
            }
            
            recentOrdersTable.setItems(orders);
            
            rs.close();
            pstmt.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadCharts() {
        loadRevenueChart();
        loadGrainDistributionChart();
    }
    
    private void loadRevenueChart() {
        try {
            revenueChart.getData().clear();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Daily Revenue");
            
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT DATE(created_at) as order_date, SUM(total_cost) as daily_revenue " +
                        "FROM orders " +
                        "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                        "GROUP BY DATE(created_at) " +
                        "ORDER BY order_date";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
            
            while (rs.next()) {
                LocalDate date = rs.getDate("order_date").toLocalDate();
                double revenue = rs.getDouble("daily_revenue");
                
                series.getData().add(new XYChart.Data<>(date.format(formatter), revenue));
            }
            
            revenueChart.getData().add(series);
            
            rs.close();
            pstmt.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadGrainDistributionChart() {
        try {
            grainDistributionChart.getData().clear();
            
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT gt.name, COUNT(o.id) as order_count " +
                        "FROM orders o " +
                        "JOIN grain_types gt ON o.grain_type_id = gt.id " +
                        "GROUP BY gt.name";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            while (rs.next()) {
                pieChartData.add(new PieChart.Data(
                    rs.getString("name"),
                    rs.getInt("order_count")
                ));
            }
            
            grainDistributionChart.setData(pieChartData);
            
            rs.close();
            pstmt.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static class RecentOrder {
        private final int orderId;
        private final String customerName;
        private final double amount;
        private final String status;
        
        public RecentOrder(int orderId, String customerName, double amount, String status) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.amount = amount;
            this.status = status;
        }
        
        public int getOrderId() { return orderId; }
        public String getCustomerName() { return customerName; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }
    }
}
