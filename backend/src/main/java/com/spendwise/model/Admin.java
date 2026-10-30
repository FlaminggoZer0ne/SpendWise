package com.spendwise.model;

import java.time.LocalDateTime;
import java.util.List;

public class Admin extends User {

    private static final List<String> MENU = List.of("USERS", "DEFAULT_CATEGORIES", "PROFILE");

    private String adminCode;

    public Admin(String username, String passwordHash, String fullName, String email, String adminCode) {
        super(username, passwordHash, fullName, email);
        this.adminCode = adminCode;
    }

    /** Konstruktor untuk DAO saat membaca baris dari database. */
    public Admin(int userId, String username, String passwordHash, String fullName, String email,
                 boolean isActive, LocalDateTime createdAt, String adminCode) {
        super(userId, username, passwordHash, fullName, email, isActive, createdAt);
        this.adminCode = adminCode;
    }

    public boolean canManageUsers() {
        return true;
    }

    public boolean canManageCategories() {
        return true;
    }

    public String getAdminCode() {
        return adminCode;
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public List<String> getMenuAccess() {
        return MENU;
    }
}
