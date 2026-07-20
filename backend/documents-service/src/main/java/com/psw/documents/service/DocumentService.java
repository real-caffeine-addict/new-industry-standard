package com.psw.documents.service;

import com.psw.common.dto.CreateAuditEventRequest;
import com.psw.common.enums.AuditableAction;
import com.psw.common.enums.TargetType;
import com.psw.documents.clients.AuditServiceClient;
import com.psw.documents.model.InsertDocumentDb;
import com.psw.documents.model.InsertRevisionDb;
import com.psw.documents.model.InsertDocumentApiContract;
import com.psw.documents.repositories.DocumentsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DocumentService { //TODO: add request validation before exposing beyond dev flow.
    private final DocumentsRepository repo;
    private final AuditServiceClient auditServiceClient;

    public DocumentService (DocumentsRepository repo,
                            AuditServiceClient auditServiceClient) {
        this.repo = repo;
        this.auditServiceClient = auditServiceClient;
    }

    @Transactional
    public String createNewDocument (InsertDocumentApiContract input, Long user){
        Long docId = repo.createDocument(insertDocumentBuilder(input));
        Long revId = repo.createRevision(initRevBuilder(input, docId));
        repo.updateCurrentRev(revId, docId);
        System.out.println(auditServiceClient.createAuditEvent(
                "documents-service",
                new CreateAuditEventRequest(
                        user,
                        input.projectId(),
                        AuditableAction.CREATED,
                        TargetType.DOCUMENT,
                        docId,
                        LocalDateTime.now()
                )
        ));
        return "Successfully created document id: " + docId + " revision id: " + revId; //TODO: replace temporary string response with structured response DTO.
    }

    private InsertDocumentDb insertDocumentBuilder (InsertDocumentApiContract input){
        return new InsertDocumentDb(
                input.projectId(),
                input.dossierId(),
                input.title(),
                input.documentNumber(),
                input.type().toString(),
                input.originatingCompanyId()
        );
    }

    private InsertRevisionDb initRevBuilder (InsertDocumentApiContract input, Long documentId) {
        return new InsertRevisionDb(
                documentId,
                input.revisionCode() == null ? "A" :input.revisionCode(),
                input.status() == null ? "DRAFT" : input.status().toString(),
                input.issuedAt(),
                String.format("%s/%s/%s", input.projectId(), input.dossierId(), input.documentNumber()), //TODO: replace temporary file location builder with document storage path policy.
                0
        );
    }
}
