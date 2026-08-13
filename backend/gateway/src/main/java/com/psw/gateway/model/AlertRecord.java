package com.psw.gateway.model;

import com.psw.common.enums.AlertSeverity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AlertRecord(
        UUID logId,
        UUID requestId,
        Long incidentId,
        Instant timestamp,
        AlertSeverity severity,
        String sourceComponent,
        String description,
        Map<String, Object> debuggingData
) {}
