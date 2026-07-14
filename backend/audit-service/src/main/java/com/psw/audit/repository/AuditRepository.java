package com.psw.audit.repository;

import com.psw.audit.model.InsertAuditEventDb;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Repository
public class AuditRepository {
    private final NamedParameterJdbcTemplate namedJdbc;
    private final JdbcTemplate jdbc;

    @Value("classpath:db/sql/insertAuditEvent.sql")
    private Resource insertAuditEvent;

    private String insertAuditEventStr;

    public AuditRepository (NamedParameterJdbcTemplate namedJdbc,
                           JdbcTemplate jdbc) {
        this.namedJdbc = namedJdbc;
        this.jdbc = jdbc;
    }

    @PostConstruct
    private void initVerifySql () {
        try {
            insertAuditEventStr = StreamUtils.copyToString(insertAuditEvent.getInputStream(), StandardCharsets.UTF_8);
        }catch (IOException e) {
            throw new RuntimeException("Failed to initiate sql strings at audit repository");
        }
    }

    public Long createAuditEvent (InsertAuditEventDb event) {
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(event);
        namedJdbc.update(insertAuditEventStr, params, key, new String[]{"id"});
        Number keyVal = key.getKey();
        if (keyVal == null)
            throw new IllegalStateException("Failed to retrieve generated audit event id");
        return keyVal.longValue();
    }

    //TODO: Update summary -> json_payload on next migration
    public String readAuditEventById (Long id) {
        String sql = "SELECT summary FROM audit_events WHERE id = ?";
        return jdbc.queryForObject(sql, String.class, id);
    }
}
