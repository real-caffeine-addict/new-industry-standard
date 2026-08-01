package com.psw.gateway.utility;

import com.psw.common.dto.AlertRecord;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class IncidentHandler {
    private static final Set<String> REQUIRED_SUPPORT_DESCRIPTIONS = Set.of(
            "Unknown exception thrown",
            "Downstream service is unavailable",
            "Authentication error",
            "Request had more than one MAJOR or CRITICAL alert"
    ); //TODO: to be completed.
    protected AlertRecord createIncidentIfRequired(AlertRecord alert){
        if (!(REQUIRED_SUPPORT_DESCRIPTIONS.contains(alert.description()) || alert.debuggingData() != null))
            return null;
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
    }

    private Long logIncident (AlertRecord alert){
        //Insert to DB or cache
        return null; //TODO: return id value
    }
}
