CREATE TABLE incidents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    log_id varchar(50) NOT NULL,
    request_id varchar(50) NOT NULL,
    time_created TIMESTAMP NOT NULL,
    alert_severity VARCHAR(15) NOT NULL ,
    source_component VARCHAR(50),
    description TEXT,
    additional_data TEXT
);

CREATE INDEX idx_log_id
    ON incidents(log_id);

CREATE INDEX idx_request_id
    ON incidents(request_id);
