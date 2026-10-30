package com.spendwise.dao;

import com.spendwise.dto.TransactionFilter;
import com.spendwise.model.Admin;
import com.spendwise.model.Budget;
import com.spendwise.model.Category;
import com.spendwise.model.Expense;
import com.spendwise.model.GoalStatus;
import com.spendwise.model.Income;
import com.spendwise.model.Member;
import com.spendwise.model.Notification;
import com.spendwise.model.SavingGoal;
import com.spendwise.model.Transaction;
import com.spendwise.model.TransactionType;
import com.spendwise.model.User;
import com.spendwise.model.Wallet;
import com.spendwise.model.WalletType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tes integrasi terhadap MySQL/MariaDB yang sudah berisi schema.sql + seed.sql.
 * Jalankan: ./mvnw test -Dspendwise.it=true -Dspring.datasource.url=... -Dspring.datasource.username=...
 * Tiap tes di-rollback, jadi data seed tidak berubah.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserDAO.class, WalletDAO.class, CategoryDAO.class, TransactionDAO.class,
        BudgetDAO.class, NotificationDAO.class, SavingGoalDAO.class})
@EnabledIfSystemProperty(named = "spendwise.it", matches = "true")
class DaoIntegrationTest {

    private static final int BUDI = 2;
    private static final int SARI = 3;

    @Autowired UserDAO userDAO;
    @Autowired WalletDAO walletDAO;
    @Autowired CategoryDAO categoryDAO;
    @Autowired TransactionDAO transactionDAO;
    @Autowired BudgetDAO budgetDAO;
    @Autowired NotificationDAO notificationDAO;
    @Autowired SavingGoalDAO savingGoalDAO;

