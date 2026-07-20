package com.psw.common.dto;

import com.psw.common.enums.AuditableAction;
import com.psw.common.enums.TargetType;

import java.time.LocalDateTime;

public record CreateAuditEventRequest(
        Long userId,
        Long projectId,
        AuditableAction action,
        TargetType targetType,
        Long targetId,
        LocalDateTime occurredAt
) {}
