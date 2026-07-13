package com.psw.documents.service;

import com.psw.documents.enums.RevisionStatus;
import com.psw.documents.model.InsertDocumentDb;
import com.psw.documents.model.InsertRevisionDb;
import com.psw.documents.model.InsertDocumentApiContract;
import com.psw.documents.repositories.DocumentsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService { //TODO: add request validation before exposing beyond dev flow.
    private final DocumentsRepository repo;

    public DocumentService (DocumentsRepository repo) { this.repo = repo; }

    @Transactional
    public String createNewDocument (InsertDocumentApiContract input){
        Long docId = repo.createDocument(insertDocumentBuilder(input));
        Long revId = repo.createRevision(initRevBuilder(input, docId));
        repo.updateCurrentRev(revId, docId);
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
