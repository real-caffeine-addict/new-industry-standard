# ADR Format Convention

## Purpose

Architecture Decision Records document important product, architecture, modeling, and implementation decisions.

An ADR should explain what was decided, why the decision was made, what alternatives or boundaries were considered, and what consequences the team accepts.

ADR files are not task notes, meeting minutes, or implementation tickets.

## File Naming

Use the following file naming pattern:

```text
adr_0001_short_decision_name.md
adr_0002_short_decision_name.md
adr_0003_short_decision_name.md
```

Rules:

- use lowercase letters;
- use underscores between words;
- keep the name descriptive but not too long;
- preserve the numeric sequence;
- do not rename approved ADRs unless there is a strong reason.

## Title

Start every ADR with a level-one title:

```markdown
# ADR-0001: Decision Title
```

The title should describe the decision, not the implementation task.

Good examples:

- `ADR-0001: Document Type Matrix Scope and Modeling Decisions`
- `ADR-0002: Product Pivot to Project Signal Wizard`

## Required Sections

Use these sections by default:

```markdown
# ADR-000X: Decision Title

## Status

Draft

## Context

...

## Decision

...

## Resolved Decisions

### 1. First decision area

...

### 2. Second decision area

...

## Consequences

### Positive

- ...

### Risks

- ...
```

Additional sections may be added when useful, such as:

- `## Modeling Notes`
- `## Technical Notes`
- `## Action Items`
- `## Later Decisions`

## Status Values

Use simple status values:

- `Draft` — still under discussion or not yet committed as a final decision.
- `Approved` — accepted as the current project direction.
- `Superseded` — replaced by a later ADR.
- `Rejected` — recorded but not adopted.

If an ADR is superseded, reference the replacing ADR in the status section.

## Writing Style

Write ADRs in clear English.

Prefer short paragraphs and practical engineering language.

Avoid marketing language, vague architecture slogans, and implementation noise.

An ADR should be understandable later without needing the original chat discussion.

## Decision Scope

An ADR should capture decisions that are significant enough to affect future work.

Good ADR topics:

- product direction changes;
- architecture boundaries;
- service ownership decisions;
- security and audit strategy;
- modeling decisions that are hard to reverse;
- major technical timing decisions.

Poor ADR topics:

- small bug fixes;
- local refactoring;
- temporary debugging steps;
- routine endpoint implementation;
- commit summaries.

## Context Section Guidance

The context section should answer:

- what problem triggered the decision;
- what phase or product area is affected;
- what constraints matter;
- what is already true in the project;
- what should not be assumed.

## Decision Section Guidance

The decision section should state the actual decision directly.

It should be possible to read only this section and understand what the team chose.

## Resolved Decisions Guidance

Use numbered subsections when the ADR contains several related decisions.

Each subsection should cover one decision area.

Use this section to remove ambiguity from future implementation work.

## Consequences Guidance

Every ADR should include both positive consequences and risks.

Positive consequences explain why the decision is useful.

Risks explain what can go wrong or what tradeoff is being accepted.

Do not hide risks just because the decision is approved.

## Action Items

Use action items only when the ADR creates follow-up documentation or implementation work.

Keep action items short and concrete.

Example:

```markdown
## Action Items

1. Update `roadmap.md` to reflect the decision.
2. Implement the shared request logger before expanding Phase 1 endpoints.
```

## Relationship to Tickets and Commits

ADRs define direction.

Tickets define work.

Commits record implementation changes.

Do not use ADRs as a substitute for task tracking or commit messages.
