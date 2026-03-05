# Grain Milling Management System

A comprehensive JavaFX application for managing grain milling operations with customer, operator, and admin interfaces.

## Features

### Customer Interface
- Place new orders with real-time cost calculation
- Track order status
- View order history
- Profile management

### Operator Interface  
- Process daily transactions
- Manage online orders
- Record production data
- Monitor inventory

### Admin Interface
- Financial reporting and analytics
- Price management
- Asset management
- User management
- System analytics

## Setup on GitHub Codespace

1. **Open in Codespace**: Click the "Code" button and select "Open with Codespaces"
2. **Automatic Setup**: The devcontainer will automatically:
   - Install Java 17 and above 
   - Setup MySQL database
   - Import database schema
   - Install necessary extensions

3. **Manual Setup (if needed)**:
   ```bash
   chmod +x setup-database.sh
   ./setup-database.sh
   mvn clean compile
