package com.spendwise.controller;

import com.spendwise.core.ApiResponse;
import com.spendwise.core.BaseController;
import com.spendwise.dto.ActiveRequest;
import com.spendwise.model.Category;
import com.spendwise.model.User;
import com.spendwise.service.AdminService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController extends BaseController {

    private static final String ADMIN = "ADMIN";

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse> listUsers(HttpSession s) {
        requireRole(s, ADMIN);
        return ok(adminService.listUsers());
    }

    @PatchMapping("/users/{id}/active")
    public ResponseEntity<ApiResponse> setUserActive(@PathVariable int id, @Valid @RequestBody ActiveRequest body,
                                                     HttpSession s) {
        User admin = requireRole(s, ADMIN);
        if (admin.getUserId() == id && !body.active()) {
            return badRequest("Tidak dapat menonaktifkan akun sendiri");
        }
        adminService.setUserActive(id, body.active());
        return ok(body.active() ? "Akun berhasil diaktifkan" : "Akun berhasil dinonaktifkan", null);
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse> createDefaultCategory(@RequestBody Category c, HttpSession s) {
        requireRole(s, ADMIN);
        adminService.createDefaultCategory(c);
        return created("Kategori default berhasil ditambahkan", c);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse> deleteDefaultCategory(@PathVariable int id, HttpSession s) {
        requireRole(s, ADMIN);
        adminService.deleteDefaultCategory(id);
        return ok("Kategori default berhasil dihapus", null);
    }
}
