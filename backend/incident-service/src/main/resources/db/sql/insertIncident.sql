INSERT INTO incidents(
    log_id,
    request_id,
    time_created,
    alert_severity,
    source_component,
    description,
    additional_data
    ) VALUES (
        :logId,
        :requestId,
        :timestamp,
        :severity,
        :sourceComponent,
        :description,
        :debuggingDataJson
        )