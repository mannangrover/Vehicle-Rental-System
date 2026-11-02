package com.vehiclerental.dao;

import com.vehiclerental.model.AuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuditLogDao {

    private static final RowMapper<AuditLog> AUDIT_LOG_MAPPER = (rs, rowNum) -> {
        AuditLog auditLog = new AuditLog();
        auditLog.setId(rs.getLong("id"));
        auditLog.setAction(rs.getString("action"));
        auditLog.setDetails(rs.getString("details"));
        auditLog.setActor(rs.getString("actor"));
        auditLog.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return auditLog;
    };

    private final JdbcTemplate jdbcTemplate;

    public AuditLogDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(String action, String details, String actor) {
        jdbcTemplate.update("INSERT INTO audit_logs (action, details, actor) VALUES (?, ?, ?)", action, details, actor);
    }

    public List<AuditLog> findRecent(int limit) {
        return jdbcTemplate.query("SELECT * FROM audit_logs ORDER BY id DESC LIMIT ?", AUDIT_LOG_MAPPER, limit);
    }
}
