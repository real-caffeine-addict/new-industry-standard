package com.psw.gateway.utility;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.psw.common.enums.AlertSeverity;
import com.psw.gateway.GatewayApplication;
import com.psw.gateway.model.AlertRecord;
import com.psw.gateway.model.RequestContext;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Requires isolated incident-service + test DB")
@SpringBootTest(
        classes = GatewayApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "spring.cloud.compatibility-verifier.enabled=false")
class IncidentFlowEndToEndTests {
    private static final String MODE = System.getProperty("psw.e2e.mode", "success");
    private static final String INCIDENT_URL = System.getProperty(
            "psw.e2e.incident-url", "http://127.0.0.1:18899");

    @Autowired
    private GwLogger gwLogger;

    private final ListAppender<ILoggingEvent> requestLogs = new ListAppender<>();
    private final ListAppender<ILoggingEvent> alertLogs = new ListAppender<>();

    @DynamicPropertySource
    static void isolatedProperties(DynamicPropertyRegistry properties) {
        properties.add("spring.config.import", () -> "optional:classpath:e2e-unused.yaml");
        properties.add("routes.incident", () -> INCIDENT_URL);
    }

    @BeforeEach
    void captureLogs() {
        requestLogs.start();
        alertLogs.start();
        requestLogger().addAppender(requestLogs);
        alertLogger().addAppender(alertLogs);
    }

    @AfterEach
    void detachLogs() {
        requestLogger().detachAppender(requestLogs);
        alertLogger().detachAppender(alertLogs);
    }

    @Test
    void successfulIncidentFlowPreservesAllCorrelationIds() {
        Assumptions.assumeTrue(MODE.equals("success"));
        UUID requestId = UUID.randomUUID();
        AlertRecord alert = qualifyingAlert(requestId);

        Long incidentId = gwLogger.logRequest(context(requestId, alert));

        assertThat(incidentId).isPositive();
        assertThat(requestLogs.list).hasSize(1);
        assertThat(alertLogs.list).hasSize(1);
        assertThat(argument(requestLogs.list.getFirst()))
                .contains(requestId.toString())
                .contains("alerts");
        assertThat(argument(alertLogs.list.getFirst()))
                .contains(requestId.toString())
                .contains(alert.logId().toString())
                .contains(incidentId.toString());
    }

    @Test
    void unavailableIncidentServiceStillWritesCorrelatedLocalLogsWithoutFalseId() {
        Assumptions.assumeTrue(MODE.equals("service-unavailable"));
        assertPersistenceFailureContract();
    }

    @Test
    void unavailableDatabaseStillWritesCorrelatedLocalLogsWithoutFalseId() {
        Assumptions.assumeTrue(MODE.equals("database-unavailable"));
        assertPersistenceFailureContract();
    }

    private void assertPersistenceFailureContract() {
        UUID requestId = UUID.randomUUID();
        AlertRecord alert = qualifyingAlert(requestId);

        Long incidentId = gwLogger.logRequest(context(requestId, alert));

        assertThat(incidentId).isNull();
        assertThat(requestLogs.list).hasSize(1);
        assertThat(alertLogs.list).hasSize(1);
        assertThat(argument(requestLogs.list.getFirst())).contains(requestId.toString());
        assertThat(argument(alertLogs.list.getFirst()))
                .contains(requestId.toString())
                .contains("Failed to log incident")
                .contains("incidentId=null");
    }

    private AlertRecord qualifyingAlert(UUID requestId) {
        return new AlertRecord(
                UUID.randomUUID(), requestId, null, Instant.now(), AlertSeverity.MAJOR,
                "Gateway E2E", "Downstream service is unavailable",
                Map.of("downstream", "projects"));
    }

    private RequestContext context(UUID requestId, AlertRecord alert) {
        return RequestContext.builder()
                .requestId(requestId)
                .peerIp("127.0.0.1")
                .importantHeaders(Map.of())
                .otherHeaders(Map.of())
                .path("/e2e")
                .method("GET")
                .status(503)
                .startedAt(Instant.now())
                .elapsed(1L)
                .alerts(new ArrayList<>(java.util.List.of(alert)))
                .build();
    }

    private String argument(ILoggingEvent event) {
        return event.getArgumentArray()[0].toString();
    }

    private Logger requestLogger() {
        return (Logger) LoggerFactory.getLogger("logRequest");
    }

    private Logger alertLogger() {
        return (Logger) LoggerFactory.getLogger("alertLogger");
    }
}
