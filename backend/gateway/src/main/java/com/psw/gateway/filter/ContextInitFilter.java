package com.psw.gateway.filter;

import com.psw.gateway.model.AlertRecord;
import com.psw.gateway.model.RequestContext;
import com.psw.common.enums.AlertSeverity;
import com.psw.gateway.utility.ContextInitUtil;
import com.psw.gateway.utility.ExceptionParserUtil;
import com.psw.gateway.utility.GwLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ContextInitFilter extends OncePerRequestFilter {
    private final ContextInitUtil util;
    private final GwLogger logger;
    private final ExceptionParserUtil parser;

    public ContextInitFilter(ContextInitUtil util,
                             GwLogger logger,
                             ExceptionParserUtil parser) {
        this.util = util;
        this.logger = logger;
        this.parser = parser;
    }

    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        RequestContext ctx = RequestContext.builder()
                .requestId(UUID.randomUUID())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .startedAt(Instant.now())
                .build();

        request.setAttribute("context", ctx);

        util.ipResolver(request, ctx);
        util.headerResolver(request, ctx);
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            ctx.setStatus(500);
            ctx.setResponseBody("Internal server error");
            ctx.getAlerts().add(new AlertRecord(
                    UUID.randomUUID(),
                    ctx.getRequestId(),
                    null,
                    Instant.now(),
                    AlertSeverity.MAJOR,
                    "ContextInitFilter",
                    "Unknown exception thrown",
                    parser.exceptionParser(e)
            ));
        } finally {
            if (ctx.getStatus() == 0) {
                ctx.setStatus(response.getStatus());
            }
            ctx.setElapsed(Duration.between(ctx.getStartedAt(),Instant.now()).toMillis());
            Long xLogId = logger.logRequest(ctx);
            if (xLogId != null) {
                response.setHeader("X-log-ID", xLogId.toString());
            }
            if (response.isCommitted()) {
                logger.logResponseOwnershipViolation(ctx.getRequestId());
            }else {
                response.setStatus(ctx.getStatus());
                response.resetBuffer();
                if (ctx.getResponseBody() != null && !ctx.getResponseBody().isEmpty()) {
                    response.getWriter().write(ctx.getResponseBody());
                } else {
                    response.getWriter().write("{}");
                }
            }
        }
    }
}
