package com.spendwise.service;

import com.spendwise.core.BusinessException;
import com.spendwise.dao.BudgetDAO;
import com.spendwise.dao.CategoryDAO;
import com.spendwise.dao.TransactionDAO;
import com.spendwise.model.Budget;
import com.spendwise.model.BudgetAlertListener;
import com.spendwise.model.Category;
import com.spendwise.model.Expense;
import com.spendwise.model.TransactionType;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Subject pada pola Observer: memanggil semua {@link BudgetAlertListener} saat ambang 80%/100% dilewati. */
@Service
public class BudgetService {

    private static final double EXCEEDED_THRESHOLD = 1.0;

    private final BudgetDAO budgetDAO;
    private final CategoryDAO categoryDAO;
    private final TransactionDAO transactionDAO;
    private final List<BudgetAlertListener> listeners = new CopyOnWriteArrayList<>();

    public BudgetService(BudgetDAO budgetDAO, CategoryDAO categoryDAO, TransactionDAO transactionDAO) {
        this.budgetDAO = budgetDAO;
        this.categoryDAO = categoryDAO;
        this.transactionDAO = transactionDAO;
    }

    /** Melengkapi anggaran dari request (kategori, nama, terpakai awal) lalu menyimpannya. */
    public boolean createBudget(Budget b) {
        Category category = categoryDAO.findById(b.getCategoryId());
        if (category == null || !(category.isDefault() || category.isOwnedBy(b.getUserId()))) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Kategori tidak ditemukan");
        }
        if (category.getType() != TransactionType.EXPENSE) {
            throw new BusinessException("Anggaran hanya untuk kategori pengeluaran");
        }
        b.setCategory(category);
        b.setName("Anggaran " + category.getName());
        b.validate();
        if (budgetDAO.findByCategoryAndMonth(b.getUserId(), category.getCategoryId(), b.getPeriod()) != null) {
            throw duplicate();
        }
        b.setSpentAmount(transactionDAO.sumExpense(b.getUserId(), category.getCategoryId(), b.getPeriod()));
        try {
            return budgetDAO.save(b);
        } catch (DuplicateKeyException e) {
            throw duplicate();
        }
    }

    /** Hanya batas anggaran yang berubah. */
    public boolean updateBudget(Budget b) {
        Budget existing = getBudget(b.getPlanId(), b.getUserId());
        existing.setTargetAmount(b.getTargetAmount());
        existing.validate();
        return budgetDAO.update(existing);
    }

    public boolean deleteBudget(int id, int userId) {
        Budget existing = getBudget(id, userId);
        return budgetDAO.delete(existing.getPlanId());
    }

    public Budget getBudget(int id, int userId) {
        Budget budget = budgetDAO.findById(id);
        if (budget == null || budget.getUserId() != userId) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Anggaran tidak ditemukan");
        }
        return budget;
    }

    public List<Budget> getBudgetsByMonth(int userId, YearMonth m) {
        return budgetDAO.findByUserAndMonth(userId, m);
    }

    /** Menambah pengeluaran ke anggaran terkait; listener dipanggil hanya saat ambang <b>dilewati</b> (BR-05). */
    public void checkBudget(Expense e) {
        YearMonth month = YearMonth.from(e.getDate());
        Budget b = budgetDAO.findByCategoryAndMonth(e.getUserId(), e.getCategory().getCategoryId(), month);
        if (b == null) {
            return;
        }
        double before = b.getProgress();
        b.addSpending(e.getAmount());
        budgetDAO.update(b);
        notifyListeners(b, before);
    }

    /** Menyetel ulang terpakai dari data transaksi; tidak mengirim notifikasi. */
    public void recalculate(int userId, int categoryId, YearMonth m) {
        Budget b = budgetDAO.findByCategoryAndMonth(userId, categoryId, m);
        if (b == null) {
            return;
        }
        b.setSpentAmount(transactionDAO.sumExpense(userId, categoryId, m));
        budgetDAO.update(b);
    }

    public void addListener(BudgetAlertListener l) {
        listeners.add(l);
    }

    private void notifyListeners(Budget b, double before) {
        double after = b.getProgress();
        if (before < EXCEEDED_THRESHOLD && after >= EXCEEDED_THRESHOLD) {
            listeners.forEach(l -> l.onBudgetExceeded(b));
        } else if (before < Budget.WARNING_THRESHOLD && after >= Budget.WARNING_THRESHOLD) {
            listeners.forEach(l -> l.onBudgetWarning(b, after * 100));
        }
    }

    private BusinessException duplicate() {
        return new BusinessException(HttpStatus.CONFLICT,
                "Anggaran untuk kategori ini pada bulan tersebut sudah ada");
    }
}
