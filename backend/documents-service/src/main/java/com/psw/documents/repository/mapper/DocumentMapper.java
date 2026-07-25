package com.psw.documents.repository.mapper;

import com.psw.documents.enums.DocumentType;
import com.psw.documents.model.Document;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DocumentMapper implements RowMapper<Document> {
    @Override
    public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Document(
            rs.getLong("id"),
            rs.getLong("project_id"),
            nullableLong(rs,"dossier_id"),
            rs.getString("title"),
            rs.getString("document_number"),
            DocumentType.valueOf(rs.getString("type")),
            nullableLong(rs, "current_revision_id"),
            nullableLong(rs, "originating_company_id")
        );
    }

    private Long nullableLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }
}