    private TransactionFilter october() {
        return new TransactionFilter(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31),
                null, null, null, null, 1, 100);
    }

    // ---------- UserDAO ----------

    @Test
    void userMapRowCreatesMemberOrAdminByRole() {
        User admin = userDAO.findByUsername("admin");
        User budi = userDAO.findByUsername("budi");
        assertInstanceOf(Admin.class, admin);
        assertInstanceOf(Member.class, budi);
        assertTrue(budi.checkPassword("member12345"));
        assertTrue(admin.checkPassword("admin12345"));
        assertNull(userDAO.findByUsername("tidakada"));
        assertEquals(3, userDAO.findAll().size());
    }

    @Test
    void userSaveUpdateExistsDelete() {
        Member m = new Member("baru_1", "x$y", "Orang Baru", "baru@mail.com");
        assertTrue(userDAO.save(m));
        assertTrue(m.getUserId() > 0);
        assertTrue(userDAO.existsByUsername("baru_1"));
        assertFalse(userDAO.existsByUsername("lain"));

        User loaded = userDAO.findById(m.getUserId());
        assertEquals("Orang Baru", loaded.getFullName());
        assertTrue(loaded.isActive());

        Member edited = new Member(m.getUserId(), "baru_1", "x$y", "Nama Diubah", "baru@mail.com", false,
                loaded.getCreatedAt());
        assertTrue(userDAO.update(edited));
        assertEquals("Nama Diubah", userDAO.findById(m.getUserId()).getFullName());
        assertFalse(userDAO.findById(m.getUserId()).isActive());

        assertTrue(userDAO.delete(m.getUserId()));
        assertFalse(userDAO.existsByUsername("baru_1"));
    }

    // ---------- WalletDAO ----------

    @Test
    void walletsOfBudiAndOwnership() {
        List<Wallet> wallets = walletDAO.findByOwner(BUDI);
        assertEquals(3, wallets.size());
        assertEquals("Tunai", wallets.get(0).getName());
        assertEquals(150000, wallets.get(0).getBalance(), 0.001);
        int id = wallets.get(0).getWalletId();
        assertNotNull(walletDAO.findByIdAndOwner(id, BUDI));
        assertNull(walletDAO.findByIdAndOwner(id, SARI));
    }

    @Test
    void walletSaveUpdatePersistsBalanceChanges() {
        Wallet w = new Wallet(SARI, "Dana", WalletType.E_WALLET, 50000);
        assertTrue(walletDAO.save(w));
        assertTrue(w.getWalletId() > 0);
        w.deposit(25000);
        assertTrue(walletDAO.update(w));
        assertEquals(75000, walletDAO.findById(w.getWalletId()).getBalance(), 0.001);
        assertTrue(walletDAO.delete(w.getWalletId()));
        assertNull(walletDAO.findById(w.getWalletId()));
    }

    @Test
    void duplicateWalletNameForSameOwnerIsRejectedByDatabase() {
        assertThrows(DataIntegrityViolationException.class,
                () -> walletDAO.save(new Wallet(BUDI, "Tunai", WalletType.CASH, 0)));
    }

    // ---------- CategoryDAO ----------

    @Test
    void availableCategoriesIncludeDefaultsAndOwn() {
        List<Category> expense = categoryDAO.findAvailable(BUDI, TransactionType.EXPENSE);
        assertEquals(10, expense.size());
        assertTrue(expense.stream().anyMatch(c -> c.getName().equals("Ngopi") && !c.isDefault()));
        assertTrue(categoryDAO.findAvailable(SARI, TransactionType.EXPENSE).stream()
                .noneMatch(c -> c.getName().equals("Ngopi")));
        assertEquals(16, categoryDAO.findAvailable(BUDI, null).size());
    }

    @Test
    void categorySaveCustomAndDefaultAndIsUsed() {
        Category custom = new Category(0, "Hobi", TransactionType.EXPENSE, false, BUDI);
        assertTrue(categoryDAO.save(custom));
        assertFalse(categoryDAO.isUsed(custom.getCategoryId()));
        assertTrue(categoryDAO.findById(custom.getCategoryId()).isOwnedBy(BUDI));

        Category def = new Category(0, "Donasi", TransactionType.EXPENSE, true, 0);
        assertTrue(categoryDAO.save(def));
        assertEquals(0, categoryDAO.findById(def.getCategoryId()).getOwnerId());

        Category makan = categoryDAO.findAvailable(BUDI, TransactionType.EXPENSE).stream()
                .filter(c -> c.getName().equals("Makan & Minum")).findFirst().orElseThrow();
        assertTrue(categoryDAO.isUsed(makan.getCategoryId()));
    }

    // ---------- TransactionDAO ----------

    @Test
    void transactionFilterByPeriodAndTotalsMatchSeed() {
        TransactionFilter f = october();
        int count = transactionDAO.countByFilter(BUDI, f);
        assertEquals(19, count);
        assertEquals(5_500_000, transactionDAO.sumByFilter(BUDI, f, TransactionType.INCOME), 0.01);
        assertEquals(3_582_000, transactionDAO.sumByFilter(BUDI, f, TransactionType.EXPENSE), 0.01);
        assertEquals(count, transactionDAO.findByFilter(BUDI, f).size());
        assertEquals(count, transactionDAO.findByPeriod(BUDI, f.from(), f.to()).size());
    }

    @Test
    void transactionPaginationAndOrdering() {
        TransactionFilter page1 = new TransactionFilter(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31),
                null, null, null, null, 1, 10);
        TransactionFilter page2 = new TransactionFilter(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31),
                null, null, null, null, 2, 10);
        List<Transaction> a = transactionDAO.findByFilter(BUDI, page1);
        List<Transaction> b = transactionDAO.findByFilter(BUDI, page2);
        assertEquals(10, a.size());
        assertEquals(9, b.size());
        assertFalse(a.get(0).getDate().isBefore(a.get(a.size() - 1).getDate()));
    }

    @Test
    void transactionFilterByTypeAndSearchAndIsolationBetweenUsers() {
        TransactionFilter expenses = new TransactionFilter(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31),
                TransactionType.EXPENSE, null, null, null, 1, 100);
        List<Transaction> list = transactionDAO.findByFilter(BUDI, expenses);
        assertTrue(list.stream().allMatch(t -> t.getType() == TransactionType.EXPENSE));
        assertTrue(list.stream().allMatch(t -> t instanceof Expense));

        TransactionFilter injection = new TransactionFilter(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 31),
                null, null, null, "' OR '1'='1", 1, 100);
        assertEquals(0, transactionDAO.findByFilter(BUDI, injection).size());

        assertEquals(0, transactionDAO.countByFilter(SARI, october()));
    }

    @Test
    void sumByFilterWithConflictingTypeIsZero() {
        TransactionFilter onlyIncome = new TransactionFilter(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31),
                TransactionType.INCOME, null, null, null, 1, 10);
        assertEquals(0, transactionDAO.sumByFilter(BUDI, onlyIncome, TransactionType.EXPENSE), 0.0);
    }

    @Test
    void transactionSaveUpdateDeleteRoundTripForBothSubclasses() {
        Wallet w = walletDAO.findByOwner(BUDI).get(0);
        Category food = categoryDAO.findAvailable(BUDI, TransactionType.EXPENSE).get(0);
        Category salary = categoryDAO.findAvailable(BUDI, TransactionType.INCOME).get(0);

        Expense e = new Expense(12345, LocalDate.of(2026, 10, 29), "Tes", food, w, "QRIS", true);
        e.setUserId(BUDI);
        assertTrue(transactionDAO.save(e));
        Transaction loaded = transactionDAO.findById(e.getTransactionId());
        assertInstanceOf(Expense.class, loaded);
        assertEquals("QRIS", loaded.getPaymentMethod());
        assertTrue(loaded.isRecurring());
        assertNull(loaded.getSource());

        Income in = new Income(500, LocalDate.of(2026, 10, 29), "Tes", salary, w, "Kantor");
        in.setUserId(BUDI);
        assertTrue(transactionDAO.save(in));
        Transaction loadedIn = transactionDAO.findById(in.getTransactionId());
        assertInstanceOf(Income.class, loadedIn);
        assertEquals("Kantor", loadedIn.getSource());
        assertFalse(loadedIn.isRecurring());

        Expense edited = new Expense(999, LocalDate.of(2026, 10, 28), "Ubah", food, w, "Tunai", false);
        edited.setUserId(BUDI);
        edited.setTransactionId(e.getTransactionId());
        assertTrue(transactionDAO.update(edited));
        assertEquals(999, transactionDAO.findById(e.getTransactionId()).getAmount(), 0.001);

        assertTrue(transactionDAO.delete(e.getTransactionId()));
        assertNull(transactionDAO.findById(e.getTransactionId()));
    }

    @Test
    void countByWalletAndSumExpense() {
        Wallet w = walletDAO.findByOwner(BUDI).get(0);
        assertTrue(transactionDAO.countByWallet(w.getWalletId()) > 0);
        Category makan = categoryDAO.findAvailable(BUDI, TransactionType.EXPENSE).stream()
                .filter(c -> c.getName().equals("Makan & Minum")).findFirst().orElseThrow();
        assertEquals(1_015_000, transactionDAO.sumExpense(BUDI, makan.getCategoryId(), YearMonth.of(2026, 10)), 0.01);
        assertEquals(0, transactionDAO.sumExpense(SARI, makan.getCategoryId(), YearMonth.of(2026, 10)), 0.0);
    }

    // ---------- BudgetDAO ----------

    @Test
    void budgetsOfOctober() {
        List<Budget> list = budgetDAO.findByUserAndMonth(BUDI, YearMonth.of(2026, 10));
        assertEquals(4, list.size());
        Budget makan = list.stream().filter(b -> b.getCategory().getName().equals("Makan & Minum"))
                .findFirst().orElseThrow();
        assertEquals("WARNING", makan.getStatus());
        assertEquals(1_200_000, makan.getTargetAmount(), 0.01);
        assertNotNull(budgetDAO.findByCategoryAndMonth(BUDI, makan.getCategory().getCategoryId(), YearMonth.of(2026, 10)));
        assertNull(budgetDAO.findByCategoryAndMonth(SARI, makan.getCategory().getCategoryId(), YearMonth.of(2026, 10)));
        assertEquals(1, list.stream().filter(Budget::isOverBudget).count());
    }

    @Test
    void budgetSaveUpdateAndUniquePerCategoryMonth() {
        Category transport = categoryDAO.findAvailable(BUDI, TransactionType.EXPENSE).stream()
                .filter(c -> c.getName().equals("Pendidikan")).findFirst().orElseThrow();
        Budget b = new Budget(BUDI, transport, YearMonth.of(2026, 11), 500_000);
        assertTrue(budgetDAO.save(b));
        assertTrue(b.getPlanId() > 0);
        b.addSpending(100_000);
        b.setTargetAmount(600_000);
        assertTrue(budgetDAO.update(b));
        Budget loaded = budgetDAO.findById(b.getPlanId());
        assertEquals(100_000, loaded.getSpentAmount(), 0.001);
        assertEquals(600_000, loaded.getTargetAmount(), 0.001);
        assertEquals("Anggaran Pendidikan", loaded.getName());

        Budget dup = new Budget(BUDI, transport, YearMonth.of(2026, 11), 1000);
        assertThrows(DataIntegrityViolationException.class, () -> budgetDAO.save(dup));
    }

    // ---------- NotificationDAO ----------

    @Test
    void notificationsUnreadRecentAndMarkAllRead() {
        assertEquals(2, notificationDAO.findUnread(BUDI).size());
        assertEquals(2, notificationDAO.countUnread(BUDI));
        assertEquals(3, notificationDAO.findRecent(BUDI, 20).size());
        assertEquals(1, notificationDAO.findRecent(BUDI, 1).size());

        Notification n = new Notification(BUDI, "Tes", Notification.WARNING);
        assertTrue(notificationDAO.save(n));
        assertEquals(3, notificationDAO.countUnread(BUDI));

        notificationDAO.markAllRead(BUDI);
        assertEquals(0, notificationDAO.countUnread(BUDI));
        assertEquals(0, notificationDAO.findUnread(BUDI).size());
        assertEquals(0, notificationDAO.countUnread(SARI));
    }

    // ---------- SavingGoalDAO ----------

    @Test
    void goalsOrderedActiveFirstAndUpdatePersists() {
        List<SavingGoal> goals = savingGoalDAO.findByUser(BUDI);
        assertEquals(2, goals.size());
        assertEquals(GoalStatus.ACTIVE, goals.get(0).getStatus());
        assertEquals(GoalStatus.ACHIEVED, goals.get(1).getStatus());

        SavingGoal laptop = goals.get(0);
        assertEquals(2_500_000, laptop.getCurrentAmount(), 0.01);
        assertEquals(31.25, laptop.getProgressPercentage(), 0.001);

        SavingGoal fresh = new SavingGoal(SARI, "Liburan", 3_000_000, LocalDate.of(2027, 12, 31));
        assertTrue(savingGoalDAO.save(fresh));
        fresh.setStatus(GoalStatus.CANCELLED);
        assertTrue(savingGoalDAO.update(fresh));
        assertEquals(GoalStatus.CANCELLED, savingGoalDAO.findById(fresh.getPlanId()).getStatus());
        assertEquals(1, savingGoalDAO.findByUser(SARI).size());
    }
}
