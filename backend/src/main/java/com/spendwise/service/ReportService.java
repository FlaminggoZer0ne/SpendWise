package com.spendwise.service;

import com.spendwise.core.BusinessException;
import com.spendwise.dao.BudgetDAO;
import com.spendwise.dao.SavingGoalDAO;
import com.spendwise.dao.TransactionDAO;
import com.spendwise.dao.WalletDAO;
import com.spendwise.model.Budget;
import com.spendwise.model.CategoryReport;
import com.spendwise.model.DashboardSummary;
import com.spendwise.model.GoalStatus;
import com.spendwise.model.PeriodReport;
import com.spendwise.model.PeriodType;
import com.spendwise.model.SavingGoal;
import com.spendwise.model.Transaction;
import com.spendwise.model.Wallet;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReportService {

    private static final long MAX_RANGE_DAYS = 3660;

    private final TransactionDAO transactionDAO;
    private final BudgetDAO budgetDAO;
    private final WalletDAO walletDAO;
    private final SavingGoalDAO savingGoalDAO;

    public ReportService(TransactionDAO transactionDAO, BudgetDAO budgetDAO, WalletDAO walletDAO,
                         SavingGoalDAO savingGoalDAO) {
        this.transactionDAO = transactionDAO;
        this.budgetDAO = budgetDAO;
        this.walletDAO = walletDAO;
        this.savingGoalDAO = savingGoalDAO;
    }

    /** Tanggal kosong berarti bulan berjalan. */
    public CategoryReport createCategoryReport(int userId, LocalDate from, LocalDate to) {
        LocalDate start = from != null ? from : monthStart();
        LocalDate end = to != null ? to : monthEnd();
        assertValidRange(start, end);
        CategoryReport report = new CategoryReport(userId, start, end, transactionDAO.findByPeriod(userId, start, end));
        report.generate();
        return report;
    }

    /** Jenis periode kosong berarti MONTHLY; dibandingkan dengan rentang sebelumnya yang sama panjang (BR-10). */
    public PeriodReport createPeriodReport(int userId, PeriodType type, LocalDate from, LocalDate to) {
        PeriodType periodType = type != null ? type : PeriodType.MONTHLY;
        LocalDate start = from != null ? from : monthStart();
        LocalDate end = to != null ? to : monthEnd();
        assertValidRange(start, end);

        PeriodReport report = new PeriodReport(userId, periodType, start, end,
                transactionDAO.findByPeriod(userId, start, end));
        report.generate();

        long days = ChronoUnit.DAYS.between(start, end) + 1;
        LocalDate prevEnd = start.minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(days - 1);
        List<Transaction> previous = transactionDAO.findByPeriod(userId, prevStart, prevEnd);
        report.setPreviousPeriodExpense(new CategoryReport(userId, prevStart, prevEnd, previous).getTotalExpense());
        return report;
    }

    public DashboardSummary getDashboardSummary(int userId) {
        double totalBalance = walletDAO.findByOwner(userId).stream().mapToDouble(Wallet::getBalance).sum();

        CategoryReport month = new CategoryReport(userId, monthStart(), monthEnd(),
                transactionDAO.findByPeriod(userId, monthStart(), monthEnd()));

        List<Budget> budgets = budgetDAO.findByUserAndMonth(userId, YearMonth.now());
        int over = (int) budgets.stream().filter(Budget::isOverBudget).count();
        int activeGoals = (int) savingGoalDAO.findByUser(userId).stream()
                .filter(g -> g.getStatus() == GoalStatus.ACTIVE).count();

        return new DashboardSummary(Math.round(totalBalance * 100.0) / 100.0, month.getTotalIncome(),
                month.getTotalExpense(), budgets.size(), over, activeGoals);
    }

    private void assertValidRange(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new BusinessException("Tanggal awal tidak boleh setelah tanggal akhir");
        }
        if (ChronoUnit.DAYS.between(start, end) > MAX_RANGE_DAYS) {
            throw new BusinessException("Rentang tanggal maksimal 10 tahun");
        }
    }

    private LocalDate monthStart() {
        return LocalDate.now().withDayOfMonth(1);
    }

    private LocalDate monthEnd() {
        return LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
    }
}
