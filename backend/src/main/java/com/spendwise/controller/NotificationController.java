package com.spendwise.controller;

import com.spendwise.core.ApiResponse;
import com.spendwise.core.BaseController;
import com.spendwise.model.User;
import com.spendwise.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController extends BaseController {

    private static final String MEMBER = "MEMBER";
    private static final int RECENT_LIMIT = 20;

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** Jumlah belum dibaca + 20 notifikasi terbaru (dibaca maupun belum). */
    @GetMapping
    public ResponseEntity<ApiResponse> unread(HttpSession s) {
        User user = requireRole(s, MEMBER);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("unreadCount", notificationService.countUnread(user.getUserId()));
        data.put("items", notificationService.getRecent(user.getUserId(), RECENT_LIMIT));
        return ok(data);
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse> markAllRead(HttpSession s) {
        User user = requireRole(s, MEMBER);
        notificationService.markAllRead(user.getUserId());
        return ok("Semua notifikasi ditandai sudah dibaca", null);
    }
}
