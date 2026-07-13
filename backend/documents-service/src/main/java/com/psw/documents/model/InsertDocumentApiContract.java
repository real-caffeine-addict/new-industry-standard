package com.psw.documents.model;

import com.psw.documents.enums.DocumentType;
import com.psw.documents.enums.RevisionStatus;

import java.time.LocalDateTime;

public record InsertDocumentApiContract(
//        Document fields
        Long projectId,
        Long dossierId,
        String title,
        String documentNumber,
        DocumentType type,
        Long originatingCompanyId,
//        Revision fields
        String  revisionCode,
        RevisionStatus status,
        LocalDateTime issuedAt
) {
}
