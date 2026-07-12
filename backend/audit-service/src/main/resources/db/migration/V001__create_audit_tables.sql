CREATE TABLE audit_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action in('CREATED','UPDATED','DELETED','UPLOADED','STATUS_CHANGED')),
    target_type VARCHAR(50) NOT NULL CHECK (target_type in ('PROJECT','PROJECT_MEMBER','USER','CONTACT','COMPANY','PROJECT_COMPANY','DOCUMENT','DOCUMENT_REVISION','DOSSIER')),
    target_id BIGINT NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    summary VARCHAR(255) NULL
);

CREATE INDEX idx_audit_events_project_id
    ON audit_events(project_id);

CREATE INDEX idx_audit_events_target
    ON audit_events(target_type, target_id);

CREATE INDEX idx_audit_events_user_id
    ON audit_events(user_id);

CREATE INDEX idx_audit_events_occurred_at
    ON audit_events(occurred_at);