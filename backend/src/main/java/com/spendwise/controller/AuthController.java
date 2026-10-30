package com.spendwise.controller;

import com.spendwise.core.ApiResponse;
import com.spendwise.core.BaseController;
import com.spendwise.dto.LoginRequest;
import com.spendwise.dto.PasswordRequest;
import com.spendwise.dto.ProfileRequest;
import com.spendwise.dto.RegisterRequest;
import com.spendwise.model.Member;
import com.spendwise.model.User;
import com.spendwise.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Validasi gagal",
                    Map.of("confirmPassword", "Konfirmasi password tidak sama")));
        }
        Member member = authService.register(req.username(), req.password(), req.fullName(), req.email());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", member.getUserId());
        data.put("username", member.getUsername());
        data.put("role", member.getRole());
        return created("Registrasi berhasil, silakan login", data);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest req, HttpSession s) {
        User user = authService.login(req.username(), req.password());
        s.setAttribute(SESSION_USER, user);
        return ok("Login berhasil", user);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpSession s) {
        requireLogin(s);
        s.invalidate();
        return ok("Logout berhasil", null);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> me(HttpSession s) {
        User current = requireLogin(s);
        User fresh = authService.getUser(current.getUserId());
        s.setAttribute(SESSION_USER, fresh);
        return ok(fresh);
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(@Valid @RequestBody ProfileRequest req, HttpSession s) {
        User current = requireLogin(s);
        User updated = authService.updateProfile(current.getUserId(), req.fullName(), req.email());
        s.setAttribute(SESSION_USER, updated);
        return ok("Profil berhasil diperbarui", updated);
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody PasswordRequest req, HttpSession s) {
        User current = requireLogin(s);
        if (!req.newPassword().equals(req.confirmPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Validasi gagal",
                    Map.of("confirmPassword", "Konfirmasi password tidak sama")));
        }
        authService.changePassword(current.getUserId(), req.oldPassword(), req.newPassword());
        s.setAttribute(SESSION_USER, authService.getUser(current.getUserId()));
        return ok("Password berhasil diubah", null);
    }
}
