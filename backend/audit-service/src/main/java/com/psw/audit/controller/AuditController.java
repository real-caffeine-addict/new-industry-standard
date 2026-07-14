package com.psw.audit.controller;

import com.psw.audit.service.AuditService;
import com.psw.common.dto.InsertAuditEventRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class AuditController {
    private final AuditService service;

    public AuditController (AuditService service) { this.service = service; }

    @PostMapping("/insert-event")
    public String insertAuditEvent (@RequestBody InsertAuditEventRequest input){
        String origin = "documents-service"; //TODO: Extract origin service from signature
        return service.createAuditEvent(input, origin);
    }

    @GetMapping("/get-audit-event/{id}")
    public String getAuditEventById (@PathVariable ("id") Long id){
        return service.getAuditEventById(id);
    }
}
