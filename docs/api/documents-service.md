# Documents Service API

## POST /documents

Creates a document with its initial revision.

### Request

| Field | Type | Required | Notes |
|---|---|---:|---|
| projectId | number | yes | Existing project id |
| dossierId | number | no | Existing dossier id when assigned |
| title | string | yes | Document title |
| documentNumber | string | yes | Unique per project and/or dossier |
| type | string | yes | DRAWING, SPEC, DATASHEET, SUBMITTAL, QUERY, RCO, CO, REPORT |
| originatingCompanyId | number/null | no | External company id |
| revisionCode | string/null | no | Defaults to A |
| status | string/null | no | Defaults to DRAFT |
| issuedAt | datetime | yes | ISO-8601 local datetime |

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