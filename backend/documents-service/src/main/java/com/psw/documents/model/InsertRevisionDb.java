package com.psw.documents.model;

import java.time.LocalDateTime;

public record InsertRevisionDb(
        Long documentId,
        String revisionCode,
        String status,
        LocalDateTime issuedAt,
        String fileLocation,
        int numberOfFiles
) {
}
