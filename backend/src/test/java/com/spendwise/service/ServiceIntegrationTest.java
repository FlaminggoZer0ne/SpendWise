package com.spendwise.service;

import com.spendwise.config.ListenerConfig;
import com.spendwise.core.BusinessException;
import com.spendwise.dao.BudgetDAO;
import com.spendwise.dao.CategoryDAO;
import com.spendwise.dao.NotificationDAO;
import com.spendwise.dao.SavingGoalDAO;
import com.spendwise.dao.TransactionDAO;
import com.spendwise.dao.UserDAO;
import com.spendwise.dao.WalletDAO;
import com.spendwise.dto.TransactionFilter;
import com.spendwise.dto.TransactionRequest;
import com.spendwise.model.Budget;
import com.spendwise.model.Category;
import com.spendwise.model.CategoryReport;
import com.spendwise.model.DashboardSummary;
import com.spendwise.model.GoalStatus;
import com.spendwise.model.Member;
import com.spendwise.model.Notification;
import com.spendwise.model.PeriodReport;
import com.spendwise.model.PeriodType;
import com.spendwise.model.SavingGoal;
import com.spendwise.model.Transaction;
import com.spendwise.model.TransactionType;
import com.spendwise.model.Wallet;
import com.spendwise.model.WalletType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Skenario end-to-end di lapisan service terhadap database seed (lihat DaoIntegrationTest untuk cara menjalankan).
 * Tiap tes di-rollback otomatis.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserDAO.class, WalletDAO.class, CategoryDAO.class, TransactionDAO.class, BudgetDAO.class,
        NotificationDAO.class, SavingGoalDAO.class, AuthService.class, WalletService.class, AdminService.class,
        CategoryService.class, TransactionService.class, BudgetService.class, NotificationService.class,
        SavingGoalService.class, ReportService.class, ListenerConfig.class})
@EnabledIfSystemProperty(named = "spendwise.it", matches = "true")
class ServiceIntegrationTest {

    private static final int BUDI = 2;
    private static final int SARI = 3;
    private static final LocalDate DAY = LocalDate.of(2026, 10, 28);

    @Autowired AuthService authService;
    @Autowired WalletService walletService;
    @Autowired AdminService adminService;
    @Autowired CategoryService categoryService;
    @Autowired TransactionService transactionService;
    @Autowired BudgetService budgetService;
    @Autowired NotificationService notificationService;
    @Autowired SavingGoalService savingGoalService;
    @Autowired ReportService reportService;
    @Autowired WalletDAO walletDAO;

    private Wallet wallet(int userId, String name) {
        return walletService.getWallets(userId).stream().filter(w -> w.getName().equals(name))
                .findFirst().orElseThrow();
    }

    private double balance(int userId, String name) {
        return wallet(userId, name).getBalance();
    }

    private Category category(int userId, TransactionType type, String name) {
        return categoryService.getAvailable(userId, type).stream().filter(c -> c.getName().equals(name))
                .findFirst().orElseThrow();
    }

    private TransactionRequest expenseReq(double amount, Wallet w, Category c) {
        return new TransactionRequest(TransactionType.EXPENSE, amount, DAY, c.getCategoryId(), w.getWalletId(),
                "tes", null, "QRIS", false);
    }

    private TransactionRequest incomeReq(double amount, Wallet w, Category c) {
        return new TransactionRequest(TransactionType.INCOME, amount, DAY, c.getCategoryId(), w.getWalletId(),
                "tes", "Kantor", null, null);
    }

    // ---------- F01 / F02 ----------

    @Test
    void registerCreatesCashWalletAndLoginWorks() {
        Member m = authService.register("anggota_baru", "password123", "Anggota Baru", "baru@mail.com");
        assertEquals(1, walletService.getWallets(m.getUserId()).size());
        assertEquals("Tunai", walletService.getWallets(m.getUserId()).get(0).getName());
        assertEquals(m.getUserId(), authService.login("anggota_baru", "password123").getUserId());
        assertEquals(409, assertThrows(BusinessException.class,
                () -> authService.register("ANGGOTA_BARU", "password123", "X", "x@mail.com")).getStatus().value());
    }

