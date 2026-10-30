package com.spendwise.service;

import com.spendwise.core.BusinessException;
import com.spendwise.dao.SavingGoalDAO;
import com.spendwise.dao.WalletDAO;
import com.spendwise.model.GoalStatus;
import com.spendwise.model.SavingGoal;
import com.spendwise.model.Wallet;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SavingGoalService {

    private final SavingGoalDAO goalDAO;
    private final WalletDAO walletDAO;

    public SavingGoalService(SavingGoalDAO goalDAO, WalletDAO walletDAO) {
        this.goalDAO = goalDAO;
        this.walletDAO = walletDAO;
    }

    public boolean createGoal(SavingGoal g) {
        if (g.getName() != null) {
            g.setName(g.getName().trim());
        }
        g.validate();
        return goalDAO.save(g);
    }

    public List<SavingGoal> getGoals(int userId) {
        return goalDAO.findByUser(userId);
    }

    public SavingGoal getGoal(int goalId, int userId) {
        SavingGoal goal = goalDAO.findById(goalId);
        if (goal == null || goal.getUserId() != userId) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Target tabungan tidak ditemukan");
        }
        return goal;
    }

    /** Saldo dompet berkurang dan dana target bertambah dalam satu transaksi database. */
    @Transactional
    public boolean contribute(int goalId, double amount, int walletId, int userId) {
        SavingGoal goal = getGoal(goalId, userId);
        Wallet wallet = walletDAO.findByIdAndOwner(walletId, userId);
        if (wallet == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Dompet tidak ditemukan");
        }
        if (!goal.addContribution(amount, wallet)) {
            throw new BusinessException("Saldo dompet tidak mencukupi");
        }
        walletDAO.update(wallet);
        return goalDAO.update(goal);
    }

    /** Dana terkumpul dikembalikan ke dompet utama (wallet_id terkecil, BR-14). */
    @Transactional
    public boolean cancelGoal(int goalId, int userId) {
        SavingGoal goal = getGoal(goalId, userId);
        if (goal.getStatus() == GoalStatus.ACHIEVED) {
            throw new BusinessException("Target yang sudah tercapai tidak dapat dibatalkan");
        }
        if (goal.getStatus() == GoalStatus.CANCELLED) {
            throw new BusinessException("Target sudah dibatalkan");
        }
        double refund = goal.getCurrentAmount();
        if (refund > 0) {
            List<Wallet> wallets = walletDAO.findByOwner(userId);
            if (wallets.isEmpty()) {
                throw new BusinessException("Dompet utama tidak ditemukan");
            }
            Wallet main = wallets.get(0);
            goal.withdrawFund(refund);
            main.deposit(refund);
            walletDAO.update(main);
        }
        goal.setStatus(GoalStatus.CANCELLED);
        return goalDAO.update(goal);
    }
}
