package com.spendwise.dao;

import com.spendwise.core.JdbcSupport;
import com.spendwise.core.Repository;
import com.spendwise.model.Wallet;
import com.spendwise.model.WalletType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@org.springframework.stereotype.Repository
public class WalletDAO implements Repository<Wallet> {

    private static final String SELECT = "SELECT wallet_id, owner_id, name, wallet_type, balance FROM wallets";

    private final JdbcTemplate jdbc;

    public WalletDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean save(Wallet w) {
        int id = JdbcSupport.insertReturningKey(jdbc,
                "INSERT INTO wallets (owner_id, name, wallet_type, balance) VALUES (?, ?, ?, ?)",
                w.getOwnerId(), w.getName(), w.getWalletType().name(), w.getBalance());
        w.setWalletId(id);
        return id > 0;
    }

    @Override
    public boolean update(Wallet w) {
        return jdbc.update("UPDATE wallets SET name = ?, wallet_type = ?, balance = ? WHERE wallet_id = ?",
                w.getName(), w.getWalletType().name(), w.getBalance(), w.getWalletId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return jdbc.update("DELETE FROM wallets WHERE wallet_id = ?", id) > 0;
    }

    @Override
    public Wallet findById(int id) {
        List<Wallet> list = jdbc.query(SELECT + " WHERE wallet_id = ?", (rs, i) -> mapRow(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Wallet> findAll() {
        return jdbc.query(SELECT + " ORDER BY wallet_id", (rs, i) -> mapRow(rs));
    }

    /** Urut wallet_id; dompet pertama adalah dompet utama (BR-14). */
    public List<Wallet> findByOwner(int userId) {
        return jdbc.query(SELECT + " WHERE owner_id = ? ORDER BY wallet_id", (rs, i) -> mapRow(rs), userId);
    }

    public Wallet findByIdAndOwner(int walletId, int userId) {
        List<Wallet> list = jdbc.query(SELECT + " WHERE wallet_id = ? AND owner_id = ?",
                (rs, i) -> mapRow(rs), walletId, userId);
        return list.isEmpty() ? null : list.get(0);
    }

    private Wallet mapRow(ResultSet rs) throws SQLException {
        return new Wallet(rs.getInt("wallet_id"), rs.getInt("owner_id"), rs.getString("name"),
                WalletType.valueOf(rs.getString("wallet_type")), rs.getDouble("balance"));
    }
}
