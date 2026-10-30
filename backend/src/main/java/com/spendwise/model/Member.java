package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Member extends User {

    private static final List<String> MENU = List.of("DASHBOARD", "TRANSACTIONS", "WALLETS",
            "CATEGORIES", "BUDGETS", "GOALS", "REPORTS", "PROFILE");

    private final List<Wallet> wallets = new ArrayList<>();

    public Member(String username, String passwordHash, String fullName, String email) {
        super(username, passwordHash, fullName, email);
    }

    /** Konstruktor untuk DAO saat membaca baris dari database. */
    public Member(int userId, String username, String passwordHash, String fullName, String email,
                  boolean isActive, LocalDateTime createdAt) {
        super(userId, username, passwordHash, fullName, email, isActive, createdAt);
    }

    public void addWallet(Wallet wallet) {
        if (wallet != null) {
            wallets.add(wallet);
        }
    }

    public boolean removeWallet(int walletId) {
        return wallets.removeIf(w -> w.getWalletId() == walletId);
    }

    @JsonIgnore
    public double getTotalBalance() {
        double total = wallets.stream().mapToDouble(Wallet::getBalance).sum();
        return Math.round(total * 100.0) / 100.0;
    }

    @JsonIgnore
    public List<Wallet> getWallets() {
        return List.copyOf(wallets);
    }

    @Override
    public String getRole() {
        return "MEMBER";
    }

    @Override
    public List<String> getMenuAccess() {
        return MENU;
    }
}
