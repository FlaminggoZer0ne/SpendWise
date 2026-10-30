package com.spendwise.controller;

import com.spendwise.core.ApiResponse;
import com.spendwise.core.BaseController;
import com.spendwise.dto.TransferRequest;
import com.spendwise.model.User;
import com.spendwise.model.Wallet;
import com.spendwise.service.WalletService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
public class WalletController extends BaseController {

    private static final String MEMBER = "MEMBER";

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list(HttpSession s) {
        User user = requireRole(s, MEMBER);
        List<Wallet> wallets = walletService.getWallets(user.getUserId());
        double total = Math.round(wallets.stream().mapToDouble(Wallet::getBalance).sum() * 100.0) / 100.0;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalBalance", total);
        data.put("wallets", wallets);
        return ok(data);
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody Wallet w, HttpSession s) {
        User user = requireRole(s, MEMBER);
        w.setOwnerId(user.getUserId());
        walletService.createWallet(w);
        return created("Dompet berhasil ditambahkan", w);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable int id, @RequestBody Wallet w, HttpSession s) {
        User user = requireRole(s, MEMBER);
        w.setWalletId(id);
        w.setOwnerId(user.getUserId());
        walletService.updateWallet(w);
        return ok("Dompet berhasil diperbarui", walletService.getWallet(id, user.getUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable int id, HttpSession s) {
        User user = requireRole(s, MEMBER);
        walletService.deleteWallet(id, user.getUserId());
        return ok("Dompet berhasil dihapus", null);
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse> transfer(@Valid @RequestBody TransferRequest req, HttpSession s) {
        User user = requireRole(s, MEMBER);
        walletService.transfer(req.fromWalletId(), req.toWalletId(), req.amount(), user.getUserId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("from", walletService.getWallet(req.fromWalletId(), user.getUserId()));
        data.put("to", walletService.getWallet(req.toWalletId(), user.getUserId()));
        return ok("Transfer berhasil", data);
    }
}
