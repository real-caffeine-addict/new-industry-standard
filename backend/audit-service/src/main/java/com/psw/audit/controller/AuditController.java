package com.psw.audit.controller;

import com.psw.audit.service.AuditService;
import com.psw.common.dto.CreateAuditEventRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class AuditController {
    private final AuditService service;

    public AuditController (AuditService service) { this.service = service; }

    @PostMapping("/insert-event")
    public String insertAuditEvent (@RequestHeader ("X-Origin-Service") String originService,
                                    @RequestBody CreateAuditEventRequest input){
        return service.createAuditEvent(input, originService); //TODO: Extract origin service from signature
    }

    @GetMapping("/get-audit-event/{id}")
    public String getAuditEventById (@PathVariable ("id") Long id){
        return service.getAuditEventById(id);
    }
}
