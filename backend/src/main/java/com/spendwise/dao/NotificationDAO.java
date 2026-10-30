package com.spendwise.dao;

import com.spendwise.core.JdbcSupport;
import com.spendwise.core.Repository;
import com.spendwise.model.Notification;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@org.springframework.stereotype.Repository
public class NotificationDAO implements Repository<Notification> {

    private static final String SELECT = "SELECT notification_id, user_id, message, level, is_read, created_at "
            + "FROM notifications";

    private final JdbcTemplate jdbc;

    public NotificationDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean save(Notification n) {
        int id = JdbcSupport.insertReturningKey(jdbc,
                "INSERT INTO notifications (user_id, message, level, is_read, created_at) VALUES (?, ?, ?, ?, ?)",
                n.getUserId(), n.getMessage(), n.getLevel(), n.isRead(), Timestamp.valueOf(n.getCreatedAt()));
        n.setNotificationId(id);
        return id > 0;
    }

    @Override
    public boolean update(Notification n) {
        return jdbc.update("UPDATE notifications SET is_read = ? WHERE notification_id = ?",
                n.isRead(), n.getNotificationId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return jdbc.update("DELETE FROM notifications WHERE notification_id = ?", id) > 0;
    }

    @Override
    public Notification findById(int id) {
        List<Notification> list = jdbc.query(SELECT + " WHERE notification_id = ?", (rs, i) -> mapRow(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Notification> findAll() {
        return jdbc.query(SELECT + " ORDER BY notification_id", (rs, i) -> mapRow(rs));
    }

    public List<Notification> findUnread(int userId) {
        return jdbc.query(SELECT + " WHERE user_id = ? AND is_read = 0 ORDER BY created_at DESC, notification_id DESC",
                (rs, i) -> mapRow(rs), userId);
    }

    /** Notifikasi terbaru (dibaca maupun belum), maksimal {@code limit}. */
    public List<Notification> findRecent(int userId, int limit) {
        return jdbc.query(SELECT + " WHERE user_id = ? ORDER BY created_at DESC, notification_id DESC LIMIT ?",
                (rs, i) -> mapRow(rs), userId, limit);
    }

    public int countUnread(int userId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0",
                Integer.class, userId);
        return n == null ? 0 : n;
    }

    public void markAllRead(int userId) {
        jdbc.update("UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0", userId);
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        return new Notification(rs.getInt("notification_id"), rs.getInt("user_id"), rs.getString("message"),
                rs.getString("level"), rs.getBoolean("is_read"), JdbcSupport.toLocalDateTime(rs.getTimestamp("created_at")));
    }
}
