package com.psw.common.dto;

import java.time.Instant;

public record IncidentDto(
        String logId,
        String  requestId,
        Instant timestamp,
        String severity,
        String sourceComponent,
        String description,
        String debuggingDataJson
) {}
