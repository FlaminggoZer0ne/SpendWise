package com.spendwise.service;

import com.spendwise.core.BusinessException;
import com.spendwise.dao.CategoryDAO;
import com.spendwise.dao.TransactionDAO;
import com.spendwise.dao.WalletDAO;
import com.spendwise.dto.PageResult;
import com.spendwise.dto.TransactionFilter;
import com.spendwise.dto.TransactionRequest;
import com.spendwise.model.Category;
import com.spendwise.model.Expense;
import com.spendwise.model.Income;
import com.spendwise.model.Transaction;
import com.spendwise.model.TransactionType;
import com.spendwise.model.Wallet;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

/** Inti Modul 2: membangun Income/Expense, menerapkannya ke saldo, menyimpan, lalu menyinkronkan anggaran. */
@Service
public class TransactionService {

    private final TransactionDAO transactionDAO;
    private final WalletDAO walletDAO;
    private final CategoryDAO categoryDAO;
    private final BudgetService budgetService;

    public TransactionService(TransactionDAO transactionDAO, WalletDAO walletDAO, CategoryDAO categoryDAO,
                              BudgetService budgetService) {
        this.transactionDAO = transactionDAO;
        this.walletDAO = walletDAO;
        this.categoryDAO = categoryDAO;
        this.budgetService = budgetService;
    }

    @Transactional
    public Transaction addTransaction(TransactionRequest req, int userId) {
        Transaction t = buildTransaction(req);
        assertOwned(t, userId);
        t.setUserId(userId);
        t.validate();
        applyOrFail(t);
        walletDAO.update(t.getWallet());
        transactionDAO.save(t);
        t.asExpense().ifPresent(budgetService::checkBudget);
        return t;
    }

    /** Efek lama dibatalkan dulu, baru efek baru diterapkan (berlaku juga jika dompet/jenis berubah). */
    @Transactional
    public boolean editTransaction(int id, TransactionRequest req, int userId) {
        Transaction old = findOwned(id, userId);
        old.revertFromWallet();
        walletDAO.update(old.getWallet());

        // dibangun setelah saldo lama dikembalikan agar dompet dimuat dengan saldo terbaru
        Transaction t = buildTransaction(req);
        assertOwned(t, userId);
        t.setUserId(userId);
        t.setTransactionId(id);
        t.validate();
        applyOrFail(t);
        walletDAO.update(t.getWallet());
        boolean updated = transactionDAO.update(t);

        recalculateBudget(old);
        recalculateBudget(t);
        return updated;
    }

    @Transactional
    public boolean deleteTransaction(int id, int userId) {
        Transaction old = findOwned(id, userId);
        old.revertFromWallet();
        walletDAO.update(old.getWallet());
        boolean deleted = transactionDAO.delete(id);
        recalculateBudget(old);
        return deleted;
    }

    public List<Transaction> search(int userId, TransactionFilter f) {
        assertValidRange(f);
        return transactionDAO.findByFilter(userId, f);
    }

    /** Satu halaman hasil beserta total data, total pemasukan, dan total pengeluaran dari seluruh hasil filter. */
    public PageResult<Transaction> searchPage(int userId, TransactionFilter f) {
        List<Transaction> items = search(userId, f);
        return PageResult.of(items, f.pageOrDefault(), f.sizeOrDefault(),
                transactionDAO.countByFilter(userId, f),
                transactionDAO.sumByFilter(userId, f, TransactionType.INCOME),
                transactionDAO.sumByFilter(userId, f, TransactionType.EXPENSE));
    }

    public Transaction getTransaction(int id, int userId) {
        return findOwned(id, userId);
    }

    /** Factory: jenis transaksi menentukan subclass yang dibuat. */
    private Transaction buildTransaction(TransactionRequest req) {
        Wallet wallet = walletDAO.findById(req.walletId());
        if (wallet == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Dompet tidak ditemukan");
        }
        Category category = categoryDAO.findById(req.categoryId());
        if (category == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Kategori tidak ditemukan");
        }
        String description = req.description() == null || req.description().isBlank()
                ? null : req.description().trim();
        return switch (req.type()) {
            case INCOME -> new Income(req.amount(), req.date(), description, category, wallet, blankToNull(req.source()));
            case EXPENSE -> new Expense(req.amount(), req.date(), description, category, wallet,
                    blankToNull(req.paymentMethod()), Boolean.TRUE.equals(req.isRecurring()));
        };
    }

    /** Dompet harus milik pengguna; kategori default atau milik pengguna. Jika tidak: 404 (tanpa bocor). */
    private void assertOwned(Transaction t, int userId) {
        if (t.getWallet().getOwnerId() != userId) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Dompet tidak ditemukan");
        }
        Category c = t.getCategory();
        if (!c.isDefault() && !c.isOwnedBy(userId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Kategori tidak ditemukan");
        }
    }

    private Transaction findOwned(int id, int userId) {
        Transaction t = transactionDAO.findById(id);
        if (t == null || t.getUserId() != userId) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Transaksi tidak ditemukan");
        }
        return t;
    }

    private void applyOrFail(Transaction t) {
        if (!t.applyToWallet()) {
            throw new BusinessException("Saldo dompet tidak mencukupi");
        }
    }

    private void recalculateBudget(Transaction t) {
        budgetService.recalculate(t.getUserId(), t.getCategory().getCategoryId(), YearMonth.from(t.getDate()));
    }

    private void assertValidRange(TransactionFilter f) {
        if (f.fromOrDefault().isAfter(f.toOrDefault())) {
            throw new BusinessException("Tanggal awal tidak boleh setelah tanggal akhir");
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
