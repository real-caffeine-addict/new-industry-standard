-- Dev-only seed data.
-- Assumes a clean local development database.
-- Do not use for production or shared environments.
INSERT INTO users (username, status)
VALUES ('seed_user', 'ACTIVE');

INSERT INTO projects (name, code, status, start_date, project_files_location)
VALUES ('Seed Project', 'SEED01', 'ACTIVE', '2026-07-01', '/tmp/psw/seed-project/files');

INSERT INTO project_members (user_id, project_id, project_role)
VALUES (1, 1, 'DOCUMENT_CONTROL');

INSERT INTO dossiers (project_id, name, code, discipline, system, subsystem, area)
VALUES (1, 'Seed Dossier', 'DOS01', 'CIVIL', 'SYS01', 'SUB01', 'AREA01');