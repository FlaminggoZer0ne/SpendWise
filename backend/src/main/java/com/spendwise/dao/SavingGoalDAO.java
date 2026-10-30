package com.spendwise.dao;

import com.spendwise.core.JdbcSupport;
import com.spendwise.core.Repository;
import com.spendwise.model.GoalStatus;
import com.spendwise.model.SavingGoal;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@org.springframework.stereotype.Repository
public class SavingGoalDAO implements Repository<SavingGoal> {

    private static final String SELECT = "SELECT goal_id, user_id, name, target_amount, current_amount, deadline, "
            + "status FROM saving_goals";

    private final JdbcTemplate jdbc;

    public SavingGoalDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean save(SavingGoal g) {
        int id = JdbcSupport.insertReturningKey(jdbc,
                "INSERT INTO saving_goals (user_id, name, target_amount, current_amount, deadline, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                g.getUserId(), g.getName(), g.getTargetAmount(), g.getCurrentAmount(),
                Date.valueOf(g.getDeadline()), g.getStatus().name());
        g.setPlanId(id);
        return id > 0;
    }

    @Override
    public boolean update(SavingGoal g) {
        return jdbc.update("UPDATE saving_goals SET current_amount = ?, status = ? WHERE goal_id = ?",
                g.getCurrentAmount(), g.getStatus().name(), g.getPlanId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return jdbc.update("DELETE FROM saving_goals WHERE goal_id = ?", id) > 0;
    }

    @Override
    public SavingGoal findById(int id) {
        List<SavingGoal> list = jdbc.query(SELECT + " WHERE goal_id = ?", (rs, i) -> mapRow(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<SavingGoal> findAll() {
        return jdbc.query(SELECT + " ORDER BY goal_id", (rs, i) -> mapRow(rs));
    }

    /** Urut: ACTIVE dulu, lalu tenggat terdekat. */
    public List<SavingGoal> findByUser(int userId) {
        return jdbc.query(SELECT + " WHERE user_id = ? ORDER BY (status = 'ACTIVE') DESC, deadline ASC, goal_id",
                (rs, i) -> mapRow(rs), userId);
    }

    private SavingGoal mapRow(ResultSet rs) throws SQLException {
        return new SavingGoal(rs.getInt("goal_id"), rs.getInt("user_id"), rs.getString("name"),
                rs.getDouble("target_amount"), rs.getDouble("current_amount"),
                rs.getDate("deadline").toLocalDate(), GoalStatus.valueOf(rs.getString("status")));
    }
}
