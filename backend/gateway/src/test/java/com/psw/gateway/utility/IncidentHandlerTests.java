package com.psw.gateway.utility;

import com.psw.common.dto.IncidentDto;
import com.psw.common.enums.AlertSeverity;
import com.psw.gateway.client.IncidentClient;
import com.psw.gateway.model.AlertRecord;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncidentHandlerTests {
    private final ExceptionParserUtil parser = new ExceptionParserUtil();
    private final IncidentClient client = mock(IncidentClient.class);
    private final IncidentHandler handler = new IncidentHandler(parser, client);

    @Test
    void ignoresAlertThatDoesNotQualify() {
        AlertRecord alert = alert("Routine warning", null);

        assertThat(handler.createIncidentIfRequired(alert)).isNull();
        verify(client, never()).logIncident(any());
    }

    @Test
    void persistsQualifyingAlertAndAttachesReturnedId() {
        AlertRecord alert = alert("Diagnostic failure", Map.of("operation", "load"));
        when(client.logIncident(any())).thenReturn(42L);

        AlertRecord result = handler.createIncidentIfRequired(alert);

        assertThat(result.incidentId()).isEqualTo(42L);
        assertThat(result.logId()).isEqualTo(alert.logId());
        assertThat(result.requestId()).isEqualTo(alert.requestId());
        verify(client).logIncident(new IncidentDto(
                alert.logId().toString(), alert.requestId().toString(), alert.timestamp(),
                alert.severity().name(), alert.sourceComponent(), alert.description(),
                alert.debuggingData().toString()));
    }

    @Test
    void emptyIncidentServiceResponseDoesNotCreateFalseIncidentReference() {
        AlertRecord alert = alert("Diagnostic failure", Map.of("operation", "load"));
        when(client.logIncident(any())).thenReturn(null);

        AlertRecord result = handler.createIncidentIfRequired(alert);

        assertThat(result).isNotNull();
        assertThat(result.incidentId()).isNull();
        verify(client).logIncident(any());
    }

    @Test
    void clientFailureProducesCriticalFallbackWithoutRecursion() {
        AlertRecord alert = alert("Diagnostic failure", Map.of("operation", "load"));
        when(client.logIncident(any())).thenThrow(new RuntimeException("offline"));

        AlertRecord result = handler.createIncidentIfRequired(alert);

        assertThat(result.severity()).isEqualTo(AlertSeverity.CRITICAL);
        assertThat(result.requestId()).isEqualTo(alert.requestId());
        assertThat(result.incidentId()).isNull();
        assertThat(result.description()).contains("Failed to log incident");
        assertThat(result.debuggingData()).isNotNull();
        verify(client).logIncident(any());
    }

    private AlertRecord alert(String description, Map<String, Object> debuggingData) {
        return new AlertRecord(UUID.randomUUID(), UUID.randomUUID(), null, Instant.now(),
                AlertSeverity.MAJOR, "test", description, debuggingData);
    }
}
