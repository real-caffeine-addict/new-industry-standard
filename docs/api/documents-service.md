# Documents Service API

## POST localhost:8083/documents/create-document

Creates a document with its initial revision.

### Request

| Field | Type | Required | Notes |
|---|---|---:|---|
| projectId | number | yes | Existing project id |
| dossierId | number/null | no | Existing dossier id when assigned |
| title | string | yes | Document title |
| documentNumber | string | yes | Unique per project and/or dossier |
| type | string | yes | DRAWING, SPEC, DATASHEET, SUBMITTAL, QUERY, RCO, CO, REPORT |
| originatingCompanyId | number/null | no | External company id |
| revisionCode | string/null | no | Defaults to A |
| status | string/null | no | DRAFT, SUBMITTED, APPROVED_WITH_COMMENTS, IFC, REJECTED, CANCELLED, SUPERSEDED. Defaults to DRAFT |
| issuedAt | datetime | yes | ISO-8601 local datetime, for example 2026-07-01T10:00:00 |

### Example Request

```json
{
  "projectId": 1,
  "dossierId": 1,
  "title": "Seed Drawing",
  "documentNumber": "DRW-001",
  "type": "DRAWING",
  "originatingCompanyId": null,
  "revisionCode": "A",
  "status": "SUBMITTED",
  "issuedAt": "2026-07-01T10:00:00"
}
```

### Response

Temporary plain text response containing created document and revision ids.

#### Notes
- Phase 1 runs without authentication.
- Request validation is not yet implemented.
- File upload is not yet implemented. fileLocation is generated temporarily by the service

## GET localhost:8083/documents/{id}

Reads the stored document by ID.

### Path parameters

| Field | Type   | Required | Notes       |
| ----- | ------ | -------: |-------------|
| id    | number |      yes | Document ID |

### Example request

```
GET /documents/1
```

### Response

{
"id": 1,
"projectId": 1,
"dossierId": 1,
"title": "Seed Drawing 1",
"documentNumber": "DRW-001",
"type": "DRAWING",
"currentRevisionId": 1,
"originatingCompanyId": null
}
