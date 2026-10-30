package com.spendwise.model;

import com.spendwise.core.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    private final Category salary = new Category(1, "Gaji", TransactionType.INCOME, true, 0);
    private final Category food = new Category(6, "Makan & Minum", TransactionType.EXPENSE, true, 0);

    private Wallet wallet(double balance) {
        return new Wallet(1, 2, "Tunai", WalletType.CASH, balance);
    }

    private Income income(double amount, Wallet w) {
        return new Income(amount, LocalDate.now(), "Gaji", salary, w, "Kantor");
    }

    private Expense expense(double amount, Wallet w) {
        return new Expense(amount, LocalDate.now(), "Makan", food, w, "QRIS", false);
    }

    @Test
    void incomeAndExpenseHaveDifferentTypes() {
        Wallet w = wallet(100);
        Transaction i = income(10, w);
        Transaction e = expense(10, w);
        assertEquals(TransactionType.INCOME, i.getType());
        assertEquals(TransactionType.EXPENSE, e.getType());
    }

    @Test
    void polymorphicApplyAndRevert() {
        Wallet w = wallet(100);
        Transaction[] all = {income(50, w), expense(30, w)};
        for (Transaction t : all) {
            assertTrue(t.applyToWallet());
        }
        assertEquals(120, w.getBalance(), 0.001);
        for (Transaction t : all) {
            t.revertFromWallet();
        }
        assertEquals(100, w.getBalance(), 0.001);
    }

    @Test
    void expenseApplyFailsWhenBalanceInsufficient() {
        Wallet w = wallet(20);
        assertFalse(expense(30, w).applyToWallet());
        assertEquals(20, w.getBalance(), 0.001);
    }

    @Test
    void incomeRevertFailsWhenBalanceAlreadySpent() {
        Wallet w = wallet(0);
        Income i = income(50, w);
        assertThrows(BusinessException.class, i::revertFromWallet);
    }

    @Test
    void validateAcceptsValidTransaction() {
        assertTrue(expense(45000, wallet(100000)).validate());
    }

    @Test
    void validateRejectsBadAmount() {
        assertThrows(BusinessException.class, () -> expense(0, wallet(1)).validate());
        assertThrows(BusinessException.class, () -> expense(1_000_000_001, wallet(1)).validate());
    }

    @Test
    void validateRejectsFutureDate() {
        Expense e = new Expense(10, LocalDate.now().plusDays(1), "x", food, wallet(1), null, false);
        assertEquals("Tanggal tidak boleh di masa depan",
                assertThrows(BusinessException.class, e::validate).getMessage());
    }

    @Test
    void validateRejectsCategoryTypeMismatch() {
        Expense e = new Expense(10, LocalDate.now(), "x", salary, wallet(1), null, false);
        assertEquals("Kategori tidak sesuai dengan jenis transaksi",
                assertThrows(BusinessException.class, e::validate).getMessage());
    }

    @Test
    void categoryOwnership() {
        Category custom = new Category(15, "Ngopi", TransactionType.EXPENSE, false, 2);
        assertTrue(custom.isOwnedBy(2));
        assertFalse(custom.isOwnedBy(3));
        assertFalse(food.isOwnedBy(2));
    }
}
