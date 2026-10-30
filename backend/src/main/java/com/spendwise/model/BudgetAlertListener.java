package com.spendwise.model;

/** Observer untuk peristiwa anggaran. */
public interface BudgetAlertListener {

    void onBudgetWarning(Budget b, double percent);

    void onBudgetExceeded(Budget b);
}
