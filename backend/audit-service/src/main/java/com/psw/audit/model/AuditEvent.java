package com.psw.audit.model;

import com.psw.audit.enums.AuditableAction;
import com.psw.audit.enums.TargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private Long id;
    private Long userId;
    private Long projectId;
    private String originService;
    private AuditableAction action;
    private TargetType targetType;
    private Long targetId;
    private LocalDateTime occurredAt;
    private LocalDateTime loggedAt;
    private String jsonPayload;
}
