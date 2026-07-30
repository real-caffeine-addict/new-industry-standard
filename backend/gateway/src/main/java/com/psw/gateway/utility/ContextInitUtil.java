package com.psw.gateway.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psw.common.dto.AlertRecord;
import com.psw.common.dto.RequestContext;
import com.psw.common.enums.AlertSeverity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.UUID;

@Component
public class ContextInitUtil {
    private final ObjectMapper mapper;

    public ContextInitUtil(ObjectMapper mapper) { this.mapper = mapper; }

    public void ipResolver (ServerHttpRequest request, RequestContext ctx) {
            ctx.setForwardedIp(request.getHeaders().getFirst("X-Forwarded-For")); //TODO: Update resolver when production infra is selected.
            InetSocketAddress inetSocketAddress = request.getRemoteAddress();
            ctx.setPeerIp(inetSocketAddress != null ? inetSocketAddress.getHostString() : null);
            if (inetSocketAddress == null) {
                ctx.getAlerts().add(new AlertRecord(
                        UUID.randomUUID(),
                        ctx.getRequestId(),
                        Instant.now(),
                        AlertSeverity.MINOR,
                        "ContextInitFilter",
                        "Remote peer address is unavailable"
                ));
            }
    }

    public void headerResolver (HttpHeaders httpHeaders, RequestContext ctx) {
        try {
            //TODO: add headers handlers
            String important = mapper.writeValueAsString(httpHeaders);
            String other = "other";
            ctx.setImportantHeaders(important);
            ctx.setOtherHeaders(other);
        }catch (JsonProcessingException e){
            ctx.setOtherHeaders(httpHeaders.toString());
            ctx.getAlerts().add(new AlertRecord(
                    UUID.randomUUID(),
                    ctx.getRequestId(),
                    Instant.now(),
                    AlertSeverity.WARN,
                    "ContextInitFilter",
                    "Failed to serialize request headers"
            ));
        }
    }
}
