package com.spendwise.dao;

import com.spendwise.core.JdbcSupport;
import com.spendwise.core.Repository;
import com.spendwise.model.Budget;
import com.spendwise.model.Category;
import com.spendwise.model.TransactionType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;

@org.springframework.stereotype.Repository
public class BudgetDAO implements Repository<Budget> {

    private static final String SELECT_JOIN = "SELECT b.budget_id, b.user_id, b.category_id, b.name, b.period, "
            + "b.limit_amount, b.spent_amount, c.name AS category_name, c.type AS category_type, "
            + "c.is_default, COALESCE(c.owner_id, 0) AS category_owner "
            + "FROM budgets b JOIN categories c ON c.category_id = b.category_id";

    private final JdbcTemplate jdbc;

    public BudgetDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean save(Budget b) {
        int id = JdbcSupport.insertReturningKey(jdbc,
                "INSERT INTO budgets (user_id, category_id, name, period, limit_amount, spent_amount) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                b.getUserId(), b.getCategoryId(), b.getName(), b.getPeriod().toString(),
                b.getTargetAmount(), b.getSpentAmount());
        b.setPlanId(id);
        return id > 0;
    }

    @Override
    public boolean update(Budget b) {
        return jdbc.update("UPDATE budgets SET limit_amount = ?, spent_amount = ? WHERE budget_id = ?",
                b.getTargetAmount(), b.getSpentAmount(), b.getPlanId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return jdbc.update("DELETE FROM budgets WHERE budget_id = ?", id) > 0;
    }

    @Override
    public Budget findById(int id) {
        List<Budget> list = jdbc.query(SELECT_JOIN + " WHERE b.budget_id = ?", (rs, i) -> mapRow(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Budget> findAll() {
        return jdbc.query(SELECT_JOIN + " ORDER BY b.budget_id", (rs, i) -> mapRow(rs));
    }

    public Budget findByCategoryAndMonth(int userId, int catId, YearMonth m) {
        List<Budget> list = jdbc.query(SELECT_JOIN + " WHERE b.user_id = ? AND b.category_id = ? AND b.period = ?",
                (rs, i) -> mapRow(rs), userId, catId, m.toString());
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Budget> findByUserAndMonth(int userId, YearMonth m) {
        return jdbc.query(SELECT_JOIN + " WHERE b.user_id = ? AND b.period = ? ORDER BY b.budget_id",
                (rs, i) -> mapRow(rs), userId, m.toString());
    }

    private Budget mapRow(ResultSet rs) throws SQLException {
        Category category = new Category(rs.getInt("category_id"), rs.getString("category_name"),
                TransactionType.valueOf(rs.getString("category_type")), rs.getBoolean("is_default"),
                rs.getInt("category_owner"));
        return new Budget(rs.getInt("budget_id"), rs.getInt("user_id"), rs.getString("name"), category,
                YearMonth.parse(rs.getString("period")), rs.getDouble("limit_amount"), rs.getDouble("spent_amount"));
    }
}
