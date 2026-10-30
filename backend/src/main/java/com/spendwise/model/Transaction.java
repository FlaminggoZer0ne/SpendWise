package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spendwise.core.BusinessException;
import com.spendwise.core.Validatable;

import java.time.LocalDate;
import java.util.Optional;

public abstract class Transaction implements Validatable {

    private static final double MAX_AMOUNT = 1_000_000_000;

    protected int transactionId;
    protected int userId;
    protected double amount;
    protected LocalDate date;
    protected String description;
    protected Category category;
    protected Wallet wallet;

    public Transaction(double amount, LocalDate date, String desc, Category c, Wallet w) {
        this.amount = amount;
        this.date = date;
        this.description = desc;
        this.category = c;
        this.wallet = w;
    }

    public abstract TransactionType getType();

    /** Menerapkan efek transaksi ke saldo dompet (in-memory). @return false jika saldo tidak cukup */
    public abstract boolean applyToWallet();

    /** Membatalkan efek transaksi dari saldo dompet (in-memory). */
    public abstract void revertFromWallet();

    @Override
    public boolean validate() {
        if (amount <= 0 || amount > MAX_AMOUNT) {
            throw new BusinessException("Nominal harus lebih dari 0 dan maksimal 1.000.000.000");
        }
        if (date == null) {
            throw new BusinessException("Tanggal wajib diisi");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new BusinessException("Tanggal tidak boleh di masa depan");
        }
        if (category == null) {
            throw new BusinessException("Kategori wajib dipilih");
        }
        if (wallet == null) {
            throw new BusinessException("Dompet wajib dipilih");
        }
        if (category.getType() != getType()) {
            throw new BusinessException("Kategori tidak sesuai dengan jenis transaksi");
        }
        return true;
    }

    /** Pengeluaran mengembalikan dirinya, pemasukan kosong (menggantikan instanceof di service). */
    public Optional<Expense> asExpense() {
        return Optional.empty();
    }

    /** Hanya bermakna untuk Income; Expense mengembalikan null. */
    public String getSource() {
        return null;
    }

    /** Hanya bermakna untuk Expense; Income mengembalikan null. */
    public String getPaymentMethod() {
        return null;
    }

    @JsonProperty("isRecurring")
    public boolean isRecurring() {
        return false;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public Wallet getWallet() {
        return wallet;
    }
}
