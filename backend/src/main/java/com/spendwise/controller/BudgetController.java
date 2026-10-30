package com.spendwise.controller;

import com.spendwise.core.ApiResponse;
import com.spendwise.core.BaseController;
import com.spendwise.core.BusinessException;
import com.spendwise.model.Budget;
import com.spendwise.model.User;
import com.spendwise.service.BudgetService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController extends BaseController {

    private static final String MEMBER = "MEMBER";

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list(@RequestParam(required = false) String month, HttpSession s) {
        User user = requireRole(s, MEMBER);
        YearMonth m = parseMonth(month);
        List<Budget> budgets = budgetService.getBudgetsByMonth(user.getUserId(), m);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("month", m.toString());
        data.put("totalLimit", round2(budgets.stream().mapToDouble(Budget::getTargetAmount).sum()));
        data.put("totalSpent", round2(budgets.stream().mapToDouble(Budget::getSpentAmount).sum()));
        data.put("budgets", budgets);
        return ok(data);
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody Budget b, HttpSession s) {
        User user = requireRole(s, MEMBER);
        b.setUserId(user.getUserId());
        budgetService.createBudget(b);
        return created("Anggaran berhasil ditambahkan", b);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable int id, @RequestBody Budget b, HttpSession s) {
        User user = requireRole(s, MEMBER);
        b.setPlanId(id);
        b.setUserId(user.getUserId());
        budgetService.updateBudget(b);
        return ok("Anggaran berhasil diperbarui", budgetService.getBudget(id, user.getUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable int id, HttpSession s) {
        User user = requireRole(s, MEMBER);
        budgetService.deleteBudget(id, user.getUserId());
        return ok("Anggaran berhasil dihapus", null);
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new BusinessException("Format bulan harus YYYY-MM");
        }
    }

    private double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
