package com.spendwise.controller;

import com.spendwise.core.ApiResponse;
import com.spendwise.core.BaseController;
import com.spendwise.dto.TransactionFilter;
import com.spendwise.dto.TransactionRequest;
import com.spendwise.model.Transaction;
import com.spendwise.model.User;
import com.spendwise.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController extends BaseController {

    private static final String MEMBER = "MEMBER";

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list(@ModelAttribute TransactionFilter f, HttpSession s) {
        User user = requireRole(s, MEMBER);
        return ok(transactionService.searchPage(user.getUserId(), f));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody TransactionRequest req, HttpSession s) {
        User user = requireRole(s, MEMBER);
        Transaction t = transactionService.addTransaction(req, user.getUserId());
        return created("Transaksi berhasil disimpan", t);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable int id, @Valid @RequestBody TransactionRequest req,
                                              HttpSession s) {
        User user = requireRole(s, MEMBER);
        transactionService.editTransaction(id, req, user.getUserId());
        return ok("Transaksi berhasil diperbarui", transactionService.getTransaction(id, user.getUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable int id, HttpSession s) {
        User user = requireRole(s, MEMBER);
        transactionService.deleteTransaction(id, user.getUserId());
        return ok("Transaksi berhasil dihapus", null);
    }
}
