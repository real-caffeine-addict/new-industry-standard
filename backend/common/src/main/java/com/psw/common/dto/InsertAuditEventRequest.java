package com.psw.common.dto;

import java.time.LocalDateTime;

public record InsertAuditEventRequest(
        Long userId,
        Long projectId,
        String action,
        String targetType,
        Long targetId,
        LocalDateTime occurredAt
) {}
