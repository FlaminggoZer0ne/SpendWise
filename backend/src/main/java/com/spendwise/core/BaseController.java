package com.spendwise.core;

import com.spendwise.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {

    protected static final String SESSION_USER = "SESSION_USER";

    protected User getCurrentUser(HttpSession s) {
        return (User) s.getAttribute(SESSION_USER);
    }

    protected User requireLogin(HttpSession s) {
        User user = getCurrentUser(s);
        if (user == null) {
            throw new UnauthorizedException("Silakan login terlebih dahulu");
        }
        return user;
    }

    protected User requireRole(HttpSession s, String role) {
        User user = requireLogin(s);
        if (!user.getRole().equals(role)) {
            throw new ForbiddenException("Anda tidak memiliki akses ke fitur ini");
        }
        return user;
    }

    protected ResponseEntity<ApiResponse> ok(Object body) {
        return ResponseEntity.ok(ApiResponse.success(body));
    }

    protected ResponseEntity<ApiResponse> ok(String message, Object body) {
        return ResponseEntity.ok(ApiResponse.success(message, body));
    }

    protected ResponseEntity<ApiResponse> created(String message, Object body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, body));
    }

    protected ResponseEntity<ApiResponse> badRequest(String msg) {
        return ResponseEntity.badRequest().body(ApiResponse.error(msg));
    }
}
