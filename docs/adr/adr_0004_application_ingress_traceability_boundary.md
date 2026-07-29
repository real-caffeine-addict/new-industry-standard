# ADR-0004: Application Ingress Traceability Boundary

## Status

Accepted

## Context

Project Signal Wizard is entering a phase where the Gateway and the production technical request logger will become part of the Phase 1 application boundary.

The system will eventually run behind Apache. Apache is the external web-server / reverse-proxy boundary. The application boundary starts at the Gateway.

The project requires full technical traceability for every request that passes Apache and reaches the application. Requests that fail before reaching the application are outside the application trace boundary and must be covered by Apache, systemd, and system journal logs.

The technical request logger is separate from the business audit trail:

- technical request logging explains how an HTTP request moved through the system;
- business audit events record user/business actions on controlled project records.

The request logger must not be treated as a simple console log. It is an application ingress trace layer.

The key design requirement is:

> Every valid HTTP request that reaches the Gateway must receive a request context as early as possible, carry that context through the Gateway lifecycle, enrich it along the way, and emit one structured technical log at the end of the request lifecycle.

Spring Cloud Gateway introduces a WebFlux runtime instead of the Servlet/MVC runtime used by the regular backend services. This means the Gateway does not use `HttpServletRequest`, `HttpServletResponse`, or Servlet filters. The equivalent application-level request context is based on `ServerWebExchange`, WebFlux `WebFilter`s, and Gateway `GlobalFilter`s.

## Decision

Introduce an explicit **Application Ingress Traceability Boundary** at the Gateway.

The Gateway is responsible for creating and managing a technical `RequestContext` for every valid HTTP request that reaches the application after Apache.

The request context must be created at the earliest application-controlled point in the Gateway request lifecycle.

The request context must then be enriched as the request moves through Gateway routing, downstream service calls, error paths, and response handling.

At the end of the request lifecycle, the request context must be converted into one structured technical request log.

This log is the canonical technical trace for the application-level request lifecycle.

Business audit events remain separate and must not be merged with technical request logging.

## Considered Alternatives

### 1. Keep logging only in downstream services

Rejected.

Service-level logs cannot prove that every request entering the application was observed. A request may fail in the Gateway, during route matching, before reaching a downstream service, or while forwarding to a service.

This approach would leave blind spots between Apache and the backend services.

### 2. Use only Apache access/error logs

Rejected.

Apache logs are required for protocol-edge and pre-application traceability, but they cannot describe application routing, Gateway decisions, downstream service calls, retries, internal errors, or application-level context enrichment.

Apache logs and Gateway request logs must complement each other, not replace each other.

### 3. Use a normal application logger in controllers/services

Rejected.

Scattered log statements do not create a reliable request lifecycle record. They also encourage inconsistent fields, missing request IDs, and duplicate or contradictory logs.

The project requires one canonical application-level technical request log per Gateway request.

### 4. Build a Servlet/MVC Gateway manually

Rejected for now.

A Servlet/MVC Gateway would allow use of familiar `HttpServletRequest`, `HttpServletResponse`, and Servlet filters. However, it would require manually implementing proxy behavior such as routing, method preservation, headers, request bodies, response bodies, status propagation, multipart handling, timeouts, and future route policies.

The project should not build and maintain its own HTTP proxy framework unless Spring Cloud Gateway proves unable to satisfy the traceability requirement.

### 5. Intercept traffic at Netty `ChannelHandler` level

Rejected for Phase 1.

This would provide a lower-level interception point, closer to decoded TCP/HTTP traffic, but it would move the project into Netty internals, connection reuse, byte buffers, HTTP decoder order, backpressure, and protocol-level handling.

Protocol-edge failures remain the responsibility of Apache, systemd, and system journal logs. The application logger starts at the earliest practical Spring/WebFlux point.

### 6. Use Spring Cloud Gateway WebFlux with explicit trace lifecycle

Accepted.

Spring Cloud Gateway provides routing and proxy mechanics while allowing the project to define its own request context, request ID policy, logging structure, and enrichment rules.

This keeps product-specific control in project code while avoiding a custom proxy framework.

## Resolved Decisions

### 1. Traceability boundary

Apache owns protocol-edge and pre-application failures.

This includes, for example:

- TCP / connection-level failures;
- malformed HTTP rejected before the application;
- TLS handling if terminated at Apache;
- Apache-level access and error logging.

The Gateway owns every valid HTTP request that reaches the application boundary.

### 2. Request ID creation

Every request that reaches the Gateway must have a request ID.

The application-level request ID header is:

```text
X-Request-Id
```

If `X-Request-Id` exists on the incoming request, the Gateway may reuse it only if it passes validation.

For Phase 1, accepted request IDs must be treated as untrusted external input and should be validated for length and safe characters before reuse.

If no valid request ID exists, the Gateway must generate one.

The request ID must be attached to the request context and returned on the response where possible.

### 3. Request context ownership

The Gateway owns the application-level request context.

The context must be attached to the WebFlux request lifecycle through `ServerWebExchange` attributes.

Gateway filters and later infrastructure components must enrich the existing context rather than creating unrelated logging records.

Downstream services should receive the request ID and may log it in their own technical logs, but they do not own the Gateway request context.

### 4. Required request context data

The initial request context must include at least:

