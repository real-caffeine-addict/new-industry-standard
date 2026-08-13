package com.psw.gateway.utility;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.psw.common.enums.AlertSeverity;
import com.psw.gateway.model.AlertRecord;
import com.psw.gateway.model.RequestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class GwLoggerTests {
    private final IncidentHandler incidentHandler = mock(IncidentHandler.class);
    private final GwLogger logger = new GwLogger(incidentHandler, new ExceptionParserUtil());
    private final ListAppender<ILoggingEvent> requestAppender = new ListAppender<>();
    private final ListAppender<ILoggingEvent> alertAppender = new ListAppender<>();

    @BeforeEach
    void attachAppenders() {
        requestAppender.start();
        alertAppender.start();
        ((Logger) LoggerFactory.getLogger("logRequest")).addAppender(requestAppender);
        ((Logger) LoggerFactory.getLogger("alertLogger")).addAppender(alertAppender);
    }

    @AfterEach
    void detachAppenders() {
        ((Logger) LoggerFactory.getLogger("logRequest")).detachAppender(requestAppender);
        ((Logger) LoggerFactory.getLogger("alertLogger")).detachAppender(alertAppender);
    }

    @Test
    void requestWithoutAlertsWritesOneRequestLogAndReturnsNoIncidentId() {
        Long result = logger.logRequest(context(List.of()));

        assertThat(result).isNull();
        assertThat(requestAppender.list).hasSize(1);
        assertThat(alertAppender.list).isEmpty();
    }

    @Test
    void nonSevereAlertsAreEachEmittedOnceAtTheirMappedLevels() {
        List<AlertRecord> alerts = List.of(
                alert(AlertSeverity.INFO, "info"),
                alert(AlertSeverity.WARN, "warn"),
                alert(AlertSeverity.MINOR, "minor"));

        logger.logRequest(context(alerts));

        assertThat(alertAppender.list).extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.INFO, Level.WARN, Level.ERROR);
        verify(incidentHandler, never()).createIncidentIfRequired(org.mockito.ArgumentMatchers.any());
        assertThat(requestAppender.list.getFirst().getArgumentArray()[0].toString())
                .contains("alerts").contains("3");
    }

    @Test
    void severeAlertWithoutIncidentIsLoggedOnceAndReturnsNoId() {
        AlertRecord alert = alert(AlertSeverity.MAJOR, "major");
        when(incidentHandler.createIncidentIfRequired(alert)).thenReturn(null);

        assertThat(logger.logRequest(context(List.of(alert)))).isNull();

        assertThat(alertAppender.list).hasSize(1);
        verify(incidentHandler).createIncidentIfRequired(alert);
    }

    @Test
    void severeAlertWithIncidentIsLoggedOnceAndReturnsId() {
        AlertRecord alert = alert(AlertSeverity.CRITICAL, "critical");
        AlertRecord persisted = new AlertRecord(alert.logId(), alert.requestId(), 91L,
                alert.timestamp(), alert.severity(), alert.sourceComponent(),
                alert.description(), alert.debuggingData());
        when(incidentHandler.createIncidentIfRequired(alert)).thenReturn(persisted);

        assertThat(logger.logRequest(context(List.of(alert)))).isEqualTo(91L);

        assertThat(alertAppender.list).hasSize(1);
    }

    @Test
    void persistenceFailureAlertIsLoggedOnceAndDoesNotExposeIncidentId() {
        AlertRecord alert = alert(AlertSeverity.MAJOR, "major");
        AlertRecord fallback = new AlertRecord(UUID.randomUUID(), alert.requestId(), null,
                Instant.now(), AlertSeverity.CRITICAL, "Incident service",
                "Failed to log incident", Map.of("message", "offline"));
        when(incidentHandler.createIncidentIfRequired(alert)).thenReturn(fallback);

        assertThat(logger.logRequest(context(List.of(alert)))).isNull();
        assertThat(alertAppender.list).hasSize(1);
    }

    @Test
    void multipleSevereAlertsCreateOneSyntheticAlertAndProcessEachOriginalOnce() {
        AlertRecord first = alert(AlertSeverity.MAJOR, "first");
        AlertRecord second = alert(AlertSeverity.CRITICAL, "second");
        AlertRecord synthetic = new AlertRecord(UUID.randomUUID(), first.requestId(), 7L,
                Instant.now(), AlertSeverity.CRITICAL, "GwLogger",
                "Request had more than one MAJOR or CRITICAL alert", null);
        when(incidentHandler.createIncidentIfRequired(org.mockito.ArgumentMatchers.argThat(
                item -> item.description().equals("Request had more than one MAJOR or CRITICAL alert"))))
                .thenReturn(synthetic);
        when(incidentHandler.createIncidentIfRequired(first)).thenReturn(null);
        when(incidentHandler.createIncidentIfRequired(second)).thenReturn(null);

        assertThat(logger.logRequest(context(List.of(first, second)))).isEqualTo(7L);

        assertThat(alertAppender.list).hasSize(3);
        verify(incidentHandler).createIncidentIfRequired(first);
        verify(incidentHandler).createIncidentIfRequired(second);
    }

    @Test
    void unicodeAndMultilineDiagnosticsRemainInStructuredAlertData() {
        UUID requestId = UUID.randomUUID();
        AlertRecord alert = new AlertRecord(
                UUID.randomUUID(), requestId, null, Instant.now(), AlertSeverity.INFO,
                "gateway", "תקלה – failure", Map.of("diagnostic", "שורה ראשונה\nsecond line ☃"));

        logger.logRequest(context(List.of(alert)));

        assertThat(alertAppender.list).hasSize(1);
        assertThat(alertAppender.list.getFirst().getArgumentArray()[0].toString())
                .contains("תקלה – failure")
                .contains("שורה ראשונה\nsecond line ☃");
    }

    @Test
    void severalQualifyingAlertsAreEachProcessedExactlyOnce() {
        AlertRecord first = alert(AlertSeverity.MAJOR, "first");
        AlertRecord second = new AlertRecord(UUID.randomUUID(), first.requestId(), null,
                Instant.now(), AlertSeverity.MAJOR, "test", "second", null);
        AlertRecord third = new AlertRecord(UUID.randomUUID(), first.requestId(), null,
                Instant.now(), AlertSeverity.CRITICAL, "test", "third", null);
        AlertRecord synthetic = new AlertRecord(UUID.randomUUID(), first.requestId(), null,
                Instant.now(), AlertSeverity.CRITICAL, "GwLogger",
                "Request had more than one MAJOR or CRITICAL alert", null);
        when(incidentHandler.createIncidentIfRequired(org.mockito.ArgumentMatchers.argThat(
                item -> item.description().equals("Request had more than one MAJOR or CRITICAL alert"))))
                .thenReturn(synthetic);

        logger.logRequest(context(List.of(first, second, third)));

        verify(incidentHandler, times(1)).createIncidentIfRequired(first);
        verify(incidentHandler, times(1)).createIncidentIfRequired(second);
        verify(incidentHandler, times(1)).createIncidentIfRequired(third);
        assertThat(alertAppender.list).hasSize(4);
    }

    private RequestContext context(List<AlertRecord> alerts) {
        UUID requestId = alerts.isEmpty() ? UUID.randomUUID() : alerts.getFirst().requestId();
        return RequestContext.builder()
                .requestId(requestId).peerIp("127.0.0.1").forwardedIp("10.0.0.1")
                .importantHeaders(Map.of()).otherHeaders(Map.of()).path("/test").method("GET")
                .status(200).startedAt(Instant.now()).elapsed(1L)
                .alerts(new ArrayList<>(alerts)).build();
    }

    private AlertRecord alert(AlertSeverity severity, String description) {
        return new AlertRecord(UUID.randomUUID(), UUID.randomUUID(), null, Instant.now(),
                severity, "test", description, null);
    }
}
