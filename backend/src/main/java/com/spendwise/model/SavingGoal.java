package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spendwise.core.BusinessException;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

/** Target tabungan; targetAmount = nominal target. */
public class SavingGoal extends FinancialPlan {

    private double currentAmount;
    private LocalDate deadline;
    private GoalStatus status = GoalStatus.ACTIVE;

    /** Untuk deserialisasi JSON request pembuatan target. */
    private SavingGoal() {
    }

    public SavingGoal(int userId, String name, double targetAmount, LocalDate deadline) {
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.deadline = deadline;
    }

    /** Konstruktor lengkap untuk DAO saat membaca dari database. */
    public SavingGoal(int planId, int userId, String name, double targetAmount, double currentAmount,
                      LocalDate deadline, GoalStatus status) {
        this.planId = planId;
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = round2(currentAmount);
        this.deadline = deadline;
        this.status = status;
    }

    /**
     * Setor dari dompet sumber. Status menjadi ACHIEVED otomatis saat target tercapai.
     *
     * @return false jika saldo dompet tidak mencukupi
     */
    public boolean addContribution(double amount, Wallet source) {
        if (status == GoalStatus.ACHIEVED) {
            throw new BusinessException("Target sudah tercapai");
        }
        if (status == GoalStatus.CANCELLED) {
            throw new BusinessException("Target sudah dibatalkan");
        }
        if (amount <= 0) {
            throw new BusinessException("Nominal harus lebih dari 0");
        }
        if (round2(currentAmount + amount) > targetAmount) {
            throw new BusinessException("Nominal melebihi sisa target");
        }
        if (!source.withdraw(amount)) {
            return false;
        }
        currentAmount = round2(currentAmount + amount);
        if (currentAmount >= targetAmount) {
            status = GoalStatus.ACHIEVED;
        }
        return true;
    }

    /** Mengurangi dana terkumpul (mis. saat dibatalkan). @return false jika dana tidak cukup */
    public boolean withdrawFund(double amount) {
        if (amount <= 0) {
            throw new BusinessException("Nominal harus lebih dari 0");
        }
        if (amount > currentAmount) {
            return false;
        }
        currentAmount = round2(currentAmount - amount);
        return true;
    }

    public long getDaysRemaining() {
        return getDaysRemaining(LocalDate.now());
    }

    long getDaysRemaining(LocalDate today) {
        return Math.max(0, ChronoUnit.DAYS.between(today, deadline));
    }

    /** BR-07: sisa target / max(1, sisa bulan dibulatkan ke atas). */
    public double getRecommendedMonthlySaving() {
        return getRecommendedMonthlySaving(LocalDate.now());
    }

    double getRecommendedMonthlySaving(LocalDate today) {
        if (!deadline.isAfter(today)) {
            return getRemainingAmount();
        }
        Period p = Period.between(today, deadline);
        long months = p.toTotalMonths() + (p.getDays() > 0 ? 1 : 0);
        return round2(getRemainingAmount() / Math.max(1, months));
    }

    @Override
    public double getProgress() {
        return targetAmount <= 0 ? 0 : Math.min(1.0, currentAmount / targetAmount);
    }

    @Override
    public boolean isCompleted() {
        return status == GoalStatus.ACHIEVED;
    }

    @Override
    public boolean validate() {
        super.validate();
        if (deadline == null || !deadline.isAfter(LocalDate.now())) {
            throw new BusinessException("Tenggat harus di masa depan");
        }
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public double getCurrentAmount() {
        return currentAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }
}
