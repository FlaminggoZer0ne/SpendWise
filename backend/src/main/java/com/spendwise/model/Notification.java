package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/** Notifikasi untuk satu pengguna. level: WARNING | DANGER. */
public class Notification {

    public static final String WARNING = "WARNING";
    public static final String DANGER = "DANGER";

    private int notificationId;
    private int userId;
    private String message;
    private String level;
    private boolean isRead;
    private LocalDateTime createdAt;

    public Notification(int userId, String message, String level) {
        this.userId = userId;
        this.message = message;
        this.level = level;
        this.createdAt = LocalDateTime.now();
    }

    /** Konstruktor lengkap untuk DAO saat membaca dari database. */
    public Notification(int notificationId, int userId, String message, String level,
                        boolean isRead, LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.message = message;
        this.level = level;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public String getMessage() {
        return message;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public int getUserId() {
        return userId;
    }

    public String getLevel() {
        return level;
    }

    @JsonProperty("isRead")
    public boolean isRead() {
        return isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
