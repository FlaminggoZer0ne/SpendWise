package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.spendwise.core.BusinessException;
import com.spendwise.core.Validatable;

/** Dompet. Saldo hanya berubah lewat deposit, withdraw, dan transferTo (BR-01). */
public class Wallet implements Validatable {

    private static final int NAME_MAX = 50;

    private int walletId;
    private int ownerId;
    private String name;
    private WalletType walletType;
    private double balance;

    /** Untuk deserialisasi JSON request pembuatan/ubah dompet. */
    private Wallet() {
    }

    /** Dompet baru; saldo awal masuk lewat deposit. */
    public Wallet(int ownerId, String name, WalletType walletType, double initialBalance) {
        this.ownerId = ownerId;
        this.name = name;
        this.walletType = walletType;
        if (initialBalance > 0) {
            deposit(initialBalance);
        }
    }

    /** Konstruktor lengkap untuk DAO saat membaca dari database. */
    public Wallet(int walletId, int ownerId, String name, WalletType walletType, double balance) {
        this.walletId = walletId;
        this.ownerId = ownerId;
        this.name = name;
        this.walletType = walletType;
        this.balance = round2(balance);
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new BusinessException("Nominal harus lebih dari 0");
        }
        balance = round2(balance + amount);
    }

    /** @return false jika saldo tidak cukup; pemanggil yang memutuskan pesan error */
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            throw new BusinessException("Nominal harus lebih dari 0");
        }
        if (amount > balance) {
            return false;
        }
        balance = round2(balance - amount);
        return true;
    }

    public boolean transferTo(Wallet target, double amount) {
        if (target == null || target.walletId == this.walletId) {
            throw new BusinessException("Dompet asal dan tujuan tidak boleh sama");
        }
        if (!withdraw(amount)) {
            return false;
        }
        target.deposit(amount);
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public double getBalance() {
        return balance;
    }

    @Override
    public boolean validate() {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Nama dompet wajib diisi");
        }
        if (name.length() > NAME_MAX) {
            throw new BusinessException("Nama dompet maksimal 50 karakter");
        }
        if (walletType == null) {
            throw new BusinessException("Jenis dompet wajib dipilih");
        }
        if (balance < 0) {
            throw new BusinessException("Saldo tidak boleh negatif");
        }
        return true;
    }

    /** Saldo awal dari JSON {@code initialBalance}; hanya berlaku pada dompet yang masih kosong. */
    @JsonSetter("initialBalance")
    private void applyInitialBalance(Double amount) {
        if (amount != null && amount > 0 && balance == 0) {
            deposit(amount);
        } else if (amount != null && amount < 0) {
            throw new BusinessException("Saldo awal tidak boleh negatif");
        }
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getWalletId() {
        return walletId;
    }

    public void setWalletId(int walletId) {
        this.walletId = walletId;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WalletType getWalletType() {
        return walletType;
    }

    public void setWalletType(WalletType walletType) {
        this.walletType = walletType;
    }

    private static double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
