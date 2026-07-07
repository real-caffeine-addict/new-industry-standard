package com.psw.documents.model;

import com.psw.documents.enums.RevisionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRevision {
    private Long id;
    private Long documentId;
    private String revisionCode;
    private RevisionStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime receivedAt;
    private String fileLocation;
    private int numberOfFiles;
}