    @Test
    void seedAccountsCanLoginAndProfileAndPasswordChange() {
        assertEquals("ADMIN", authService.login("admin", "admin12345").getRole());
        assertEquals("MEMBER", authService.login("budi", "member12345").getRole());
        authService.updateProfile(BUDI, "Budi S.", "budi.s@mail.com");
        assertEquals("Budi S.", authService.getUser(BUDI).getFullName());
        assertTrue(authService.changePassword(BUDI, "member12345", "baru12345"));
        assertEquals(BUDI, authService.login("budi", "baru12345").getUserId());
    }

    @Test
    void adminCanDeactivateAndInactiveUserCannotLogin() {
        adminService.setUserActive(SARI, false);
        assertThrows(com.spendwise.core.ForbiddenException.class, () -> authService.login("sari", "member12345"));
        adminService.setUserActive(SARI, true);
        assertEquals(SARI, authService.login("sari", "member12345").getUserId());
    }

    // ---------- F03 ----------

    @Test
    void transferMovesBothBalancesAndFailureChangesNothing() {
        double tunai = balance(BUDI, "Tunai");
        double bca = balance(BUDI, "BCA");
        Wallet from = wallet(BUDI, "BCA");
        Wallet to = wallet(BUDI, "Tunai");

        assertTrue(walletService.transfer(from.getWalletId(), to.getWalletId(), 200_000, BUDI));
        assertEquals(bca - 200_000, balance(BUDI, "BCA"), 0.001);
        assertEquals(tunai + 200_000, balance(BUDI, "Tunai"), 0.001);

        assertThrows(BusinessException.class,
                () -> walletService.transfer(from.getWalletId(), to.getWalletId(), 999_999_999, BUDI));
        assertEquals(bca - 200_000, balance(BUDI, "BCA"), 0.001);
    }

    @Test
    void cannotTouchAnotherUsersWalletAndWalletWithTransactionsCannotBeDeleted() {
        Wallet budiWallet = wallet(BUDI, "Tunai");
        Wallet sariWallet = walletService.getWallets(SARI).get(0);
        assertEquals(404, assertThrows(BusinessException.class,
                () -> walletService.transfer(budiWallet.getWalletId(), sariWallet.getWalletId(), 10, BUDI))
                .getStatus().value());
        assertEquals("Dompet masih memiliki transaksi", assertThrows(BusinessException.class,
                () -> walletService.deleteWallet(budiWallet.getWalletId(), BUDI)).getMessage());
        assertEquals("Minimal harus ada satu dompet", assertThrows(BusinessException.class,
                () -> walletService.deleteWallet(sariWallet.getWalletId(), SARI)).getMessage());
    }

    @Test
    void updateWalletNeverChangesBalance() {
        Wallet w = wallet(BUDI, "GoPay");
        Wallet request = new Wallet(BUDI, "GoPay Utama", WalletType.E_WALLET, 999_999);
        request.setWalletId(w.getWalletId());
        walletService.updateWallet(request);
        Wallet reloaded = walletService.getWallet(w.getWalletId(), BUDI);
        assertEquals("GoPay Utama", reloaded.getName());
        assertEquals(w.getBalance(), reloaded.getBalance(), 0.001);
    }

    // ---------- F04 / F06 ----------

    @Test
    void defaultAndCustomCategoryRules() {
        Category donasi = new Category(0, "Donasi", TransactionType.EXPENSE, false, 0);
        assertTrue(adminService.createDefaultCategory(donasi));
        assertTrue(donasi.isDefault());
        assertEquals(409, assertThrows(BusinessException.class, () -> adminService.createDefaultCategory(
                new Category(0, "donasi", TransactionType.EXPENSE, false, 0))).getStatus().value());
        assertTrue(adminService.deleteDefaultCategory(donasi.getCategoryId()));

        Category makan = category(BUDI, TransactionType.EXPENSE, "Makan & Minum");
        assertEquals("Kategori sudah dipakai dan tidak dapat dihapus", assertThrows(BusinessException.class,
                () -> adminService.deleteDefaultCategory(makan.getCategoryId())).getMessage());
        assertEquals("Kategori default tidak dapat dihapus", assertThrows(BusinessException.class,
                () -> categoryService.deleteCustom(makan.getCategoryId(), BUDI)).getMessage());

        Category custom = new Category(0, "Hobi", TransactionType.EXPENSE, true, 99);
        assertTrue(categoryService.createCustom(custom, SARI));
        assertFalse(custom.isDefault());
        assertTrue(custom.isOwnedBy(SARI));
        assertEquals(409, assertThrows(BusinessException.class, () -> categoryService.createCustom(
                new Category(0, "Makan & Minum", TransactionType.EXPENSE, false, 0), SARI)).getStatus().value());
        assertEquals(404, assertThrows(BusinessException.class,
                () -> categoryService.deleteCustom(custom.getCategoryId(), BUDI)).getStatus().value());
        assertTrue(categoryService.deleteCustom(custom.getCategoryId(), SARI));
    }

