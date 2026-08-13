package com.psw.incidents.repository;

import com.psw.common.dto.IncidentDto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Repository
public class IncidentsRepository {
    private final NamedParameterJdbcTemplate jdbc;
    @Value("classpath:db/sql/insertIncident.sql")
    private Resource insertInc;

    private String insertIncStr;

    @PostConstruct
    private void initVerifySql () {
        try {
            insertIncStr = StreamUtils.copyToString(insertInc.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e){
            throw new RuntimeException("Failed to initiate sql strings at incidents repository");
        }
    }

    public IncidentsRepository (NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Long insertIncident (IncidentDto record) {
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(record);
        jdbc.update(insertIncStr, params, key, new String[]{"id"});
        Number keyValue = key.getKey();
        if (keyValue == null) {
            throw new IllegalStateException("Failed to retrieve generated incident id");
        }
        return keyValue.longValue();
    }
}
