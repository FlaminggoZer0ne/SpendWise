package com.spendwise.controller;

import com.spendwise.core.ApiResponse;
import com.spendwise.core.BaseController;
import com.spendwise.dto.ContributionRequest;
import com.spendwise.model.SavingGoal;
import com.spendwise.model.User;
import com.spendwise.service.SavingGoalService;
import com.spendwise.service.WalletService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class SavingGoalController extends BaseController {

    private static final String MEMBER = "MEMBER";

    private final SavingGoalService savingGoalService;
    private final WalletService walletService;

    public SavingGoalController(SavingGoalService savingGoalService, WalletService walletService) {
        this.savingGoalService = savingGoalService;
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list(HttpSession s) {
        User user = requireRole(s, MEMBER);
        return ok(savingGoalService.getGoals(user.getUserId()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody SavingGoal g, HttpSession s) {
        User user = requireRole(s, MEMBER);
        g.setUserId(user.getUserId());
        savingGoalService.createGoal(g);
        return created("Target tabungan berhasil dibuat", g);
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<ApiResponse> contribute(@PathVariable int id, @Valid @RequestBody ContributionRequest req,
                                                  HttpSession s) {
        User user = requireRole(s, MEMBER);
        savingGoalService.contribute(id, req.amount(), req.walletId(), user.getUserId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("goal", savingGoalService.getGoal(id, user.getUserId()));
        data.put("wallet", walletService.getWallet(req.walletId(), user.getUserId()));
        return ok("Setoran berhasil", data);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse> cancel(@PathVariable int id, HttpSession s) {
        User user = requireRole(s, MEMBER);
        savingGoalService.cancelGoal(id, user.getUserId());
        return ok("Target dibatalkan, dana dikembalikan ke dompet utama",
                savingGoalService.getGoal(id, user.getUserId()));
    }
}