    // ---------- F05: skenario saldo ----------

    @Test
    void addExpenseAndIncomeChangeBalanceAndInsufficientIsRejected() {
        Wallet tunai = wallet(BUDI, "Tunai");
        Category makan = category(BUDI, TransactionType.EXPENSE, "Makan & Minum");
        Category gaji = category(BUDI, TransactionType.INCOME, "Gaji");
        double start = tunai.getBalance();

        Transaction e = transactionService.addTransaction(expenseReq(50_000, tunai, makan), BUDI);
        assertTrue(e.getTransactionId() > 0);
        assertEquals(start - 50_000, balance(BUDI, "Tunai"), 0.001);

        transactionService.addTransaction(incomeReq(200_000, tunai, gaji), BUDI);
        assertEquals(start + 150_000, balance(BUDI, "Tunai"), 0.001);

        assertEquals("Saldo dompet tidak mencukupi", assertThrows(BusinessException.class,
                () -> transactionService.addTransaction(expenseReq(start + 1_000_000, tunai, makan), BUDI)).getMessage());
        assertEquals(start + 150_000, balance(BUDI, "Tunai"), 0.001);
    }

    @Test
    void editChangingWalletAndTypeKeepsAllBalancesCorrect() {
        Wallet tunai = wallet(BUDI, "Tunai");
        Wallet bca = wallet(BUDI, "BCA");
        Category makan = category(BUDI, TransactionType.EXPENSE, "Makan & Minum");
        Category gaji = category(BUDI, TransactionType.INCOME, "Gaji");
        double t0 = tunai.getBalance();
        double b0 = bca.getBalance();

        Transaction created = transactionService.addTransaction(expenseReq(40_000, tunai, makan), BUDI);
        int id = created.getTransactionId();
        assertEquals(t0 - 40_000, balance(BUDI, "Tunai"), 0.001);

        // ganti nominal pada dompet yang sama
        transactionService.editTransaction(id, expenseReq(60_000, tunai, makan), BUDI);
        assertEquals(t0 - 60_000, balance(BUDI, "Tunai"), 0.001);

        // pindah dompet
        transactionService.editTransaction(id, expenseReq(60_000, bca, makan), BUDI);
        assertEquals(t0, balance(BUDI, "Tunai"), 0.001);
        assertEquals(b0 - 60_000, balance(BUDI, "BCA"), 0.001);

        // ganti jenis ke pemasukan
        transactionService.editTransaction(id, incomeReq(25_000, bca, gaji), BUDI);
        assertEquals(b0 + 25_000, balance(BUDI, "BCA"), 0.001);
        assertEquals(TransactionType.INCOME, transactionService.getTransaction(id, BUDI).getType());

        // hapus -> saldo kembali
        assertTrue(transactionService.deleteTransaction(id, BUDI));
        assertEquals(b0, balance(BUDI, "BCA"), 0.001);
        assertEquals(t0, balance(BUDI, "Tunai"), 0.001);
    }

