package com.spendwise.dao;

import com.spendwise.core.JdbcSupport;
import com.spendwise.core.Repository;
import com.spendwise.model.Admin;
import com.spendwise.model.Member;
import com.spendwise.model.User;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@org.springframework.stereotype.Repository
public class UserDAO implements Repository<User> {

    private static final String SELECT = "SELECT user_id, username, password_hash, full_name, email, role, "
            + "admin_code, is_active, created_at FROM users";

    private final JdbcTemplate jdbc;

    public UserDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean save(User u) {
        int id = JdbcSupport.insertReturningKey(jdbc,
                "INSERT INTO users (username, password_hash, full_name, email, role, is_active, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                u.getUsername(), u.getPasswordHash(), u.getFullName(), u.getEmail(), u.getRole(),
                u.isActive(), java.sql.Timestamp.valueOf(u.getCreatedAt()));
        u.setUserId(id);
        return id > 0;
    }

    @Override
    public boolean update(User u) {
        return jdbc.update("UPDATE users SET password_hash = ?, full_name = ?, email = ?, is_active = ? "
                        + "WHERE user_id = ?",
                u.getPasswordHash(), u.getFullName(), u.getEmail(), u.isActive(), u.getUserId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return jdbc.update("DELETE FROM users WHERE user_id = ?", id) > 0;
    }

    @Override
    public User findById(int id) {
        List<User> list = jdbc.query(SELECT + " WHERE user_id = ?", (rs, i) -> mapRow(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<User> findAll() {
        return jdbc.query(SELECT + " ORDER BY user_id", (rs, i) -> mapRow(rs));
    }

    public User findByUsername(String username) {
        List<User> list = jdbc.query(SELECT + " WHERE username = ?", (rs, i) -> mapRow(rs), username);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean existsByUsername(String username) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        return n != null && n > 0;
    }

    /** Kolom {@code role} menentukan subclass yang dibuat (single-table inheritance). */
    private User mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String username = rs.getString("username");
        String hash = rs.getString("password_hash");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        boolean active = rs.getBoolean("is_active");
        java.time.LocalDateTime createdAt = JdbcSupport.toLocalDateTime(rs.getTimestamp("created_at"));
        if ("ADMIN".equals(rs.getString("role"))) {
            return new Admin(id, username, hash, fullName, email, active, createdAt, rs.getString("admin_code"));
        }
        return new Member(id, username, hash, fullName, email, active, createdAt);
    }
}
