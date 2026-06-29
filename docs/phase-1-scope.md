# Phase 1 Scope — Core Document Control System

## Purpose

Phase 1 builds the first usable version of the document control system.

The goal is not to build a complete project-management platform, a full workflow engine, or an AI system.

The goal is to replace a basic Excel-based Master Document Register with a controlled, traceable, project-based document register.

Phase 1 should prove that the system can manage documents, revisions, files, statuses, and audit history in a reliable way.

---

## Phase 1 Product Statement

At the end of Phase 1, a user should be able to:

1. Open a project.
2. View the Master Document Register.
3. Create a document register entry.
4. Upload a file as the first revision.
5. See that revision marked as the current revision.
6. Upload a newer revision.
7. See the newer revision become current.
8. See the previous revision marked as superseded.
9. View the revision history.
10. View the audit history.
11. Export the Master Document Register.

If this flow works end-to-end, Phase 1 is successful.

---

## End Demo

The Phase 1 demo should show this exact flow:

```text
Create project
→ Open project
→ Open Master Document Register
→ Create document: DOC-001
→ Upload Rev A
→ DOC-001 appears in the MDR with Rev A as current
→ Upload Rev B
→ Rev B becomes current
→ Rev A becomes superseded
→ Open document details
→ View revision history
→ View audit history
→ Export MDR
```

This demo is the real acceptance test for Phase 1.

---

## Included in Phase 1

Phase 1 includes only the operational backbone of document control.

Included:

- Project shell
- Master Document Register
- Document creation
- Basic document metadata
- Revision creation
- Current revision handling
- Superseded revision handling
- File upload linked to a revision
- Basic document or revision status
- Revision history
- Audit history
- MDR export

---

## Explicitly Not Included in Phase 1

Phase 1 must not include:

- AI
- OCR
- RAG
- Autonomous agents
- MCP
- Full workflow engine
- Full approval workflow
- Transmittal workflow
- Submittal packages
- Review cycles
- QA/QC module
- Turnover packages
- Claims
- Cost control
- Schedule control
- Procurement
- Mobile app
- Email ingestion
- SharePoint / Aconex / Procore / Autodesk integrations
- Complex permission matrix
- Advanced dashboards

These are later phases.

Adding them now would slow down the first working product and blur the core design.

---

## Minimal Domain Model

Phase 1 should start with the smallest model that supports the end demo.

Required entities:

- Project
- Document
- DocumentRevision
- DocumentFile
- AuditEvent

Likely useful lookup/reference entities:

- Company
- DocumentType
- Discipline
- DocumentStatus

Authentication and user roles can connect to the existing system if already available.
If not, Phase 1 may use a simple development user until real permissions are needed.

---

## Core Domain Rules

The following rules are mandatory:

1. A project contains documents.
2. A document represents the logical identity of a controlled document.
3. A document can have multiple revisions.
4. A file belongs to a specific revision, not directly to the document.
5. Only one revision can be current for a document.
6. When a newer revision becomes current, the previous current revision becomes superseded.
7. Superseded revisions must remain visible in revision history.
8. Old revisions must not be overwritten or silently deleted.
9. Important actions must create audit events.
10. The MDR should show the current revision of each document.

---

## Minimum Document Metadata

A document should have at least:

- Document number
- Title
- Document type
- Discipline
- Originating company
- Current revision
- Current status

A revision should have at least:

- Revision code
- Revision date or received date
- Current flag
- Superseded flag
- Status
- Remarks

A file should have at least:

- Original file name
- File type
- File size
- Storage reference
- Uploaded by
- Uploaded at

---

## Required Screens

Phase 1 needs only these screens:

1. Project list or project entry screen
2. Project page
3. Master Document Register
4. Create document form
5. Document details page
6. Add revision / upload file section
7. Revision history section
8. Audit history section

The UI can be simple.
Correct behavior is more important than visual design.

---

## Required Backend Capabilities

The backend should support:

- Create project
- List projects
- Create document
- List documents in MDR format
- Read document details
- Add first revision
- Add newer revision
- Mark previous revision as superseded
- Upload or link file to revision
- Change basic status
- Read revision history
- Read audit history
- Export MDR

Do not build generic engines before the first vertical slice works.

---

## First Vertical Slice

The first development target is:

```text
Project → Document → First Revision → MDR row
```

This means:

1. Create a project.
2. Create a document inside that project.
3. Add one revision to the document.
4. Show the document in the MDR with the revision displayed.

Only after this works should the team add superseded behavior, audit history, and export.

---

## Phase 1 Build Order

Recommended implementation order:

1. Scope file and branch setup
2. Minimal database model
3. Backend vertical slice: project, document, first revision
4. Frontend vertical slice: project page and MDR
5. Add newer revision behavior
6. Add current / superseded logic
7. Add audit events
8. Add document details and revision history
9. Add MDR export
10. Manual end-to-end demo

---

## Definition of Done

Phase 1 is done only when:

- A project can be created or opened.
- A document can be created in that project.
- A first revision can be added.
- A newer revision can be added.
- The newest revision is clearly marked as current.
- The previous revision is clearly marked as superseded.
- Revision history is visible.
- Audit history is visible.
- The MDR shows the current state of the document register.
- The MDR can be exported.
- The full end demo works without AI.

---

## Guiding Rule

If a feature does not help the end demo, it probably does not belong in Phase 1.

Phase 1 should be boring, deterministic, and reliable.
