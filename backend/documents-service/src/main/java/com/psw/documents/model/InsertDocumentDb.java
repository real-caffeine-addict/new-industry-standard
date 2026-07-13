package com.psw.documents.model;

import com.psw.documents.enums.DocumentType;

public record InsertDocumentDb(
    Long projectId,
    Long dossierId,
    String title,
    String documentNumber,
    String type,
    Long originatingCompanyId
) {
}
