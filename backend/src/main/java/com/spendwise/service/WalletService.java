package com.spendwise.service;

import com.spendwise.core.BusinessException;
import com.spendwise.dao.TransactionDAO;
import com.spendwise.dao.WalletDAO;
import com.spendwise.model.Wallet;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletService {

    private final WalletDAO walletDAO;
    private final TransactionDAO transactionDAO;

    public WalletService(WalletDAO walletDAO, TransactionDAO transactionDAO) {
        this.walletDAO = walletDAO;
        this.transactionDAO = transactionDAO;
    }

    public List<Wallet> getWallets(int userId) {
        return walletDAO.findByOwner(userId);
    }

    /** Dompet milik pengguna; 404 jika tidak ada atau milik orang lain. */
    public Wallet getWallet(int walletId, int userId) {
        Wallet wallet = walletDAO.findByIdAndOwner(walletId, userId);
        if (wallet == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Dompet tidak ditemukan");
        }
        return wallet;
    }

    public boolean createWallet(Wallet w) {
        w.setName(w.getName() == null ? null : w.getName().trim());
        w.validate();
        assertNameFree(w.getOwnerId(), w.getName(), 0);
        try {
            return walletDAO.save(w);
        } catch (DuplicateKeyException e) {
            throw nameTaken();
        }
    }

    /** Hanya nama dan jenis yang berubah; saldo tidak pernah disentuh (BR-01). */
    public boolean updateWallet(Wallet w) {
        Wallet existing = getWallet(w.getWalletId(), w.getOwnerId());
        existing.setName(w.getName() == null ? null : w.getName().trim());
        existing.setWalletType(w.getWalletType());
        existing.validate();
        assertNameFree(existing.getOwnerId(), existing.getName(), existing.getWalletId());
        try {
            return walletDAO.update(existing);
        } catch (DuplicateKeyException e) {
            throw nameTaken();
        }
    }

    public boolean deleteWallet(int walletId, int userId) {
        Wallet wallet = getWallet(walletId, userId);
        if (transactionDAO.countByWallet(walletId) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "Dompet masih memiliki transaksi");
        }
        if (walletDAO.findByOwner(userId).size() <= 1) {
            throw new BusinessException(HttpStatus.CONFLICT, "Minimal harus ada satu dompet");
        }
        return walletDAO.delete(wallet.getWalletId());
    }

    /** Kedua saldo berubah dalam satu transaksi database; transfer tidak masuk laporan (BR-12). */
    @Transactional
    public boolean transfer(int fromId, int toId, double amount, int userId) {
        if (fromId == toId) {
            throw new BusinessException("Dompet asal dan tujuan tidak boleh sama");
        }
        Wallet from = getWallet(fromId, userId);
        Wallet to = getWallet(toId, userId);
        if (!from.transferTo(to, amount)) {
            throw new BusinessException("Saldo dompet tidak mencukupi");
        }
        walletDAO.update(from);
        walletDAO.update(to);
        return true;
    }

    private void assertNameFree(int ownerId, String name, int exceptWalletId) {
        boolean taken = walletDAO.findByOwner(ownerId).stream()
                .anyMatch(w -> w.getWalletId() != exceptWalletId && w.getName().equalsIgnoreCase(name));
        if (taken) {
            throw nameTaken();
        }
    }

    private BusinessException nameTaken() {
        return new BusinessException(HttpStatus.CONFLICT, "Nama dompet sudah dipakai");
    }
}
