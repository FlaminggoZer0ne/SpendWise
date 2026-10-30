package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spendwise.core.BusinessException;
import com.spendwise.core.Validatable;

public abstract class FinancialPlan implements Validatable {

    private static final double MAX_TARGET = 1_000_000_000;

    protected int planId;
    protected int userId;
    protected String name;
    protected double targetAmount;

    /** Rasio progres: Budget = terpakai/batas (bisa lebih dari 1), SavingGoal = terkumpul/target (maks 1). */
    public abstract double getProgress();

    public abstract boolean isCompleted();

    public double getRemainingAmount() {
        return round2(targetAmount - getProgress() * targetAmount);
    }

    public double getProgressPercentage() {
        return round2(getProgress() * 100);
    }

    @Override
    public boolean validate() {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Nama wajib diisi");
        }
        if (targetAmount <= 0 || targetAmount > MAX_TARGET) {
            throw new BusinessException("Nominal target harus lebih dari 0 dan maksimal 1.000.000.000");
        }
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    protected static double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