    /**
     * AC7: edit yang gagal di tengah jalan (efek lama sudah dibatalkan, efek baru ditolak) harus membatalkan
     * semuanya. Dijalankan tanpa transaksi tes agar @Transactional milik service yang diuji; data dibersihkan manual.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void failedEditRollsBackTheRevert() {
        Wallet tunai = wallet(BUDI, "Tunai");
        Category makan = category(BUDI, TransactionType.EXPENSE, "Makan & Minum");
        double seedBalance = tunai.getBalance();
        int id = transactionService.addTransaction(expenseReq(10_000, tunai, makan), BUDI).getTransactionId();
        try {
            double afterAdd = balance(BUDI, "Tunai");
            assertEquals(seedBalance - 10_000, afterAdd, 0.001);

            assertThrows(BusinessException.class, () -> transactionService.editTransaction(
                    id, expenseReq(afterAdd + 10_000_000, tunai, makan), BUDI));

            assertEquals(afterAdd, balance(BUDI, "Tunai"), 0.001);
            assertEquals(10_000, transactionService.getTransaction(id, BUDI).getAmount(), 0.001);
        } finally {
            transactionService.deleteTransaction(id, BUDI);
        }
        assertEquals(seedBalance, balance(BUDI, "Tunai"), 0.001);
    }

    @Test
    void cannotUseAnotherUsersWalletCategoryOrTransaction() {
        Wallet sariWallet = walletService.getWallets(SARI).get(0);
        Category makan = category(BUDI, TransactionType.EXPENSE, "Makan & Minum");
        Category ngopi = category(BUDI, TransactionType.EXPENSE, "Ngopi");
        Wallet tunai = wallet(BUDI, "Tunai");

        assertEquals(404, assertThrows(BusinessException.class, () -> transactionService.addTransaction(
                expenseReq(1000, sariWallet, makan), BUDI)).getStatus().value());
        assertEquals(404, assertThrows(BusinessException.class, () -> transactionService.addTransaction(
                expenseReq(1000, tunai, ngopi), SARI)).getStatus().value());

        Transaction budiTrx = transactionService.search(BUDI, new TransactionFilter(DAY.minusDays(40), DAY,
                null, null, null, null, 1, 5)).get(0);
        assertEquals(404, assertThrows(BusinessException.class,
                () -> transactionService.deleteTransaction(budiTrx.getTransactionId(), SARI)).getStatus().value());
    }

    @Test
    void validationRejectsFutureDateAndWrongCategoryType() {
        Wallet tunai = wallet(BUDI, "Tunai");
        Category makan = category(BUDI, TransactionType.EXPENSE, "Makan & Minum");
        Category gaji = category(BUDI, TransactionType.INCOME, "Gaji");
        assertEquals("Tanggal tidak boleh di masa depan", assertThrows(BusinessException.class,
                () -> transactionService.addTransaction(new TransactionRequest(TransactionType.EXPENSE, 1000.0,
                        LocalDate.now().plusDays(1), makan.getCategoryId(), tunai.getWalletId(), null, null, null, null),
                        BUDI)).getMessage());
        assertEquals("Kategori tidak sesuai dengan jenis transaksi", assertThrows(BusinessException.class,
                () -> transactionService.addTransaction(expenseReq(1000, tunai, gaji), BUDI)).getMessage());
    }

    @Test
    void searchPageReturnsTotalsForWholeFilter() {
        var page = transactionService.searchPage(BUDI, new TransactionFilter(LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31), null, null, null, null, 1, 10));
        assertEquals(10, page.items().size());
        assertEquals(19, page.totalItems());
        assertEquals(2, page.totalPages());
        assertEquals(5_500_000, page.totalIncome(), 0.01);
        assertEquals(3_582_000, page.totalExpense(), 0.01);
        assertEquals("Tanggal awal tidak boleh setelah tanggal akhir", assertThrows(BusinessException.class,
                () -> transactionService.search(BUDI, new TransactionFilter(DAY, DAY.minusDays(1), null, null, null,
                        null, 1, 10))).getMessage());
    }

    // ---------- F08 / F09: anggaran & notifikasi ----------

    @Test
    void budgetNotificationsFireOnceWhenThresholdsAreCrossed() {
        Wallet bca = wallet(BUDI, "BCA");
        Category pendidikan = category(BUDI, TransactionType.EXPENSE, "Pendidikan");
        YearMonth oct = YearMonth.of(2026, 10);

        Budget b = new Budget(BUDI, new Category(pendidikan.getCategoryId(), null, null, false, 0), oct, 100_000);
        assertTrue(budgetService.createBudget(b));
        assertEquals("Anggaran Pendidikan", b.getName());
        assertEquals(0, b.getSpentAmount(), 0.001);
        int unreadBefore = notificationService.countUnread(BUDI);

        transactionService.addTransaction(expenseReq(70_000, bca, pendidikan), BUDI);   // 70%
        assertEquals(unreadBefore, notificationService.countUnread(BUDI));

        transactionService.addTransaction(expenseReq(15_000, bca, pendidikan), BUDI);   // 85% -> WARNING
        assertEquals(unreadBefore + 1, notificationService.countUnread(BUDI));
        Notification warn = notificationService.getRecent(BUDI, 1).get(0);
        assertEquals(Notification.WARNING, warn.getLevel());
        assertEquals("Anggaran Pendidikan Oktober 2026 sudah terpakai 85%", warn.getMessage());

        transactionService.addTransaction(expenseReq(5_000, bca, pendidikan), BUDI);    // 90% tidak berulang
        assertEquals(unreadBefore + 1, notificationService.countUnread(BUDI));

        transactionService.addTransaction(expenseReq(20_000, bca, pendidikan), BUDI);   // 110% -> DANGER
        assertEquals(unreadBefore + 2, notificationService.countUnread(BUDI));
        Notification danger = notificationService.getRecent(BUDI, 1).get(0);
        assertEquals(Notification.DANGER, danger.getLevel());
        assertEquals("Anggaran Pendidikan Oktober 2026 terlampaui (Rp 110.000 dari Rp 100.000)", danger.getMessage());
    }

    @Test
    void editAndDeleteExpenseRecalculateBudgetWithoutNotifying() {
        Wallet bca = wallet(BUDI, "BCA");
        Category pendidikan = category(BUDI, TransactionType.EXPENSE, "Pendidikan");
        Category hiburan = category(BUDI, TransactionType.EXPENSE, "Hiburan");
        YearMonth oct = YearMonth.of(2026, 10);
        Budget pend = new Budget(BUDI, new Category(pendidikan.getCategoryId(), null, null, false, 0), oct, 100_000);
        budgetService.createBudget(pend);

        int id = transactionService.addTransaction(expenseReq(30_000, bca, pendidikan), BUDI).getTransactionId();
        assertEquals(30_000, budgetService.getBudget(pend.getPlanId(), BUDI).getSpentAmount(), 0.001);

        transactionService.editTransaction(id, expenseReq(95_000, bca, pendidikan), BUDI);
        assertEquals(95_000, budgetService.getBudget(pend.getPlanId(), BUDI).getSpentAmount(), 0.001);

        // pindah kategori: anggaran lama berkurang
        transactionService.editTransaction(id, expenseReq(95_000, bca, hiburan), BUDI);
        assertEquals(0, budgetService.getBudget(pend.getPlanId(), BUDI).getSpentAmount(), 0.001);

        transactionService.deleteTransaction(id, BUDI);
        assertEquals(0, budgetService.getBudget(pend.getPlanId(), BUDI).getSpentAmount(), 0.001);
    }

    @Test
    void createBudgetComputesInitialSpentFromExistingTransactions() {
        Category makan = category(BUDI, TransactionType.EXPENSE, "Makan & Minum");
        Category kos = category(BUDI, TransactionType.EXPENSE, "Kos & Sewa");
        assertEquals(409, assertThrows(BusinessException.class, () -> budgetService.createBudget(
                new Budget(BUDI, new Category(makan.getCategoryId(), null, null, false, 0),
                        YearMonth.of(2026, 10), 5_000_000))).getStatus().value());

        Budget b = new Budget(BUDI, new Category(kos.getCategoryId(), null, null, false, 0),
                YearMonth.of(2026, 10), 2_000_000);
        budgetService.createBudget(b);
        assertEquals(1_300_000, b.getSpentAmount(), 0.01);

        assertEquals(404, assertThrows(BusinessException.class,
                () -> budgetService.deleteBudget(b.getPlanId(), SARI)).getStatus().value());
        assertTrue(budgetService.deleteBudget(b.getPlanId(), BUDI));
    }

    @Test
    void notificationsMarkAllRead() {
        assertTrue(notificationService.countUnread(BUDI) > 0);
        notificationService.markAllRead(BUDI);
        assertEquals(0, notificationService.countUnread(BUDI));
        assertEquals(0, notificationService.getUnread(BUDI).size());
    }

    // ---------- F10: tabungan ----------

    @Test
    void contributeReducesWalletAndAchievesGoal() {
        Wallet bca = wallet(BUDI, "BCA");
        SavingGoal laptop = savingGoalService.getGoals(BUDI).get(0);
        double remaining = laptop.getTargetAmount() - laptop.getCurrentAmount();
        double bcaBefore = bca.getBalance();

        assertTrue(savingGoalService.contribute(laptop.getPlanId(), 100_000, bca.getWalletId(), BUDI));
        assertEquals(bcaBefore - 100_000, balance(BUDI, "BCA"), 0.001);
        assertEquals(laptop.getCurrentAmount() + 100_000,
                savingGoalService.getGoal(laptop.getPlanId(), BUDI).getCurrentAmount(), 0.001);

        assertEquals("Nominal melebihi sisa target", assertThrows(BusinessException.class, () ->
                savingGoalService.contribute(laptop.getPlanId(), remaining, bca.getWalletId(), BUDI)).getMessage());
        assertEquals("Saldo dompet tidak mencukupi", assertThrows(BusinessException.class, () ->
                savingGoalService.contribute(laptop.getPlanId(), remaining - 100_000, bca.getWalletId(), BUDI))
                .getMessage());
    }

    @Test
    void goalReachingTargetBecomesAchievedAutomatically() {
        Wallet tunai = walletService.getWallets(SARI).get(0);
        walletDAO.update(rich(tunai));
        SavingGoal g = new SavingGoal(SARI, "Kecil", 50_000, LocalDate.now().plusMonths(2));
        savingGoalService.createGoal(g);
        savingGoalService.contribute(g.getPlanId(), 50_000, tunai.getWalletId(), SARI);
        assertEquals(GoalStatus.ACHIEVED, savingGoalService.getGoal(g.getPlanId(), SARI).getStatus());
        assertEquals("Target yang sudah tercapai tidak dapat dibatalkan", assertThrows(BusinessException.class,
                () -> savingGoalService.cancelGoal(g.getPlanId(), SARI)).getMessage());
    }

    @Test
    void cancelRefundsCollectedFundsToMainWallet() {
        Wallet main = walletService.getWallets(BUDI).get(0);
        SavingGoal laptop = savingGoalService.getGoals(BUDI).get(0);
        double mainBefore = main.getBalance();

        assertTrue(savingGoalService.cancelGoal(laptop.getPlanId(), BUDI));

        assertEquals(GoalStatus.CANCELLED, savingGoalService.getGoal(laptop.getPlanId(), BUDI).getStatus());
        assertEquals(mainBefore + laptop.getCurrentAmount(),
                walletService.getWallet(main.getWalletId(), BUDI).getBalance(), 0.001);
        assertEquals(0, savingGoalService.getGoal(laptop.getPlanId(), BUDI).getCurrentAmount(), 0.001);
        assertEquals("Target sudah dibatalkan", assertThrows(BusinessException.class,
                () -> savingGoalService.cancelGoal(laptop.getPlanId(), BUDI)).getMessage());
    }

    @Test
    void goalPastDeadlineIsRejectedOnCreate() {
        assertEquals("Tenggat harus di masa depan", assertThrows(BusinessException.class, () ->
                savingGoalService.createGoal(new SavingGoal(BUDI, "Lampau", 1000, LocalDate.now().minusDays(1))))
                .getMessage());
    }

    // ---------- F11–F14: laporan ----------

    @Test
    void categoryReportMatchesSeed() {
        CategoryReport r = reportService.createCategoryReport(BUDI, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        assertEquals(5_500_000, r.getTotalIncome(), 0.01);
        assertEquals(3_582_000, r.getTotalExpense(), 0.01);
        assertEquals(1_918_000, r.getNetBalance(), 0.01);
        assertEquals("Kos & Sewa", r.getTopExpenseCategory());
        assertEquals(36.29, r.getPercentage("Kos & Sewa"), 0.01);
        assertTrue(r.toCSV().startsWith("﻿Kategori;Total;Persentase"));
    }

    @Test
    void periodReportWeeklyAndComparison() {
        PeriodReport r = reportService.createPeriodReport(BUDI, PeriodType.WEEKLY,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        assertEquals("2026-W40", r.getRows().get(0).label());
        assertEquals(115_548.39, r.getAverageDailyExpense(), 0.01);
        assertNotNull(r.getComparisonWithPrevious());

        PeriodReport monthly = reportService.createPeriodReport(BUDI, PeriodType.MONTHLY,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 10, 31));
        assertEquals(6, monthly.getRows().size());
        assertEquals(0, monthly.getRows().get(0).expense(), 0.0);
    }

    @Test
    void reportsRejectInvalidRangeAndDashboardForEmptyMember() {
        assertThrows(BusinessException.class, () -> reportService.createCategoryReport(
                BUDI, LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 1)));
        DashboardSummary budi = reportService.getDashboardSummary(BUDI);
        assertEquals(1_333_000, budi.getTotalBalance(), 0.01);
        DashboardSummary sari = reportService.getDashboardSummary(SARI);
        assertEquals(0, sari.getTotalBalance(), 0.0);
        assertEquals(0, sari.getActiveBudgets());
        assertEquals(0, sari.getActiveGoals());
        assertTrue(Double.isNaN(sari.getSavingRate()));
    }

    private Wallet rich(Wallet w) {
        w.deposit(1_000_000);
        return w;
    }
}
