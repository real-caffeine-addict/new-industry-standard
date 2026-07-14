INSERT INTO audit_events (
    user_id,
    project_id,
    origin_service,
    action,
    target_type,
    target_id,
    occurred_at,
    summary
    ) VALUES (
        :userId,
        :projectId,
        :originService,
        :action,
        :targetType,
        :targetId,
        :occurredAt,
        :jsonPayload
    );
