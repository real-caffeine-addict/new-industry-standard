CREATE TABLE projects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(10) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status in ('PLANNED','ACTIVE','SUSPENDED','TURNOVER','FINISHED')),
    start_date DATE NOT NULL,
    project_files_location VARCHAR(255),
    CONSTRAINT uq_project_name_code UNIQUE (name, code)
);

CREATE TABLE project_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    project_role VARCHAR(50) NOT NULL CHECK (project_role in ('PROJECT_MANAGER','EXECUTIVE_ENGINEER','DOCUMENT_CONTROL','VIEWER')),
    CONSTRAINT fk_project_member_project_id
        FOREIGN KEY (project_id) REFERENCES projects(id)
            ON DELETE CASCADE
            ON UPDATE RESTRICT,
    CONSTRAINT uq_project_member_project UNIQUE (user_id, project_id)

);

CREATE TABLE dossiers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    name VARCHAR(25) NOT NULL,
    code VARCHAR(10) NOT NULL,
    discipline VARCHAR(30) NOT NULL CHECK (discipline in ('CIVIL','MECHANICAL','ELECTRICAL','OTHER')),
    system VARCHAR(20) NOT NULL,
    subsystem VARCHAR(20),
    area VARCHAR(20),
    CONSTRAINT fk_dossier_project_id
        FOREIGN KEY (project_id) REFERENCES projects(id)
            ON DELETE CASCADE
            ON UPDATE RESTRICT,
    CONSTRAINT uq_dossier_project UNIQUE (name, project_id)
);
