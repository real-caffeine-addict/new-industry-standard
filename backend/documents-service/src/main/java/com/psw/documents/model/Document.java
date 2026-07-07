package com.psw.documents.model;

import com.psw.documents.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    private Long id;
    private Long projectId;
    private Long dossierId;
    private String title;
    private String documentNumber;
    private DocumentType type;
    private Long currentRevisionId;
    private Long originatingCompanyId;
}
