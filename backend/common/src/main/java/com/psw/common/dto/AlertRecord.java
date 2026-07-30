package com.psw.common.dto;

import com.psw.common.enums.AlertSeverity;

import java.time.Instant;
import java.util.UUID;

public record AlertRecord(
        UUID logId,
        UUID requestId,
        Instant timestamp,
        AlertSeverity severity,
        String sourceComponent,
        String description
) {}