- request ID;
- application service name;
- start timestamp;
- HTTP method;
- original path;
- query string where relevant;
- request headers as seen by the Gateway after Apache, subject to redaction rules;
- source IP / forwarded IP chain where available;
- user agent where available.

Later enrichment must include at least:

- matched Gateway route;
- downstream target service / URI;
- downstream status where available;
- final response status;
- duration in milliseconds;
- error summary where applicable;
- retry count where retries are explicitly configured;
- retry error summaries where applicable.

### 5. Header privacy and redaction

Request headers may contain sensitive or identifying data.

The logger must not blindly persist all header values in clear text.

The following headers must be redacted before being written to technical logs:

- `Authorization`;
- `Cookie`;
- `Set-Cookie`;
- `Proxy-Authorization`;
- `X-Api-Key`;
- `Api-Key`;
- any header whose name contains `token`, `secret`, `password`, or `credential`, case-insensitive.

For redacted headers, the log may keep the header name and a marker such as:

```text
[REDACTED]
```

The logger may record non-sensitive operational headers as seen by the Gateway after Apache.

TODO: Define the long-term retention period for technical request logs before production deployment.

TODO: Review privacy requirements before logging personally identifying headers, IP addresses, or user-agent values in production environments.

### 6. No hidden retries

Retries must not be enabled implicitly or treated as invisible infrastructure behavior.

If retries are introduced, they must be configured intentionally and must enrich the request context with retry count and retry failure information.

For Phase 1, no Gateway retry policy is enabled unless explicitly implemented and documented.

### 7. Early responses

The system must minimize framework-generated early responses before request context creation.

Default authentication, default CORS behavior, or other framework defaults must not be allowed to produce application responses before the Gateway request context exists.

For Phase 1, Gateway security must be explicitly configured as permit-all, because authentication is not implemented yet.

If a response is committed before the final logging stage, the logger must still attempt to emit the best available technical trace and clearly reflect the observed status or failure state.

The system must not pretend that every framework or protocol-level failure can be rewritten into a custom response once the response has already been committed.

### 8. WebFlux Gateway model

Because Spring Cloud Gateway runs on WebFlux, Gateway request lifecycle code must use WebFlux/Gateway primitives rather than Servlet primitives.

The relevant concepts are:

- `ServerWebExchange` for request/response context;
- WebFlux `WebFilter` for the earliest application-controlled request lifecycle work;
- Spring Cloud Gateway `GlobalFilter` for route/proxy lifecycle enrichment;
- Gateway route filters for route-specific behavior where needed.

Servlet/MVC infrastructure beans from `common` must not be loaded into the Gateway runtime.

Common Servlet/MVC beans must be guarded by configuration such as:

```text
psw.mvc-beans.enabled=false
```

for the Gateway runtime.

### 9. Separation from business audit

Technical request logs are not business audit events.

Business audit events continue to record controlled project actions such as document creation, revision changes, uploads, status changes, and other business operations.

Technical request logging may reference the request ID and route/service behavior, but it must not replace business audit records.

### 10. ADR numbering policy

ADR numbers are sequential and assigned when the ADR is introduced into the repository.

Accepted ADR numbers must not be reused for different decisions.

If an ADR file is created with the wrong number before merge, it should be renamed before merge.

After merge, avoid renumbering accepted ADRs unless the repository history is still private and the correction is explicitly intentional.

## Consequences

### Positive

- Every application-level request has a consistent technical trace.
- Troubleshooting becomes possible across Gateway and downstream services.
- Request ID propagation can become a standard requirement for all services.
- Gateway logging becomes a deliberate lifecycle design rather than scattered log statements.
- Business audit remains clean and separate from technical logging.
- Future authentication, permissions, retries, and error handling can enrich the same request context instead of creating disconnected logs.

### Risks

- WebFlux/Gateway lifecycle is different from the Servlet/MVC lifecycle used by the regular services.
- The team must understand `ServerWebExchange`, WebFlux filters, Gateway global filters, and response commit timing.
- Some protocol-level or framework-level failures may occur before the application request context exists.
- Response rewriting is not always possible after a response has been committed.
- Overloading the request context with too much data may create noisy logs or sensitive-data exposure.
- Header logging can create privacy and security risk if redaction is incomplete.

### Mitigations

- Keep Apache/systemd/journal responsible for pre-application failures.
- Create request context at the earliest practical Gateway point.
- Disable or override framework defaults that can produce unwanted early application responses.
- Start with context creation, request ID, routing details, status, duration, and error summary before adding advanced behavior.
- Apply header redaction before any header values are written to logs.
- Keep retries explicit and observable.
- Do not add Netty-level interception in Phase 1 unless WebFlux-level traceability proves insufficient.

## Action Items

1. Add Gateway-specific WebFlux security configuration for Phase 1 permit-all behavior.
2. Ensure Servlet/MVC common beans are disabled for the Gateway runtime.
3. Define the initial `RequestContext` structure.
4. Add an early Gateway WebFilter to create and attach the request context.
5. Add a Gateway GlobalFilter to enrich the context with route/proxy outcome data.
6. Emit one structured technical request log at the end of the request lifecycle.
7. Propagate `X-Request-Id` to downstream services.
8. Update UI HTTP calls to go through the Gateway.
9. Define production technical-log retention before production deployment.
10. Review header/IP/user-agent privacy policy before production deployment.
