package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Ringkasan dashboard (read-only). */
public class DashboardSummary {

    private final double totalBalance;
    private final double monthIncome;
    private final double monthExpense;
    private final int activeBudgets;
    private final int overBudgetCount;
    private final int activeGoals;

    public DashboardSummary(double totalBalance, double monthIncome, double monthExpense,
                            int activeBudgets, int overBudgetCount, int activeGoals) {
        this.totalBalance = totalBalance;
        this.monthIncome = monthIncome;
        this.monthExpense = monthExpense;
        this.activeBudgets = activeBudgets;
        this.overBudgetCount = overBudgetCount;
        this.activeGoals = activeGoals;
    }

    /**
     * BR-09: (pemasukan - pengeluaran) / pemasukan x 100.
     *
     * @return NaN jika pemasukan bulan ini 0
     */
    @JsonIgnore
    public double getSavingRate() {
        if (monthIncome <= 0) {
            return Double.NaN;
        }
        return Math.round((monthIncome - monthExpense) / monthIncome * 10000.0) / 100.0;
    }

    /** Null jika pemasukan 0 (JSON: savingRate). */
    @JsonProperty("savingRate")
    public Double savingRateOrNull() {
        double v = getSavingRate();
        return Double.isNaN(v) ? null : v;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public double getMonthIncome() {
        return monthIncome;
    }

    public double getMonthExpense() {
        return monthExpense;
    }

    public int getActiveBudgets() {
        return activeBudgets;
    }

    public int getOverBudgetCount() {
        return overBudgetCount;
    }

    public int getActiveGoals() {
        return activeGoals;
    }
}
