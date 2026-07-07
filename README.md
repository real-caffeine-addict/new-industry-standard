# Project Signal Wizard

Updated: Jul 1, 2026

**Project Signal Wizard** is a project-control assistant for industrial contractors and project teams.

It helps project managers detect early signals hidden in project records, document control, field activity, material movement, quality evidence, procurement activity, and contract workflows — before those signals become project fires.

The product starts with a focused **Document Control Section MVP**. This is intentional: controlled project documentation is the first reliable truth layer in an industrial project. Once that layer is reliable, the system can expand toward material control, QA/QC readiness, procurement support, contract/VO signals, automation, and AI-assisted project guidance.

---

## Vision

Project managers do not fail because they lack effort. They fail because important signals are buried in noise.

A new project manager usually starts from the field because that is visible, urgent, and immediate. An experienced project manager also listens to the field — but hears patterns: missing approvals, wrong revisions, material mismatches, weak documentation, quality gates, commercial exposure, and tomorrow's blockers.

Project Signal Wizard is designed to close that experience gap.

The goal is not to replace the project manager. The goal is to help project managers, project engineers, document controllers, and project-control teams see what an experienced manager would check next.

---

## Current Product Stage

The project has completed **Phase 0 — Product Framing and Domain Grounding**.

The current focus is **Phase 1 — Document Control Section MVP**.

This first MVP is not the full product. It is the first operational section of the wider system: a reliable document-control backbone that can later produce project signals.

---

## First MVP: Document Control Section

The first MVP focuses on controlled project documentation:

- project setup;
- companies, users, and roles;
- controlled document register;
- document metadata;
- file upload;
- revision control;
- current and superseded revision handling;
- document status tracking;
- basic transmittal tracking;
- audit trail;
- Master Document Register report;
- overdue review report.

This section is deliberately narrow. It should prove that the system can maintain reliable project records before adding broader project-control intelligence.

---

## Target Users

Primary users are contractor-side project teams, especially:

- project managers;
- project engineers;
- document controllers;
- project-control staff;
- mechanical, piping, steelwork, and fabrication contractors.

Later users may include:

- discipline managers at EPC companies;
- QA/QC managers;
- procurement and material-control teams;
- subcontractor coordinators.

---

## Product Direction

Project Signal Wizard is expected to grow from document control into a broader project-control assistant.

Future sections may include:

- material and warehouse control;
- QA/QC readiness and turnover evidence;
- procurement support;
- subcontractor tracking;
- contract, VO, and claim signal detection;
- meeting summaries and action tracking;
- AI-assisted reporting;
- controlled workflow automation;
- mobile project briefings.

The long-term direction is a system that quietly watches the trusted project data, detects mismatches and missing evidence, and gives the project manager concise signals that support fast decisions.

---

## AI and Automation Approach

AI is an important part of the long-term product, but it is not the source of truth.

The system should use AI where it creates practical value:

- natural-language search and project questions;
- document and meeting summarization;
- metadata extraction;
- report drafting;
- RFQ and communication drafts;
- signal explanation;
- workflow assistance.

Consequential actions require control. Contractual, commercial, quality, safety, or external-facing actions should remain auditable and subject to human approval.

Default rule:

> AI may suggest. The system validates. A human approves. The audit trail records.

---

## Technical Direction

Planned stack:

- **Backend:** Spring Boot / Java
- **Database:** MariaDB
- **Web UI:** React
- **Mobile UI:** React Native later
- **Background processing:** queued workers/jobs for long-running tasks
- **AI:** assistant layer added gradually after reliable workflows exist

The starting architecture uses a small microservice-based backend with clear domain boundaries. The project should avoid premature generic agent frameworks or autonomous behavior before the core workflows are reliable.

---

## Key Documentation

Important project documents include:

- `purpose.md` — product purpose and positioning;
- `roadmap.md` — living product roadmap;
- ADRs — significant product and architecture decisions;
- domain documents — glossary, document types, statuses, revision rules, workflow rules, permissions, and MVP reports.

The roadmap is a living planning document. Significant product, scope, or architecture decisions should be recorded separately as ADRs.
