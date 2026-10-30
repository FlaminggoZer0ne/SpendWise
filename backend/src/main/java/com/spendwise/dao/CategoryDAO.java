package com.spendwise.dao;

import com.spendwise.core.JdbcSupport;
import com.spendwise.core.Repository;
import com.spendwise.model.Category;
import com.spendwise.model.TransactionType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@org.springframework.stereotype.Repository
public class CategoryDAO implements Repository<Category> {

    private static final String SELECT = "SELECT category_id, name, type, is_default, owner_id FROM categories";

    private final JdbcTemplate jdbc;

    public CategoryDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean save(Category c) {
        Integer owner = c.isDefault() ? null : c.getOwnerId();
        int id = JdbcSupport.insertReturningKey(jdbc,
                "INSERT INTO categories (name, type, is_default, owner_id) VALUES (?, ?, ?, ?)",
                c.getName(), c.getType().name(), c.isDefault(), owner);
        c.setCategoryId(id);
        return id > 0;
    }

    @Override
    public boolean update(Category c) {
        return jdbc.update("UPDATE categories SET name = ?, type = ? WHERE category_id = ?",
                c.getName(), c.getType().name(), c.getCategoryId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return jdbc.update("DELETE FROM categories WHERE category_id = ?", id) > 0;
    }

    @Override
    public Category findById(int id) {
        List<Category> list = jdbc.query(SELECT + " WHERE category_id = ?", (rs, i) -> mapRow(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Category> findAll() {
        return jdbc.query(SELECT + " ORDER BY category_id", (rs, i) -> mapRow(rs));
    }

    /** Kategori default + milik pengguna; {@code t} null berarti semua jenis. */
    public List<Category> findAvailable(int userId, TransactionType t) {
        if (t == null) {
            return jdbc.query(SELECT + " WHERE (is_default = 1 OR owner_id = ?) "
                    + "ORDER BY type, is_default DESC, name", (rs, i) -> mapRow(rs), userId);
        }
        return jdbc.query(SELECT + " WHERE (is_default = 1 OR owner_id = ?) AND type = ? "
                + "ORDER BY is_default DESC, name", (rs, i) -> mapRow(rs), userId, t.name());
    }

    /** True jika dipakai transaksi atau anggaran. */
    public boolean isUsed(int categoryId) {
        Integer n = jdbc.queryForObject("SELECT (EXISTS(SELECT 1 FROM transactions WHERE category_id = ?) "
                + "OR EXISTS(SELECT 1 FROM budgets WHERE category_id = ?))", Integer.class, categoryId, categoryId);
        return n != null && n > 0;
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        return new Category(rs.getInt("category_id"), rs.getString("name"),
                TransactionType.valueOf(rs.getString("type")), rs.getBoolean("is_default"), rs.getInt("owner_id"));
    }
}
