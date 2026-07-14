package com.psw.audit.model;

import java.time.LocalDateTime;

public record InsertAuditEventDb(
    Long userId,
    Long projectId,
    String originService,
    String action,
    String targetType,
    Long targetId,
    LocalDateTime occurredAt,
    String jsonPayload
) {
}
