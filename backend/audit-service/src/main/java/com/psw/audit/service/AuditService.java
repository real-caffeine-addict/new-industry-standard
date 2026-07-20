package com.psw.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psw.audit.model.InsertAuditEventDb;
import com.psw.audit.repository.AuditRepository;
import com.psw.common.dto.CreateAuditEventRequest;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditRepository repo;
    private final ObjectMapper mapper;

    public AuditService (AuditRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public String createAuditEvent (CreateAuditEventRequest input, String origin) {
        String jsonPayload = jsonBuilder(input);
        Long eventId = repo.createAuditEvent(
                new InsertAuditEventDb(
                        input.userId(),
                        input.projectId(),
                        origin,
                        input.action().name(),
                        input.targetType().name(),
                        input.targetId(),
                        input.occurredAt(),
                        jsonPayload
                )
        );
        return "Audit event " + eventId + " logged";
    }

    public String getAuditEventById (Long id) { return repo.readAuditEventById(id); }

    private String jsonBuilder (Object pojo) {
        try {
            return mapper.writeValueAsString(pojo);
        } catch (JsonProcessingException e){
            throw new IllegalStateException("Failed to serialize object " + pojo.getClass() , e);
        }
    }
}
