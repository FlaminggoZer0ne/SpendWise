package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spendwise.core.BusinessException;
import com.spendwise.core.PasswordHasher;

import java.time.LocalDateTime;
import java.util.List;

public abstract class User {

    private static final int MIN_PASSWORD_LENGTH = 8;

    protected int userId;
    protected String username;
    protected String passwordHash;
    protected String fullName;
    protected String email;
    protected boolean isActive;
    protected LocalDateTime createdAt;

    public User(String username, String passwordHash, String fullName, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    /** Konstruktor untuk DAO saat membaca baris dari database. */
    protected User(int userId, String username, String passwordHash, String fullName, String email,
                   boolean isActive, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public boolean checkPassword(String rawPassword) {
        return PasswordHasher.matches(rawPassword, passwordHash);
    }

    public boolean changePassword(String oldPw, String newPw) {
        if (!checkPassword(oldPw)) {
            throw new BusinessException("Password lama tidak sesuai");
        }
        if (newPw == null || newPw.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException("Password baru minimal 8 karakter");
        }
        if (newPw.equals(oldPw)) {
            throw new BusinessException("Password baru tidak boleh sama dengan password lama");
        }
        this.passwordHash = PasswordHasher.hash(newPw);
        return true;
    }

    public abstract String getRole();

    public abstract List<String> getMenuAccess();

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
