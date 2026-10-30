package com.spendwise.core;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.sql.Timestamp;

/** Utilitas kecil untuk DAO (kelas pendukung, tanpa state). */
public final class JdbcSupport {

    private JdbcSupport() {
    }

    /** INSERT dengan placeholder {@code ?} lalu mengembalikan kunci AUTO_INCREMENT yang dibuat. */
    public static int insertReturningKey(JdbcTemplate jdbc, String sql, Object... params) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Kunci hasil INSERT tidak tersedia");
        }
        return key.intValue();
    }

    public static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
