package com.grainmill.controller;

public class UserSession {
    private static UserSession instance;
    
    private int userId;
    private String username;
    private String role;
    private String fullName;
    
    private UserSession() {}
    
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public void cleanUserSession() {
        userId = 0;
        username = null;
        role = null;
        fullName = null;
    }
}