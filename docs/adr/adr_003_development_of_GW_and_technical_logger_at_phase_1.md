# ADR-0003: Development of Gateway and Technical Request Logger During Phase 1

## Status

Draft

## Context

Phase 1 is focused on building the Document Control Section MVP.

The current implementation already has a working vertical slice:

- React UI submits a document creation request;
- `documents-service` persists the document and initial revision;
- `documents-service` calls `audit-service` through Feign;
- `audit-service` persists the business audit event.

At this stage, the UI still calls backend services directly during local development.

This was acceptable for the first vertical slice, but it should not become the long-term pattern. As more endpoints and screens are added, direct UI-to-service calls would spread service URLs, CORS handling, error handling, request identifiers, and HTTP details across the frontend.

The project also needs production-grade technical request logging for troubleshooting. This is separate from the business audit trail.

Business audit events answer:

> Who did what business action, on which target, in which project, and when?

Technical request logs answer:

> Which request entered the application, how was it routed, how long did it take, what status was returned, and what failed if there was an error?

The original roadmap does not list an API Gateway as an explicit Phase 1 product scope item. However, Phase 1 is the point where the first real UI and multiple backend services start to interact. Introducing the Gateway and Request Logger during Phase 1 reduces future rework and creates a stable entry boundary before the UI grows.

This decision does not introduce authentication, identity enforcement, internal service signatures, distributed tracing infrastructure, log aggregation, dashboards, or alerting.

## Decision

Develop a technical `RequestLogger` and introduce a Gateway during Phase 1, before continuing with most remaining Phase 1 endpoints.

The immediate execution order is:

1. finish the next momentum-preserving read slice:
   - read document;
   - read audit event;
   - minimal UI support;
2. build the technical request logging foundation;
3. introduce the Gateway as the UI entry point;
4. update the UI to call the Gateway instead of individual services;
5. separate the UI HTTP/API client layer from React components;
6. continue Phase 1 endpoint-by-endpoint through the Gateway.

The Gateway and Request Logger are treated as Phase 1 technical architecture work, not as standalone product features.

## Resolved Decisions

### 1. Technical logger and audit trail are separate systems

The `RequestLogger` is not a replacement for the audit trail.

The technical logger records request/response troubleshooting information.

The audit service records business events that matter for traceability, accountability, and document-control history.

Business actions such as document creation, revision creation, status changes, transmittals, superseding, and approvals remain the responsibility of domain services and the audit service.

### 2. RequestLogger should be production-oriented from the start

The technical request logger should not be treated as throwaway MVP code.

It should define the production logging behavior expected from backend services and the Gateway.

The initial implementation should include:

- request id generation when missing;
- request id propagation when present;
- `X-Request-Id` response header;
- MDC usage for consistent logging context;
- service name;
- HTTP method;
- request path;
- response status;
- request duration;
- exception summary when applicable;
- awareness of forwarded headers where relevant.

### 3. Authentication is explicitly out of scope for this decision

This ADR does not introduce authentication or authorization.

The request logger must work before identity is available.

When authentication is added later, the logger may include authenticated user context, tenant/project context, or permission context where safe and appropriate.

Until then, it should not pretend to know the real user identity.

### 4. Internal service signatures are out of scope for now

This ADR does not introduce internal service signing, service identity, or trusted-service verification.

Feign calls should propagate the request id, but they should not yet implement internal authentication or signing.

Service-to-service trust will be handled by a later security decision.

### 5. Gateway becomes the frontend entry boundary during Phase 1

The UI should stop calling individual backend services directly once the Gateway is introduced.

The intended request path becomes:

```text
React UI
→ Gateway
→ internal backend services
```

This gives the project one place to handle inbound request logging, request id behavior, routing, frontend-facing error behavior, and later authentication.

### 6. UI HTTP logic should be separated from components

When the Gateway is introduced, the React UI should separate HTTP/API calls from React rendering components.

The goal is not to build a large frontend architecture or install a data-fetching library.

The goal is only to avoid scattering raw `fetch` calls, service URLs, headers, and response parsing across components.

Phase 1 UI conventions still apply:

- desktop-first only;
- no responsive work;
- no unnecessary libraries;
- minimal state;
- simple visible loading, success, error, and disabled states.

### 7. Gateway is a Phase 1 technical decision, not a roadmap scope expansion

The Gateway is not added because Phase 1 product scope changed.

The Phase 1 product scope remains the Document Control Section MVP.

The Gateway is added because the architecture now needs a stable application entry point before more services, endpoints, and UI panels are built.

This keeps future Phase 1 work cleaner without expanding into Phase 2 workflows or AI features.

### 8. Logging infrastructure beyond application logs is deferred

The implementation should not attempt to build a full observability platform now.

The following are deferred:

- centralized log aggregation;
- dashboards;
- alert rules;
- distributed tracing platform;
- metrics platform;
- long-term retention policy implementation.

The application should still produce structured, consistent logs that can be consumed by such infrastructure later.

## Technical Notes

The first implementation should likely include two related concerns:

### Inbound request logging

Used by backend services and later by the Gateway.

Expected behavior:

```text
if X-Request-Id exists:
    use it
else:
    generate a new request id

put request id into MDC
add X-Request-Id to response
log request completion with method, path, status, duration, service name
log exception summary with request id when applicable
clear MDC at the end of the request
```

### Outbound request id propagation

Used by Feign clients.

Expected behavior:

```text
read request id from MDC
if present:
    add X-Request-Id to outbound request headers
```

Without propagation, logs from `documents-service` and `audit-service` cannot be reliably connected during troubleshooting.

## Consequences

### Positive

- The UI gets a single backend entry point before it grows.
- CORS and frontend-facing HTTP behavior become easier to centralize.
- Technical troubleshooting becomes easier across services.
- Request ids can connect logs across UI, Gateway, `documents-service`, and `audit-service`.
- Future authentication has a natural place to enter the system.
- Future services can follow the same logging expectations from the beginning.
- The project avoids retrofitting request logging after many endpoints already exist.

### Risks

- Gateway work can slow down visible product progress if it becomes too broad.
- Request logging can accidentally capture sensitive data if request bodies, query strings, or headers are logged carelessly.
- The team may confuse technical logs with business audit events if boundaries are not enforced.
- Gateway introduction may create routing complexity before authentication exists.
- A production-oriented logger can become overdesigned if it expands into full observability infrastructure too early.

## Action Items

1. Complete the immediate read-document and read-audit-event slice before starting the Gateway/logger branch.
2. Create a dedicated Phase 1 branch for technical request logging and Gateway introduction.
3. Implement `RequestLogger` behavior with request id handling and MDC.
4. Propagate `X-Request-Id` through Feign clients.
5. Add `X-Request-Id` visibility to the UI error path.
6. Introduce the Gateway as the UI entry point.
7. Move UI HTTP calls into a small API/client layer.
8. Continue remaining Phase 1 endpoints through the Gateway.
