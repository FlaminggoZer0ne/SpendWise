package com.spendwise.model;

import com.spendwise.core.BusinessException;

import java.time.LocalDate;

public class Income extends Transaction {

    private final String source;

    public Income(double amount, LocalDate date, String desc, Category c, Wallet w, String source) {
        super(amount, date, desc, c, w);
        this.source = source;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.INCOME;
    }

    @Override
    public boolean applyToWallet() {
        wallet.deposit(amount);
        return true;
    }

    @Override
    public void revertFromWallet() {
        if (!wallet.withdraw(amount)) {
            throw new BusinessException("Saldo dompet tidak mencukupi untuk membatalkan pemasukan ini");
        }
    }

    @Override
    public String getSource() {
        return source;
    }
}
