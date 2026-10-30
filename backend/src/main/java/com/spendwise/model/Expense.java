package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Optional;

public class Expense extends Transaction {

    private final String paymentMethod;
    private final boolean isRecurring;

    public Expense(double amount, LocalDate date, String desc, Category c, Wallet w,
                   String paymentMethod, boolean isRecurring) {
        super(amount, date, desc, c, w);
        this.paymentMethod = paymentMethod;
        this.isRecurring = isRecurring;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.EXPENSE;
    }

    @Override
    public boolean applyToWallet() {
        return wallet.withdraw(amount);
    }

    @Override
    public void revertFromWallet() {
        wallet.deposit(amount);
    }

    @Override
    public Optional<Expense> asExpense() {
        return Optional.of(this);
    }

    @Override
    public String getPaymentMethod() {
        return paymentMethod;
    }

    @Override
    @JsonProperty("isRecurring")
    public boolean isRecurring() {
        return isRecurring;
    }
}
