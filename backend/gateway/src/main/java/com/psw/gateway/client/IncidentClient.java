package com.psw.gateway.client;

import com.psw.common.dto.IncidentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "incidents", url = "${routes.incident}")
public interface IncidentClient {
    @PostMapping("/incidents")
    Long logIncident(@RequestBody IncidentDto payload);
}
