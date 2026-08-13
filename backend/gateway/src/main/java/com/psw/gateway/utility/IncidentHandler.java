package com.psw.gateway.utility;

import com.psw.common.dto.IncidentDto;
import com.psw.common.enums.AlertSeverity;
import com.psw.gateway.model.AlertRecord;
import com.psw.gateway.client.IncidentClient;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Component
public class IncidentHandler {
    private final ExceptionParserUtil parser;
    private final IncidentClient client;
    private static final Set<String> REQUIRED_SUPPORT_DESCRIPTIONS = Set.of(
            "Unknown exception thrown",
            "Downstream service is unavailable",
            "Authentication error",
            "Request had more than one MAJOR or CRITICAL alert"
    ); //TODO: to be completed.

    public IncidentHandler (ExceptionParserUtil parser,
                            IncidentClient client) {
        this.parser = parser;
        this.client = client;
    }
    protected AlertRecord createIncidentIfRequired(AlertRecord alert){
        if (!(REQUIRED_SUPPORT_DESCRIPTIONS.contains(alert.description()) || alert.debuggingData() != null))
            return null;
        try {
            return new AlertRecord(
                    alert.logId(),
                    alert.requestId(),
                    logIncident(alert),
                    alert.timestamp(),
                    alert.severity(),
                    alert.sourceComponent(),
                    alert.description(),
                    alert.debuggingData()
            );
        }catch (Exception e) {
            return new AlertRecord(
                    UUID.randomUUID(),
                    alert.requestId(),
                    null,
                    Instant.now(),
                    AlertSeverity.CRITICAL,
                    "Incident service",
                    "Failed to log incident for" + alert.description(),
                    parser.exceptionParser(e)
            );
        }
    }


    private Long logIncident (AlertRecord alert) {
        IncidentDto payload = new IncidentDto(
                alert.logId().toString(),
                alert.requestId().toString(),
                alert.timestamp(),
                alert.severity().name(),
                alert.sourceComponent(),
                alert.description(),
                alert.debuggingData().toString()
        );
        return client.logIncident(payload);
    }
}
