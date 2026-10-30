package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.spendwise.core.BusinessException;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/** Anggaran satu kategori pengeluaran per bulan; targetAmount = batas anggaran. */
public class Budget extends FinancialPlan {

    public static final double WARNING_THRESHOLD = 0.8;

    private Category category;
    private YearMonth period;
    private double spentAmount;

    /** Untuk deserialisasi JSON request pembuatan/ubah anggaran. */
    private Budget() {
    }

    public Budget(int userId, Category category, YearMonth period, double limit) {
        this.userId = userId;
        this.category = category;
        this.period = period;
        this.targetAmount = limit;
        this.name = category == null ? null : "Anggaran " + category.getName();
    }

    /** Konstruktor lengkap untuk DAO saat membaca dari database. */
    public Budget(int planId, int userId, String name, Category category, YearMonth period,
                  double limit, double spentAmount) {
        this.planId = planId;
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.period = period;
        this.targetAmount = limit;
        this.spentAmount = round2(spentAmount);
    }

    public void addSpending(double amount) {
        if (amount <= 0) {
            throw new BusinessException("Nominal harus lebih dari 0");
        }
        spentAmount = round2(spentAmount + amount);
    }

    public boolean isOverBudget() {
        return getProgress() >= 1.0;
    }

    public boolean isNearLimit(double threshold) {
        return getProgress() >= threshold;
    }

    @Override
    public double getProgress() {
        return targetAmount <= 0 ? 0 : spentAmount / targetAmount;
    }

    @Override
    public boolean isCompleted() {
        return isOverBudget();
    }

    /** SAFE | WARNING | EXCEEDED (BR-05). */
    public String getStatus() {
        if (isOverBudget()) {
            return "EXCEEDED";
        }
        return isNearLimit(WARNING_THRESHOLD) ? "WARNING" : "SAFE";
    }

    @Override
    public boolean validate() {
        super.validate();
        if (category == null) {
            throw new BusinessException("Kategori wajib dipilih");
        }
        if (period == null) {
            throw new BusinessException("Bulan anggaran wajib diisi");
        }
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public YearMonth getPeriod() {
        return period;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public double getSpentAmount() {
        return spentAmount;
    }

    /** Dipakai BudgetService.recalculate untuk menyetel ulang dari data transaksi. */
    public void setSpentAmount(double spentAmount) {
        this.spentAmount = round2(Math.max(0, spentAmount));
    }

    @JsonIgnore
    public int getCategoryId() {
        return category == null ? 0 : category.getCategoryId();
    }

    @JsonSetter("categoryId")
    private void applyCategoryId(Integer categoryId) {
        this.category = categoryId == null ? null : new Category(categoryId, null, null, false, 0);
    }

    @JsonSetter("month")
    private void applyMonth(String month) {
        try {
            this.period = month == null ? null : YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new BusinessException("Format bulan harus YYYY-MM");
        }
    }
}
