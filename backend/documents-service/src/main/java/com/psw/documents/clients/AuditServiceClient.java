package com.psw.documents.clients;

import com.psw.common.dto.CreateAuditEventRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "audit-service", url = "${routes.audit}")
public interface AuditServiceClient {
    @PostMapping("/insert-event")
    String createAuditEvent(@RequestHeader ("X-Origin-Service") String originService,
                            @RequestBody CreateAuditEventRequest data);

    @GetMapping("/get-audit-event/{id}")
    String getAuditEventById (@PathVariable("id") Long id);
}
