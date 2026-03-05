-- grain_milling_system.sql
DROP DATABASE IF EXISTS grain_milling_system;
CREATE DATABASE grain_milling_system;
USE grain_milling_system;

-- Users table for all system users
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER', 'OPERATOR', 'ADMIN') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Customers table with additional information
CREATE TABLE customers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    address TEXT,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    preferred_payment_method ENUM('CASH', 'ONLINE', 'BOTH'),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_order_date TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Grain types
CREATE TABLE grain_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    current_price_per_kg DECIMAL(10, 2) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE
);

-- Service types
CREATE TABLE service_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    base_cost DECIMAL(10, 2) NOT NULL
);

-- Orders table
CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT,
    grain_type_id INT,
    service_type_id INT,
    estimated_weight DECIMAL(8, 2),
    actual_weight DECIMAL(8, 2),
    total_cost DECIMAL(10, 2),
    payment_method ENUM('CASH_ON_DELIVERY', 'ONLINE'),
    payment_status ENUM('PENDING', 'PAID', 'FAILED') DEFAULT 'PENDING',
    order_status ENUM('PENDING', 'ACCEPTED', 'PROCESSING', 'IN_TRANSIT', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    delivery_address TEXT,
    phone VARCHAR(20),
    notes TEXT,
    delivery_latitude DECIMAL(10, 8),
    delivery_longitude DECIMAL(11, 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (grain_type_id) REFERENCES grain_types(id),
    FOREIGN KEY (service_type_id) REFERENCES service_types(id)
);

-- Inventory table for flour stock
CREATE TABLE inventory (
    id INT PRIMARY KEY AUTO_INCREMENT,
    grain_type_id INT,
    quantity_kg DECIMAL(8, 2) NOT NULL,
    min_stock_level DECIMAL(8, 2) DEFAULT 50.00,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (grain_type_id) REFERENCES grain_types(id)
);

-- Production records
CREATE TABLE production_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    input_weight DECIMAL(8, 2),
    output_weight DECIMAL(8, 2),
    moisture_content DECIMAL(5, 2),
    quality_notes TEXT,
    energy_consumption DECIMAL(8, 2),
    recorded_by INT,
    production_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (recorded_by) REFERENCES users(id)
);

-- Financial transactions
CREATE TABLE financial_transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    transaction_type ENUM('REVENUE', 'EXPENSE', 'SALARY', 'MAINTENANCE'),
    amount DECIMAL(10, 2) NOT NULL,
    description TEXT,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    recorded_by INT,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (recorded_by) REFERENCES users(id)
);

-- Machine assets
CREATE TABLE machines (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    purchase_date DATE,
    purchase_price DECIMAL(12, 2),
    expected_lifespan_years INT,
    maintenance_interval_months INT,
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    status ENUM('OPERATIONAL', 'MAINTENANCE', 'BROKEN') DEFAULT 'OPERATIONAL'
);

-- Insert initial data
INSERT INTO users (username, password, role, full_name, phone, email) VALUES
('admin', 'admin123', 'ADMIN', 'System Administrator', '+251911223344', 'admin@mill.com'),
('operator1', 'operator123', 'OPERATOR', 'Main Operator', '+251922334455', 'operator@mill.com'),
('customer1', 'customer123', 'CUSTOMER', 'John Doe', '+251911223345', 'john@example.com'),
('customer2', 'customer123', 'CUSTOMER', 'Jane Smith', '+251922334456', 'jane@example.com');

INSERT INTO customers (user_id, address, preferred_payment_method) VALUES
(3, 'Addis Ababa, Bole District', 'CASH_ON_DELIVERY'),
(4, 'Addis Ababa, Kirkos District', 'ONLINE');

INSERT INTO grain_types (name, description, current_price_per_kg) VALUES
('ስንዴ', 'Premium wheat grain', 12.50),
('ጤፍ', 'High-quality teff', 25.00),
('ገብስ', 'Barley grain', 8.50);

INSERT INTO service_types (name, description, base_cost) VALUES
('እህል ከቤቴ ይውሰዱ', 'Pickup grain from customer location', 50.00),
('የተፈጨ ዱቄት ብቻ ያምጡልኝ', 'Deliver milled flour only', 30.00);

INSERT INTO inventory (grain_type_id, quantity_kg, min_stock_level) VALUES
(1, 500.00, 100.00),
(2, 200.00, 50.00),
(3, 300.00, 80.00);

INSERT INTO machines (name, purchase_date, purchase_price, expected_lifespan_years, maintenance_interval_months, status) VALUES
('Main Milling Machine', '2022-01-15', 250000.00, 10, 6, 'OPERATIONAL'),
('Grain Cleaner', '2022-02-20', 75000.00, 8, 12, 'OPERATIONAL');

-- Insert sample orders
INSERT INTO orders (customer_id, grain_type_id, service_type_id, estimated_weight, total_cost, payment_method, order_status, delivery_address, phone) VALUES
(1, 1, 1, 25.5, 368.75, 'CASH_ON_DELIVERY', 'COMPLETED', 'Bole District, Addis Ababa', '+251911223345'),
(2, 2, 2, 15.0, 405.00, 'ONLINE', 'PROCESSING', 'Kirkos District, Addis Ababa', '+251922334456');

INSERT INTO financial_transactions (order_id, transaction_type, amount, description) VALUES
(1, 'REVENUE', 368.75, 'Order payment'),
(2, 'REVENUE', 405.00, 'Order payment');