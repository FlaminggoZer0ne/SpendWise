package com.spendwise.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.spendwise.core.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialPlanTest {

    private final Category food = new Category(6, "Makan & Minum", TransactionType.EXPENSE, true, 0);

    private Budget budget(double limit, double spent) {
        return new Budget(1, 2, "Anggaran Makan & Minum", food, YearMonth.of(2026, 10), limit, spent);
    }

    @Test
    void budgetProgressAndStatusThresholds() {
        assertEquals("SAFE", budget(1000, 790).getStatus());
        assertEquals("WARNING", budget(1000, 800).getStatus());
        assertEquals("WARNING", budget(1000, 999).getStatus());
        assertEquals("EXCEEDED", budget(1000, 1000).getStatus());
        assertEquals(1.085, budget(1000, 1085).getProgress(), 0.0001);
    }

    @Test
    void budgetPercentageAndRemainingMatchApiExample() {
        Budget b = budget(1_200_000, 1_015_000);
        assertEquals(84.58, b.getProgressPercentage(), 0.001);
        assertEquals(185_000, b.getRemainingAmount(), 0.01);
    }

    @Test
    void budgetAddSpending() {
        Budget b = budget(1000, 0);
        b.addSpending(250.5);
        assertEquals(250.5, b.getSpentAmount(), 0.001);
        assertFalse(b.isOverBudget());
        assertTrue(b.isNearLimit(0.2));
        assertThrows(BusinessException.class, () -> b.addSpending(0));
    }

    @Test
    void budgetNameDerivedFromCategory() {
        assertEquals("Anggaran Makan & Minum", new Budget(2, food, YearMonth.of(2026, 10), 100).getName());
    }

    @Test
    void budgetValidate() {
        assertTrue(new Budget(2, food, YearMonth.of(2026, 10), 100).validate());
        assertThrows(BusinessException.class, () -> new Budget(2, food, YearMonth.of(2026, 10), 0).validate());
        assertThrows(BusinessException.class, () -> new Budget(2, null, YearMonth.of(2026, 10), 10).validate());
    }

    @Test
    void budgetJsonRequest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Budget b = mapper.readValue("{\"categoryId\":6,\"month\":\"2026-10\",\"targetAmount\":1200000}", Budget.class);
        assertEquals(6, b.getCategoryId());
        assertEquals(YearMonth.of(2026, 10), b.getPeriod());
        assertEquals(1_200_000, b.getTargetAmount(), 0.001);
    }

    @Test
    void jsonRequestCannotOverrideProtectedFields() throws Exception {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        Budget b = mapper.readValue(
                "{\"categoryId\":6,\"month\":\"2026-10\",\"targetAmount\":10,\"spentAmount\":5,\"userId\":99,\"planId\":7}",
                Budget.class);
        assertEquals(0, b.getSpentAmount(), 0.0);
        assertEquals(0, b.getUserId());
        assertEquals(0, b.getPlanId());

        SavingGoal g = mapper.readValue(
                "{\"name\":\"x\",\"targetAmount\":100,\"deadline\":\"2030-01-01\",\"currentAmount\":90,\"status\":\"ACHIEVED\",\"userId\":9}",
                SavingGoal.class);
        assertEquals(0, g.getCurrentAmount(), 0.0);
        assertEquals(GoalStatus.ACTIVE, g.getStatus());
        assertEquals(0, g.getUserId());

        Category c = mapper.readValue(
                "{\"name\":\"Donasi\",\"type\":\"EXPENSE\",\"isDefault\":true,\"default\":true,\"ownerId\":5,\"categoryId\":3}",
                Category.class);
        assertEquals("Donasi", c.getName());
        assertEquals(TransactionType.EXPENSE, c.getType());
        assertFalse(c.isDefault());
        assertEquals(0, c.getOwnerId());
        assertEquals(0, c.getCategoryId());
    }

    private SavingGoal goal(double target, double current) {
        return new SavingGoal(1, 2, "Laptop Baru", target, current, LocalDate.of(2027, 6, 30), GoalStatus.ACTIVE);
    }

    private Wallet wallet(double balance) {
        return new Wallet(1, 2, "BCA", WalletType.BANK, balance);
    }

    @Test
    void goalProgressIsCappedAtOne() {
        assertEquals(0.3125, goal(8_000_000, 2_500_000).getProgress(), 0.00001);
        assertEquals(1.0, goal(100, 150).getProgress(), 0.00001);
    }

    @Test
    void goalRecommendedMonthlySavingMatchesApiExample() {
        SavingGoal g = goal(8_000_000, 2_500_000);
        LocalDate today = LocalDate.of(2026, 10, 30);
        assertEquals(243, g.getDaysRemaining(today));
        assertEquals(687_500, g.getRecommendedMonthlySaving(today), 0.01);
    }

    @Test
    void goalRecommendedSavingRoundsMonthsUp() {
        SavingGoal g = new SavingGoal(1, 2, "x", 900, 0, LocalDate.of(2026, 12, 15), GoalStatus.ACTIVE);
        // 1 Okt -> 15 Des = 2 bulan + 14 hari => 3 bulan
        assertEquals(300, g.getRecommendedMonthlySaving(LocalDate.of(2026, 10, 1)), 0.01);
    }

    @Test
    void goalPastDeadlineUsesWholeRemaining() {
        SavingGoal g = new SavingGoal(1, 2, "x", 900, 400, LocalDate.of(2026, 1, 1), GoalStatus.ACTIVE);
        LocalDate today = LocalDate.of(2026, 10, 30);
        assertEquals(0, g.getDaysRemaining(today));
        assertEquals(500, g.getRecommendedMonthlySaving(today), 0.01);
    }

    @Test
    void contributionMovesMoneyFromWalletToGoal() {
        SavingGoal g = goal(1000, 0);
        Wallet w = wallet(600);
        assertTrue(g.addContribution(400, w));
        assertEquals(400, g.getCurrentAmount(), 0.001);
        assertEquals(200, w.getBalance(), 0.001);
        assertEquals(GoalStatus.ACTIVE, g.getStatus());
    }

    @Test
    void contributionFailsWhenWalletInsufficient() {
        SavingGoal g = goal(1000, 0);
        Wallet w = wallet(100);
        assertFalse(g.addContribution(400, w));
        assertEquals(0, g.getCurrentAmount(), 0.001);
        assertEquals(100, w.getBalance(), 0.001);
    }

    @Test
    void contributionReachingTargetMarksAchieved() {
        SavingGoal g = goal(1000, 600);
        assertTrue(g.addContribution(400, wallet(1000)));
        assertTrue(g.isCompleted());
        assertEquals(GoalStatus.ACHIEVED, g.getStatus());
        assertThrows(BusinessException.class, () -> g.addContribution(1, wallet(10)));
    }

    @Test
    void contributionAboveRemainingIsRejected() {
        SavingGoal g = goal(1000, 600);
        assertEquals("Nominal melebihi sisa target",
                assertThrows(BusinessException.class, () -> g.addContribution(401, wallet(1000))).getMessage());
    }

    @Test
    void withdrawFund() {
        SavingGoal g = goal(1000, 300);
        assertFalse(g.withdrawFund(301));
        assertTrue(g.withdrawFund(300));
        assertEquals(0, g.getCurrentAmount(), 0.001);
    }

    @Test
    void goalValidateRequiresFutureDeadline() {
        assertTrue(new SavingGoal(2, "x", 100, LocalDate.now().plusDays(5)).validate());
        assertEquals("Tenggat harus di masa depan", assertThrows(BusinessException.class,
                () -> new SavingGoal(2, "x", 100, LocalDate.now()).validate()).getMessage());
    }

    @Test
    void notificationMarkAsRead() {
        Notification n = new Notification(2, "Pesan", Notification.WARNING);
        assertFalse(n.isRead());
        n.markAsRead();
        assertTrue(n.isRead());
        assertEquals("Pesan", n.getMessage());
    }
}
