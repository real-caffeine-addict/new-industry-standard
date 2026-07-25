package com.psw.documents.repository;

import com.psw.documents.model.Document;
import com.psw.documents.model.InsertDocumentDb;
import com.psw.documents.model.InsertRevisionDb;
import com.psw.documents.repository.mapper.DocumentMapper;
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
public class DocumentsRepository {
    private final NamedParameterJdbcTemplate namedJdbc;
    private final JdbcTemplate jdbc;

    @Value("classpath:db/sql/createDoc.sql")
    private Resource createDoc;
    @Value("classpath:db/sql/createRev.sql")
    private Resource createRev;

    private String createDocStr;
    private String createRevStr;

    public DocumentsRepository (NamedParameterJdbcTemplate namedJdbc,
                                JdbcTemplate jdbc) {
        this.namedJdbc = namedJdbc;
        this.jdbc = jdbc;
    }

    @PostConstruct
    private void initVerifySql () {
        try {
            createDocStr = StreamUtils.copyToString(createDoc.getInputStream(), StandardCharsets.UTF_8);
            createRevStr = StreamUtils.copyToString(createRev.getInputStream(), StandardCharsets.UTF_8);
        }catch (IOException e) {
            throw new RuntimeException("Failed to initiate sql strings at documents repository");
        }
    }

    public Long createDocument (InsertDocumentDb document) {
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(document);
        namedJdbc.update(createDocStr, params, key, new String[]{"id"});
        Number keyValue = key.getKey();
        if (keyValue == null) {
            throw new IllegalStateException("Failed to retrieve generated document id");
        }
        return keyValue.longValue();
    }

    public Long createRevision (InsertRevisionDb rev){
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(rev);
        namedJdbc.update(createRevStr, params, key, new String[]{"id"});
        Number keyValue = key.getKey();
        if (keyValue == null) {
            throw new IllegalStateException("Failed to retrieve generated document id");
        }
        return keyValue.longValue();
    }

    public void updateCurrentRev (Long revId, Long docId){
        String sql = "UPDATE documents SET current_revision_id = ? WHERE id = ?";
        jdbc.update(sql, revId, docId);
    }

    public Document getDocumentById (Long id){
        String sql = "SELECT * FROM documents WHERE id = ?";
        return jdbc.queryForObject(sql, new DocumentMapper(), id); // TODO: Add proper not-found handling after RequestLogger/error handling is introduced.
    }
}