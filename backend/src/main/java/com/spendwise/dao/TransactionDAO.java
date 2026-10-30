package com.spendwise.dao;

import com.spendwise.core.JdbcSupport;
import com.spendwise.core.Repository;
import com.spendwise.dto.TransactionFilter;
import com.spendwise.model.Category;
import com.spendwise.model.Expense;
import com.spendwise.model.Income;
import com.spendwise.model.Transaction;
import com.spendwise.model.TransactionType;
import com.spendwise.model.Wallet;
import com.spendwise.model.WalletType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Repository
public class TransactionDAO implements Repository<Transaction> {

    private static final String SELECT_JOIN = "SELECT t.transaction_id, t.user_id, t.type, t.amount, t.trx_date, "
            + "t.description, t.source, t.payment_method, t.is_recurring, "
            + "c.category_id, c.name AS category_name, c.type AS category_type, c.is_default, "
            + "COALESCE(c.owner_id, 0) AS category_owner, "
            + "w.wallet_id, w.name AS wallet_name, w.wallet_type, w.balance, w.owner_id AS wallet_owner "
            + "FROM transactions t "
            + "JOIN categories c ON c.category_id = t.category_id "
            + "JOIN wallets w ON w.wallet_id = t.wallet_id";

    private static final String ORDER = " ORDER BY t.trx_date DESC, t.transaction_id DESC";

    private final JdbcTemplate jdbc;

    public TransactionDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean save(Transaction t) {
        int id = JdbcSupport.insertReturningKey(jdbc,
                "INSERT INTO transactions (user_id, wallet_id, category_id, type, amount, trx_date, description, "
                        + "source, payment_method, is_recurring) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                t.getUserId(), t.getWallet().getWalletId(), t.getCategory().getCategoryId(), t.getType().name(),
                t.getAmount(), Date.valueOf(t.getDate()), t.getDescription(), t.getSource(),
                t.getPaymentMethod(), t.isRecurring());
        t.setTransactionId(id);
        return id > 0;
    }

    @Override
    public boolean update(Transaction t) {
        return jdbc.update("UPDATE transactions SET wallet_id = ?, category_id = ?, type = ?, amount = ?, "
                        + "trx_date = ?, description = ?, source = ?, payment_method = ?, is_recurring = ? "
                        + "WHERE transaction_id = ? AND user_id = ?",
                t.getWallet().getWalletId(), t.getCategory().getCategoryId(), t.getType().name(), t.getAmount(),
                Date.valueOf(t.getDate()), t.getDescription(), t.getSource(), t.getPaymentMethod(),
                t.isRecurring(), t.getTransactionId(), t.getUserId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return jdbc.update("DELETE FROM transactions WHERE transaction_id = ?", id) > 0;
    }

    @Override
    public Transaction findById(int id) {
        List<Transaction> list = jdbc.query(SELECT_JOIN + " WHERE t.transaction_id = ?",
                (rs, i) -> mapRow(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Transaction> findAll() {
        return jdbc.query(SELECT_JOIN + ORDER, (rs, i) -> mapRow(rs));
    }

    /** Riwayat dengan filter dinamis dan paginasi; klausa disusun dari konstanta, nilai lewat placeholder. */
    public List<Transaction> findByFilter(int userId, TransactionFilter f) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, f, null, params);
        params.add(f.sizeOrDefault());
        params.add(f.offset());
        return jdbc.query(SELECT_JOIN + where + ORDER + " LIMIT ? OFFSET ?", (rs, i) -> mapRow(rs), params.toArray());
    }

    public int countByFilter(int userId, TransactionFilter f) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, f, null, params);
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM transactions t" + where, Integer.class, params.toArray());
        return n == null ? 0 : n;
    }

    /** Total nominal dari seluruh data yang cocok filter untuk satu jenis transaksi. */
    public double sumByFilter(int userId, TransactionFilter f, TransactionType t) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, f, t, params);
        Double sum = jdbc.queryForObject("SELECT COALESCE(SUM(t.amount), 0) FROM transactions t" + where,
                Double.class, params.toArray());
        return sum == null ? 0 : sum;
    }

    public List<Transaction> findByPeriod(int userId, LocalDate from, LocalDate to) {
        return jdbc.query(SELECT_JOIN + " WHERE t.user_id = ? AND t.trx_date BETWEEN ? AND ?" + ORDER,
                (rs, i) -> mapRow(rs), userId, Date.valueOf(from), Date.valueOf(to));
    }

    public int countByWallet(int walletId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM transactions WHERE wallet_id = ?",
                Integer.class, walletId);
        return n == null ? 0 : n;
    }

    /** Total pengeluaran satu kategori dalam satu bulan (menghitung ulang spentAmount anggaran). */
    public double sumExpense(int userId, int categoryId, YearMonth m) {
        Double sum = jdbc.queryForObject("SELECT COALESCE(SUM(amount), 0) FROM transactions "
                        + "WHERE user_id = ? AND category_id = ? AND type = 'EXPENSE' "
                        + "AND trx_date >= ? AND trx_date < ?", Double.class, userId, categoryId,
                Date.valueOf(m.atDay(1)), Date.valueOf(m.plusMonths(1).atDay(1)));
        return sum == null ? 0 : sum;
    }

    private String buildWhere(int userId, TransactionFilter f, TransactionType forcedType, List<Object> params) {
        StringBuilder sb = new StringBuilder(" WHERE t.user_id = ? AND t.trx_date BETWEEN ? AND ?");
        params.add(userId);
        params.add(Date.valueOf(f.fromOrDefault()));
        params.add(Date.valueOf(f.toOrDefault()));
        TransactionType type = forcedType != null ? forcedType : f.type();
        if (forcedType != null && f.type() != null && f.type() != forcedType) {
            sb.append(" AND 1 = 0");
        } else if (type != null) {
            sb.append(" AND t.type = ?");
            params.add(type.name());
        }
        if (f.categoryId() != null) {
            sb.append(" AND t.category_id = ?");
            params.add(f.categoryId());
        }
        if (f.walletId() != null) {
            sb.append(" AND t.wallet_id = ?");
            params.add(f.walletId());
        }
        if (f.q() != null && !f.q().isBlank()) {
            sb.append(" AND t.description LIKE ?");
            params.add("%" + f.q().trim() + "%");
        }
        return sb.toString();
    }

    /** Kolom {@code type} menentukan subclass yang dibuat (single-table inheritance). */
    private Transaction mapRow(ResultSet rs) throws SQLException {
        Category category = new Category(rs.getInt("category_id"), rs.getString("category_name"),
                TransactionType.valueOf(rs.getString("category_type")), rs.getBoolean("is_default"),
                rs.getInt("category_owner"));
        Wallet wallet = new Wallet(rs.getInt("wallet_id"), rs.getInt("wallet_owner"), rs.getString("wallet_name"),
                WalletType.valueOf(rs.getString("wallet_type")), rs.getDouble("balance"));
        double amount = rs.getDouble("amount");
        LocalDate date = rs.getDate("trx_date").toLocalDate();
        String description = rs.getString("description");
        Transaction t;
        if ("INCOME".equals(rs.getString("type"))) {
            t = new Income(amount, date, description, category, wallet, rs.getString("source"));
        } else {
            t = new Expense(amount, date, description, category, wallet, rs.getString("payment_method"),
                    rs.getBoolean("is_recurring"));
        }
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setUserId(rs.getInt("user_id"));
        return t;
    }
}
