package com.spendwise.controller;

import com.spendwise.core.ApiResponse;
import com.spendwise.core.BaseController;
import com.spendwise.model.Category;
import com.spendwise.model.TransactionType;
import com.spendwise.model.User;
import com.spendwise.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController extends BaseController {

    private static final String MEMBER = "MEMBER";

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** Member: default + milik sendiri. Admin: hanya kategori default (admin tidak punya kategori kustom). */
    @GetMapping
    public ResponseEntity<ApiResponse> list(@RequestParam(required = false) TransactionType type, HttpSession s) {
        User user = requireLogin(s);
        return ok(categoryService.getAvailable(user.getUserId(), type));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody Category c, HttpSession s) {
        User user = requireRole(s, MEMBER);
        categoryService.createCustom(c, user.getUserId());
        return created("Kategori berhasil ditambahkan", c);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable int id, HttpSession s) {
        User user = requireRole(s, MEMBER);
        categoryService.deleteCustom(id, user.getUserId());
        return ok("Kategori berhasil dihapus", null);
    }
}
