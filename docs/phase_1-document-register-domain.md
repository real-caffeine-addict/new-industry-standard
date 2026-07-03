# Phase 1 — Document Register Domain

## Purpose

Phase 1 focuses on building the core document register.

The goal is not to build a full project-management system, workflow engine, AI assistant, or external collaboration portal.

The goal is simple:

A project team should be able to manage controlled project documents, revisions, files, statuses, and audit history inside the system instead of using Excel as the master register.

---

## Main Domain Direction

The system starts as an internal tool.

At this stage, users are internal system users.
The system does not manage users from external companies.

External companies are represented as project parties, contacts, originators, vendors, clients, EPCs, subcontractors, or other organizations we interact with.

---

## Core Objects

### User

A `User` is an internal authenticated system actor.

Users can log in, perform actions, and appear in audit history.

A user does not own project memberships directly.
User membership is managed from the project side.

---

### Project

A `Project` is the main work context.

The project owns:

* document register
* project memberships (users references)
* relationships with companies
* project-specific document data

Project membership should be handled through the project, not through the user.

Example direction:

```text
Project.addMember(user, role)
Project.removeMember(user)
Project.changeMemberRole(user, role)
```

Not:

```text
User.addProject(project)
```

---

### Project Membership

A `ProjectMembership` defines which internal users participate in a project and what role they have there.

It references a user, but it belongs to the project.

A user may appear in many projects, but that is a query/navigation concern, not user ownership.

---

### Company

A `Company` is an organization we interact with.

A company does not own system users in Phase 1.

A company may represent:

* client
* EPC
* contractor
* subcontractor
* vendor
* manufacturer
* consultant
* authority
* other external party

Company type should not be overcomplicated too early.

---

### Document

A `Document` is the logical register record.

It is not the uploaded file.

Example:

```text
PIP-ISO-000123 — Isometric Drawing for Line X
```

The document remains the same logical record across multiple revisions.

A document may include metadata such as:

* document number
* title
* document type
* discipline
* system / area / unit
* originator company
* responsible company
* current status
* current revision

---

### Document Revision

A `DocumentRevision` represents an official revision of a document.

Revisions are preserved.
Old revisions are not overwritten.

The system should support the rule that only one revision is current at a time.

When a new current revision is added, the previous current revision should become superseded.

---

### Document File

A `DocumentFile` is the actual uploaded file.

It belongs to a document revision, not directly to the document.

This leaves room later for multiple files per revision, such as PDF, DWG, scan, signed copy, or native file.

For MVP, it is acceptable to support only one file per revision if that keeps implementation simple.

---

### Audit Event

Audit is required from the beginning.

Important controlled actions should be logged, such as:

* document created
* metadata changed
* revision added
* file uploaded
* status changed
* current revision changed
* revision superseded

Audit should identify who did the action, when, what changed, and which project/entity was affected.

---

## Important Rules

1. `Company` does not own `User`.
2. `User` is an internal system actor.
3. `Project` owns `ProjectMembership`.
4. `Document` is the register record, not the file.
5. `DocumentRevision` is the official version of a document.
6. `DocumentFile` belongs to a revision.
7. Only one revision should be current.
8. Superseded revisions are preserved.
9. Controlled changes require audit history.

---

## Out of Scope for Phase 1

Do not include yet:

* external company users
* external portal access
* AI extraction
* OCR pipeline
* RAG
* autonomous agents
* transmittal workflow
* review cycle workflow
* package readiness
* QA/QC module
* complex approval workflow
* SharePoint / Aconex / Procore integrations
* workflow engine

These may come later, but they should not shape the Phase 1 model too early.

---

## Practical Phase 1 Flow

A minimal useful flow should support:

1. Create project
2. Create companies
3. Add company contacts
4. Define company relationships to the project
5. Create internal users
6. Add users as project members
7. Create document record
8. Upload first revision and file
9. Show document in Master Document Register
10. Add new revision
11. Mark previous revision as superseded
12. Show revision history
13. Show audit history

This is enough to create a real controlled document register MVP.
