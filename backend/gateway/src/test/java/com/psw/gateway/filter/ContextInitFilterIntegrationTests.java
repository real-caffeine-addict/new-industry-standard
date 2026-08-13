package com.psw.gateway.filter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.psw.common.enums.AlertSeverity;
import com.psw.gateway.model.AlertRecord;
import com.psw.gateway.model.RequestContext;
import com.psw.gateway.utility.ContextInitUtil;
import com.psw.gateway.utility.ExceptionParserUtil;
import com.psw.gateway.utility.GwLogger;
import com.psw.gateway.utility.IncidentHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ContextInitFilterIntegrationTests {
    private final IncidentHandler incidentHandler = mock(IncidentHandler.class);
    private final GwLogger gwLogger = new GwLogger(incidentHandler, new ExceptionParserUtil());
    private final ContextInitFilter filter = new ContextInitFilter(
            new ContextInitUtil(), gwLogger, new ExceptionParserUtil());
    private final ListAppender<ILoggingEvent> requestLogs = new ListAppender<>();
    private final ListAppender<ILoggingEvent> alertLogs = new ListAppender<>();

    @BeforeEach
    void captureTechnicalLogs() {
        requestLogs.start();
        alertLogs.start();
        requestLogger().addAppender(requestLogs);
        alertLogger().addAppender(alertLogs);
    }

    @AfterEach
    void stopCapturingTechnicalLogs() {
        requestLogger().detachAppender(requestLogs);
        alertLogger().detachAppender(alertLogs);
    }

    @Test
    void createsAndEnrichesOneContextForTheWholeLifecycle() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/projects/12");
        request.setRemoteAddr("192.0.2.4");
        request.addHeader("X-Forwarded-For", "198.51.100.7");
        request.addHeader("Authorization", "Bearer secret");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<RequestContext> observed = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> {
            RequestContext context = (RequestContext) req.getAttribute("context");
            observed.set(context);
            context.setResponseBody("{\"accepted\":true}");
            ((HttpServletResponse) res).setStatus(202);
        });

        RequestContext context = observed.get();
        assertThat(request.getAttribute("context")).isSameAs(context);
        assertThat(context.getRequestId()).isNotNull();
        assertThat(context.getMethod()).isEqualTo("POST");
        assertThat(context.getPath()).isEqualTo("/projects/12");
        assertThat(context.getPeerIp()).isEqualTo("192.0.2.4");
        assertThat(context.getForwardedIp()).isEqualTo("198.51.100.7");
        assertThat(context.getImportantHeaders().get("Authorization"))
                .containsExactly("[redacted]");
        assertThat(context.getStatus()).isEqualTo(202);
        assertThat(context.getElapsed()).isNotNegative();
        assertThat(response.getStatus()).isEqualTo(202);
        assertThat(response.getContentAsString()).isEqualTo("{\"accepted\":true}");
        assertThat(requestLogs.list).hasSize(1);
    }

    @Test
    void requestAndAlertLogsRetainTheSameRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/alerts");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<UUID> requestId = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> {
            RequestContext context = (RequestContext) req.getAttribute("context");
            requestId.set(context.getRequestId());
            context.getAlerts().add(new AlertRecord(
                    UUID.randomUUID(), context.getRequestId(), null, Instant.now(),
                    AlertSeverity.INFO, "test", "diagnostic", Map.of()));
        });

        assertThat(requestLogs.list).hasSize(1);
        assertThat(alertLogs.list).hasSize(1);
        assertThat(structuredArgument(requestLogs.list.getFirst())).contains(requestId.get().toString());
        assertThat(structuredArgument(alertLogs.list.getFirst())).contains(requestId.get().toString());
    }

    @Test
    void downstreamExceptionProducesErrorResponseAndTechnicalLogs() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/failure");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            throw new ServletException("downstream failed");
        });

        RequestContext context = (RequestContext) request.getAttribute("context");
        assertThat(context.getStatus()).isEqualTo(500);
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getContentAsString()).isEqualTo("Internal server error");
        assertThat(requestLogs.list).hasSize(1);
        assertThat(alertLogs.list).hasSize(1);
        assertThat(structuredArgument(requestLogs.list.getFirst())).contains(context.getRequestId().toString());
        assertThat(structuredArgument(alertLogs.list.getFirst())).contains(context.getRequestId().toString());
    }

    @Test
    void committedDownstreamResponseIsNotRewrittenAfterException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/committed");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            ((HttpServletResponse) res).setStatus(201);
            res.getWriter().write("already sent");
            res.flushBuffer();
            throw new ServletException("late failure");
        });

        assertThat(response.isCommitted()).isTrue();
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getContentAsString()).isEqualTo("already sent");
        assertThat(requestLogs.list).hasSize(1);
    }

    @Test
    void doesNotAppendFallbackBodyToDownstreamOwnedResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/body");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            res.setContentType("application/json");
            res.getWriter().write("{\"value\":1}");
        });

        assertThat(response.getContentAsString()).isEqualTo("{}");
        assertThat(requestLogs.list).hasSize(1);
    }

    @Test
    void responseBodyIsRedactedInCanonicalRequestLog() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            RequestContext context = (RequestContext) req.getAttribute("context");
            context.setResponseBody("token=do-not-log");
        });

        assertThat(structuredArgument(requestLogs.list.getFirst()))
                .contains("[REDACTED]")
                .doesNotContain("do-not-log");
    }

    @Test
    void concurrentRequestsDoNotShareContextOrRequestIds() throws Exception {
        AtomicReference<RequestContext> firstContext = new AtomicReference<>();
        AtomicReference<RequestContext> secondContext = new AtomicReference<>();
        CountDownLatch bothInsideChain = new CountDownLatch(2);
        CountDownLatch releaseChains = new CountDownLatch(1);

        Thread first = requestThread("/concurrent/one", firstContext, bothInsideChain, releaseChains);
        Thread second = requestThread("/concurrent/two", secondContext, bothInsideChain, releaseChains);
        first.start();
        second.start();

        assertThat(bothInsideChain.await(5, TimeUnit.SECONDS)).isTrue();
        releaseChains.countDown();
        first.join(5_000);
        second.join(5_000);

        assertThat(first.isAlive()).isFalse();
        assertThat(second.isAlive()).isFalse();
        assertThat(firstContext.get()).isNotSameAs(secondContext.get());
        assertThat(firstContext.get().getRequestId())
                .isNotEqualTo(secondContext.get().getRequestId());
    }

    private Thread requestThread(String path,
                                 AtomicReference<RequestContext> observed,
                                 CountDownLatch bothInsideChain,
                                 CountDownLatch releaseChains) {
        return new Thread(() -> {
            try {
                filter.doFilter(new MockHttpServletRequest("GET", path),
                        new MockHttpServletResponse(), (request, response) -> {
                            observed.set((RequestContext) request.getAttribute("context"));
                            bothInsideChain.countDown();
                            try {
                                if (!releaseChains.await(5, TimeUnit.SECONDS)) {
                                    throw new ServletException("timed out waiting for concurrent request");
                                }
                            } catch (InterruptedException exception) {
                                Thread.currentThread().interrupt();
                                throw new ServletException("concurrent request interrupted", exception);
                            }
                        });
            } catch (Exception exception) {
                throw new AssertionError(exception);
            }
        });
    }

    private String structuredArgument(ILoggingEvent event) {
        return event.getArgumentArray()[0].toString();
    }

    private Logger requestLogger() {
        return (Logger) LoggerFactory.getLogger("logRequest");
    }

    private Logger alertLogger() {
        return (Logger) LoggerFactory.getLogger("alertLogger");
    }
}
