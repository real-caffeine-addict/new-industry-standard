CREATE TABLE documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    dossier_id BIGINT,
    title VARCHAR(255) NOT NULL,
    document_number VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type in ('DRAWING','SPEC','DATASHEET','SUBMITTAL','QUERY','RCO','CO','REPORT')),
    current_revision_id BIGINT NULL,
    originating_company_id BIGINT,
    CONSTRAINT uq_document_dossier UNIQUE (dossier_id, document_number),
    CONSTRAINT uq_document_project UNIQUE (project_id, document_number)
);

CREATE TABLE document_revisions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    revision_code VARCHAR(20) NOT NULL,
    status varchar(50) NOT NULL CHECK (status IN ('DRAFT','SUBMITTED','APPROVED_WITH_COMMENTS','IFC','REJECTED','CANCELLED','SUPERSEDED')),
    issued_at TIMESTAMP NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    file_location VARCHAR(255) NOT NULL,
    number_of_files BIGINT NOT NULL,
    CONSTRAINT uq_document_revision UNIQUE (document_id, revision_code),
    CONSTRAINT fk_document_id_document_revisions
        FOREIGN KEY (document_id) REFERENCES documents(id)
            ON DELETE CASCADE
            ON UPDATE RESTRICT
);

ALTER TABLE documents
    ADD CONSTRAINT fk_documents_current_revision_id
        FOREIGN KEY (current_revision_id) REFERENCES document_revisions(id)
            ON DELETE SET NULL
            ON UPDATE RESTRICT;