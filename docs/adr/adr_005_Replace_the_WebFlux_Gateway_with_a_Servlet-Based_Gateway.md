# ADR-0005: Replace the WebFlux Gateway with a Servlet-Based Gateway

## Status

Accepted

## Supersedes

ADR-0004: Application Ingress Traceability Boundary

## Context

ADR-0004 selected Spring Cloud Gateway and WebFlux for the application Gateway.

During implementation, this introduced a request-processing model that differs substantially from the Spring MVC, Servlet, and Feign stack already used by the development team.

The team currently has greater experience and operational confidence with the Servlet stack.

WebFlux also made the intended Gateway lifecycle harder to express clearly. The Gateway requires explicit ownership of the request context and final HTTP response throughout the complete request lifecycle.

The expected benefit of WebFlux did not justify the added learning, implementation, and maintenance complexity.

## Decision

Replace Spring Cloud Gateway and WebFlux with a Gateway implemented using Spring MVC and the Servlet stack.

The Gateway will use:

* a highest-precedence Servlet filter;
* request attributes for the technical request context;
* Feign for downstream service calls;
* structured internal processing results;
* one final technical request log per inbound request.

WebFlux is rejected as the Gateway implementation model.

## Request Lifecycle

```text
Request enters Gateway
        ↓
Filter creates RequestContext
        ↓
Routing and downstream processing
        ↓
RequestContext is enriched
        ↓
Processing result returns to filter
        ↓
Filter writes HTTP response
        ↓
Filter writes final technical log
```

The outer filter is the sole owner of the final `HttpServletResponse`.

Internal Gateway components must not commit the response directly.

## Traceability Boundary

The external web server or cloud ingress layer owns failures that occur before the request reaches the application.

The Gateway owns every valid HTTP request that reaches the Servlet application.

Technical request logging remains separate from the business audit trail.

## Considered Alternatives

### Spring Cloud Gateway with WebFlux

Rejected.

It introduces a second backend programming model, requires skills not currently established in the development team, and complicates the required request and response lifecycle ownership.

### External web-server routing only

Rejected.

It cannot manage the application request context, downstream processing outcome, final application response, and canonical technical request log.

### No Gateway

Rejected.

The system still requires one application entry boundary for routing, traceability, request identifiers, response control, and future security.

## Consequences

### Positive

* The Gateway uses the same programming model as the rest of the backend.
* The implementation matches the current skills and experience of the development team.
* The request lifecycle and response ownership are explicit.
* Feign and existing backend conventions remain usable.
* Development and debugging remain within one familiar stack.

### Risks

* The outer filter may accumulate too many responsibilities.
* Internal components may accidentally bypass the response-ownership rule.
* Routing behavior may become inconsistent if it is not defined through clear contracts.

### Mitigations

* Keep routing, context enrichment, response construction, and logging in separate collaborators.
* Enforce a structured processing-result contract.
* Add tests verifying that the response is committed only once and that every request produces one technical log.

## Action Items

1. Mark ADR-0004 as superseded by ADR-0005.
2. Remove Spring Cloud Gateway and WebFlux from the Gateway.
3. Convert the Gateway to Spring MVC and Servlet infrastructure.
4. Replace WebFlux filters with a highest-precedence Servlet filter.
5. Define the structured processing-result contract.
6. Preserve request-ID propagation through Feign.
