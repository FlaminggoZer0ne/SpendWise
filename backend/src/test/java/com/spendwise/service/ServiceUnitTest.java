package com.spendwise.service;

import com.spendwise.core.BusinessException;
import com.spendwise.core.ForbiddenException;
import com.spendwise.core.UnauthorizedException;
import com.spendwise.dao.BudgetDAO;
import com.spendwise.dao.CategoryDAO;
import com.spendwise.dao.NotificationDAO;
import com.spendwise.dao.TransactionDAO;
import com.spendwise.dao.UserDAO;
import com.spendwise.dao.WalletDAO;
import com.spendwise.model.Budget;
import com.spendwise.model.BudgetAlertListener;
import com.spendwise.model.Category;
import com.spendwise.model.Expense;
import com.spendwise.model.Member;
import com.spendwise.model.Notification;
import com.spendwise.model.TransactionType;
import com.spendwise.model.User;
import com.spendwise.model.Wallet;
import com.spendwise.model.WalletType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceUnitTest {

    private final Category food = new Category(6, "Makan & Minum", TransactionType.EXPENSE, true, 0);

    // ---------- AuthService ----------

    @Test
    void registerCreatesMemberWithCashWallet() {
        UserDAO userDAO = mock(UserDAO.class);
        WalletDAO walletDAO = mock(WalletDAO.class);
        AuthService auth = new AuthService(userDAO, walletDAO);
        when(userDAO.existsByUsername("sari")).thenReturn(false);

        Member m = auth.register("sari", "member12345", " Sari ", "sari@mail.com");

        assertEquals("MEMBER", m.getRole());
        assertEquals("Sari", m.getFullName());
        assertTrue(m.checkPassword("member12345"));
        ArgumentCaptor<Wallet> wallet = ArgumentCaptor.forClass(Wallet.class);
        verify(walletDAO).save(wallet.capture());
        assertEquals("Tunai", wallet.getValue().getName());
        assertEquals(WalletType.CASH, wallet.getValue().getWalletType());
        assertEquals(0, wallet.getValue().getBalance(), 0.0);
    }

    @Test
    void registerRejectsDuplicateUsername() {
        UserDAO userDAO = mock(UserDAO.class);
        when(userDAO.existsByUsername("budi")).thenReturn(true);
        AuthService auth = new AuthService(userDAO, mock(WalletDAO.class));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> auth.register("budi", "member12345", "Budi", "b@mail.com"));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("Username sudah dipakai", ex.getMessage());
    }

    @Test
    void loginUsesGenericMessageAndRejectsInactive() {
        UserDAO userDAO = mock(UserDAO.class);
        AuthService auth = new AuthService(userDAO, mock(WalletDAO.class));
        Member budi = new Member("budi", com.spendwise.core.PasswordHasher.hash("member12345"), "Budi", "b@mail.com");
        when(userDAO.findByUsername("budi")).thenReturn(budi);

        assertSame(budi, auth.login("budi", "member12345"));
        assertEquals("Username atau password salah",
                assertThrows(UnauthorizedException.class, () -> auth.login("budi", "salah")).getMessage());
        assertEquals("Username atau password salah",
                assertThrows(UnauthorizedException.class, () -> auth.login("hantu", "x")).getMessage());

        budi.setActive(false);
        assertEquals("Akun Anda dinonaktifkan. Hubungi admin.",
                assertThrows(ForbiddenException.class, () -> auth.login("budi", "member12345")).getMessage());
    }

    @Test
    void changePasswordPersistsNewHash() {
        UserDAO userDAO = mock(UserDAO.class);
        AuthService auth = new AuthService(userDAO, mock(WalletDAO.class));
        User budi = new Member(2, "budi", com.spendwise.core.PasswordHasher.hash("member12345"), "Budi",
                "b@mail.com", true, null);
        when(userDAO.findById(2)).thenReturn(budi);
        when(userDAO.update(budi)).thenReturn(true);

        assertTrue(auth.changePassword(2, "member12345", "rahasia2026"));
        assertTrue(budi.checkPassword("rahasia2026"));
        assertThrows(BusinessException.class, () -> auth.changePassword(2, "salah", "rahasia9999"));
    }

    // ---------- WalletService ----------

    @Test
    void transferRejectsInsufficientBalanceWithoutPersisting() {
        WalletDAO walletDAO = mock(WalletDAO.class);
        WalletService service = new WalletService(walletDAO, mock(TransactionDAO.class));
        when(walletDAO.findByIdAndOwner(1, 2)).thenReturn(new Wallet(1, 2, "A", WalletType.CASH, 100));
        when(walletDAO.findByIdAndOwner(2, 2)).thenReturn(new Wallet(2, 2, "B", WalletType.BANK, 0));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.transfer(1, 2, 500, 2));
        assertEquals("Saldo dompet tidak mencukupi", ex.getMessage());
        verify(walletDAO, never()).update(any());
    }

    @Test
    void transferRejectsSameWalletAndForeignWallet() {
        WalletDAO walletDAO = mock(WalletDAO.class);
        WalletService service = new WalletService(walletDAO, mock(TransactionDAO.class));
        assertEquals("Dompet asal dan tujuan tidak boleh sama",
                assertThrows(BusinessException.class, () -> service.transfer(1, 1, 10, 2)).getMessage());
        when(walletDAO.findByIdAndOwner(1, 2)).thenReturn(new Wallet(1, 2, "A", WalletType.CASH, 100));
        BusinessException ex = assertThrows(BusinessException.class, () -> service.transfer(1, 9, 10, 2));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void deleteWalletRules() {
        WalletDAO walletDAO = mock(WalletDAO.class);
        TransactionDAO transactionDAO = mock(TransactionDAO.class);
        WalletService service = new WalletService(walletDAO, transactionDAO);
        Wallet w = new Wallet(1, 2, "A", WalletType.CASH, 0);
        when(walletDAO.findByIdAndOwner(1, 2)).thenReturn(w);

        when(transactionDAO.countByWallet(1)).thenReturn(3);
        assertEquals("Dompet masih memiliki transaksi",
                assertThrows(BusinessException.class, () -> service.deleteWallet(1, 2)).getMessage());

        when(transactionDAO.countByWallet(1)).thenReturn(0);
        when(walletDAO.findByOwner(2)).thenReturn(List.of(w));
        assertEquals("Minimal harus ada satu dompet",
                assertThrows(BusinessException.class, () -> service.deleteWallet(1, 2)).getMessage());

        when(walletDAO.findByOwner(2)).thenReturn(List.of(w, new Wallet(2, 2, "B", WalletType.BANK, 0)));
        when(walletDAO.delete(1)).thenReturn(true);
        assertTrue(service.deleteWallet(1, 2));
    }

    @Test
    void createWalletRejectsDuplicateNameIgnoringCase() {
        WalletDAO walletDAO = mock(WalletDAO.class);
        WalletService service = new WalletService(walletDAO, mock(TransactionDAO.class));
        when(walletDAO.findByOwner(2)).thenReturn(List.of(new Wallet(1, 2, "Tunai", WalletType.CASH, 0)));
        Wallet dup = new Wallet(2, " tunai ", WalletType.CASH, 0);
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(BusinessException.class, () -> service.createWallet(dup)).getStatus());
    }

    // ---------- BudgetService (Observer) ----------

    private BudgetService budgetService(BudgetDAO budgetDAO) {
        return new BudgetService(budgetDAO, mock(CategoryDAO.class), mock(TransactionDAO.class));
    }

    private Expense expense(double amount) {
        Expense e = new Expense(amount, LocalDate.of(2026, 10, 15), "x", food,
                new Wallet(1, 2, "A", WalletType.CASH, 0), null, false);
        e.setUserId(2);
        return e;
    }

    private Budget budget(double spent) {
        return new Budget(1, 2, "Anggaran Makan & Minum", food, YearMonth.of(2026, 10), 1000, spent);
    }

    @Test
    void checkBudgetNotifiesOnlyWhenThresholdCrossed() {
        BudgetDAO budgetDAO = mock(BudgetDAO.class);
        BudgetService service = budgetService(budgetDAO);
        BudgetAlertListener listener = mock(BudgetAlertListener.class);
        service.addListener(listener);

        Budget b = budget(700);
        when(budgetDAO.findByCategoryAndMonth(2, 6, YearMonth.of(2026, 10))).thenReturn(b);

        service.checkBudget(expense(50));   // 75%
        verify(listener, never()).onBudgetWarning(any(), org.mockito.ArgumentMatchers.anyDouble());

        service.checkBudget(expense(100));  // 85% -> warning
        verify(listener).onBudgetWarning(b, 85.0);

        service.checkBudget(expense(50));   // 90% -> tidak berulang
        verify(listener).onBudgetWarning(any(), org.mockito.ArgumentMatchers.anyDouble());

        service.checkBudget(expense(100));  // 100% -> exceeded
        verify(listener).onBudgetExceeded(b);

        service.checkBudget(expense(100));  // 110% -> tidak berulang
        verify(listener).onBudgetExceeded(any());
    }

    @Test
    void jumpingStraightPastHundredSendsOnlyExceeded() {
        BudgetDAO budgetDAO = mock(BudgetDAO.class);
        BudgetService service = budgetService(budgetDAO);
        BudgetAlertListener listener = mock(BudgetAlertListener.class);
        service.addListener(listener);
        Budget b = budget(100);
        when(budgetDAO.findByCategoryAndMonth(2, 6, YearMonth.of(2026, 10))).thenReturn(b);

        service.checkBudget(expense(950));

        verify(listener).onBudgetExceeded(b);
        verify(listener, never()).onBudgetWarning(any(), org.mockito.ArgumentMatchers.anyDouble());
    }

    @Test
    void checkBudgetWithoutBudgetDoesNothing() {
        BudgetDAO budgetDAO = mock(BudgetDAO.class);
        budgetService(budgetDAO).checkBudget(expense(50));
        verify(budgetDAO, never()).update(any());
    }

    @Test
    void recalculateResetsSpentWithoutNotifying() {
        BudgetDAO budgetDAO = mock(BudgetDAO.class);
        TransactionDAO transactionDAO = mock(TransactionDAO.class);
        BudgetService service = new BudgetService(budgetDAO, mock(CategoryDAO.class), transactionDAO);
        BudgetAlertListener listener = mock(BudgetAlertListener.class);
        service.addListener(listener);
        Budget b = budget(950);
        when(budgetDAO.findByCategoryAndMonth(2, 6, YearMonth.of(2026, 10))).thenReturn(b);
        when(transactionDAO.sumExpense(2, 6, YearMonth.of(2026, 10))).thenReturn(1200.0);

        service.recalculate(2, 6, YearMonth.of(2026, 10));

        assertEquals(1200, b.getSpentAmount(), 0.001);
        verify(budgetDAO).update(b);
        verify(listener, never()).onBudgetExceeded(any());
        verify(listener, never()).onBudgetWarning(any(), org.mockito.ArgumentMatchers.anyDouble());
    }

    @Test
    void createBudgetRejectsIncomeCategoryAndDuplicate() {
        BudgetDAO budgetDAO = mock(BudgetDAO.class);
        CategoryDAO categoryDAO = mock(CategoryDAO.class);
        BudgetService service = new BudgetService(budgetDAO, categoryDAO, mock(TransactionDAO.class));
        Category salary = new Category(1, "Gaji", TransactionType.INCOME, true, 0);
        when(categoryDAO.findById(1)).thenReturn(salary);
        when(categoryDAO.findById(6)).thenReturn(food);

        Budget income = new Budget(2, new Category(1, "Gaji", TransactionType.INCOME, true, 0),
                YearMonth.of(2026, 10), 1000);
        assertEquals("Anggaran hanya untuk kategori pengeluaran",
                assertThrows(BusinessException.class, () -> service.createBudget(income)).getMessage());

        when(budgetDAO.findByCategoryAndMonth(anyInt(), anyInt(), any())).thenReturn(budget(0));
        Budget dup = new Budget(2, food, YearMonth.of(2026, 10), 1000);
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(BusinessException.class, () -> service.createBudget(dup)).getStatus());
    }

    // ---------- NotificationService ----------

    @Test
    void notificationMessagesMatchPrdFormat() {
        NotificationDAO dao = mock(NotificationDAO.class);
        NotificationService service = new NotificationService(dao);
        Budget warn = new Budget(1, 2, "Anggaran Makan & Minum", food, YearMonth.of(2026, 10), 1_200_000, 1_020_000);
        Budget over = new Budget(1, 2, "Anggaran Makan & Minum", food, YearMonth.of(2026, 10), 1_200_000, 1_250_000);

        service.onBudgetWarning(warn, 85.0);
        service.onBudgetExceeded(over);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(dao, org.mockito.Mockito.times(2)).save(captor.capture());
        Notification first = captor.getAllValues().get(0);
        Notification second = captor.getAllValues().get(1);
        assertEquals("Anggaran Makan & Minum Oktober 2026 sudah terpakai 85%", first.getMessage());
        assertEquals(Notification.WARNING, first.getLevel());
        assertEquals(2, first.getUserId());
        assertEquals("Anggaran Makan & Minum Oktober 2026 terlampaui (Rp 1.250.000 dari Rp 1.200.000)",
                second.getMessage());
        assertEquals(Notification.DANGER, second.getLevel());
        assertFalse(second.isRead());
    }
}
