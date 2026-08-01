package com.psw.gateway.utility;

import com.psw.common.dto.AlertRecord;
import com.psw.common.dto.RequestContext;
import com.psw.common.enums.AlertSeverity;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class GwLogger {
    private final Logger requestLogger = LoggerFactory.getLogger("logRequest");
    private final Logger alertLogger = LoggerFactory.getLogger("alertLogger");
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC);
    private static final Set<AlertSeverity> INSPECTED_SEVERITIES = Set.of (AlertSeverity.CRITICAL, AlertSeverity.MAJOR);
    private final IncidentHandler incidentHandler;
    private final ExceptionParserUtil exceptionParser;
    private record AlertHandlerResult(int alerts, Long incId){}

    public GwLogger (IncidentHandler incidentHandler,
                     ExceptionParserUtil exceptionParser){
        this.incidentHandler = incidentHandler;
        this.exceptionParser = exceptionParser;
    }

    public Long logRequest(RequestContext ctx) {
        AlertHandlerResult alertHandlerResult = alertHandler(ctx.getAlerts(), ctx.getRequestId());
        Map<String, Object> logPayload = new LinkedHashMap<>();
        logPayload.put( "requestId", ctx.getRequestId());
        logPayload.put( "peerIp", ctx.getPeerIp());
        logPayload.put( "forwardedIp", ctx.getForwardedIp());
        logPayload.put("importantHeaders", ctx.getImportantHeaders());
        logPayload.put("otherHeaders", ctx.getOtherHeaders());
        logPayload.put( "path", ctx.getPath());
        logPayload.put( "method", ctx.getMethod());
        logPayload.put( "status", ctx.getStatus());
        logPayload.put( "responseBody", "[REDACTED]");
        logPayload.put( "startedAt", ISO_FORMAT.format(ctx.getStartedAt()));
        logPayload.put( "elapsed", ctx.getElapsed());
        logPayload.put("alerts", alertHandlerResult.alerts());
        requestLogger.info("request-log", StructuredArguments.entries(logPayload));
        return alertHandlerResult.incId();
    }

    private AlertHandlerResult alertHandler(List<AlertRecord> alerts, UUID requestId) {
        List<AlertRecord> severeAlerts = new ArrayList<>();
        Long incId = null;
        for (AlertRecord alert : alerts) {
            if (INSPECTED_SEVERITIES.contains(alert.severity())) {
                severeAlerts.addLast(alert);
            } else {
                logAlert(alert);
            }
        }

        if (severeAlerts.size() > 1){
            AlertRecord requestError = requestErrorHandler(requestId);
            incId = requestError.incidentId();
            logAlert(requestError);
        }

        for (AlertRecord alert : severeAlerts){
            AlertRecord  incAlert = incidentHandler.createIncidentIfRequired(alert);
            if (incAlert != null){
                if (incId == null)
                    incId = incAlert.incidentId();
                logAlert(incAlert);
            }else {
                logAlert(alert);
            }
        }
        return new AlertHandlerResult(alerts.size(), incId);
    }

    private void logAlert (AlertRecord alert){
        Map<String, Object> logPayload = new LinkedHashMap<>();
        logPayload.put("logId", alert.logId());
        logPayload.put("requestId", alert.requestId());
        logPayload.put("incidentId", alert.incidentId());
        logPayload.put("timestamp", alert.timestamp());
        logPayload.put("severity", alert.severity());
        logPayload.put("sourceComponent", alert.sourceComponent());
        logPayload.put("description", alert.description());
        if (alert.debuggingData() != null)
            logPayload.put("debuggingData", exceptionParser.exceptionParser(alert.debuggingData()));
        switch (alert.severity()){
            case WARN -> alertLogger.warn("alert-log" ,StructuredArguments.entries(logPayload));
            case INFO -> alertLogger.info("alert-log" ,StructuredArguments.entries(logPayload));
            case MINOR,MAJOR,CRITICAL -> alertLogger.error("alert-log" ,StructuredArguments.entries(logPayload));
        }
    }

    private AlertRecord requestErrorHandler (UUID requestId){
        try {
            return incidentHandler.createIncidentIfRequired(new AlertRecord(
                    UUID.randomUUID(),
                    requestId,
                    null,
                    Instant.now(),
                    AlertSeverity.CRITICAL,
                    "GwLogger",
                    "Request had more than one MAJOR or CRITICAL alert",
                    null
            ));
        }catch (Exception e){
            return new AlertRecord(
                    UUID.randomUUID(),
                    requestId,
                    null,
                    Instant.now(),
                    AlertSeverity.CRITICAL,
                    "GwLogger",
                    "Request had more than one MAJOR or CRITICAL alert",
                    null
            );
        }
    }
}
