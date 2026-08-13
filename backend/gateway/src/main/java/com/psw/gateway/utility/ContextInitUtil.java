package com.psw.gateway.utility;

import com.psw.gateway.model.AlertRecord;
import com.psw.gateway.model.RequestContext;
import com.psw.common.enums.AlertSeverity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class ContextInitUtil {
    private final static Set<String> IMPORTANT_HEADERS = Set.of(
            HttpHeaders.REFERER.toLowerCase(),
            HttpHeaders.USER_AGENT.toLowerCase(),
            HttpHeaders.ORIGIN.toLowerCase(),
            HttpHeaders.HOST.toLowerCase()
    );

    private final static Set<String> SENSITIVE_HEADERS = Set.of(
            HttpHeaders.AUTHORIZATION.toLowerCase(),
            HttpHeaders.COOKIE.toLowerCase(),
            HttpHeaders.SET_COOKIE.toLowerCase(),
            "proxy-authorization"
    );

    public void ipResolver (HttpServletRequest request, RequestContext ctx) {
            ctx.setForwardedIp(request.getHeader("X-Forwarded-For")); //TODO: Update resolver when production infra is selected.
            ctx.setPeerIp(request.getRemoteAddr());
            if (ctx.getPeerIp() == null) {
                ctx.getAlerts().add(new AlertRecord(
                        UUID.randomUUID(),
                        ctx.getRequestId(),
                        null,
                        Instant.now(),
                        AlertSeverity.MINOR,
                        "ContextInitFilter",
                        "Remote peer address is unavailable",
                        null
                ));
            }
    }

    public void headerResolver (HttpServletRequest request, RequestContext ctx) {
        Map<String, List<String>> important = new LinkedHashMap<>();
        Map<String, List<String>> other = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()){
            String name = names.nextElement();
            List<String> values = Collections.list(request.getHeaders(name));
            String normalized = name.toLowerCase(Locale.ROOT);
            if (SENSITIVE_HEADERS.contains(normalized)) {
                important.put(name, List.of("[redacted]"));
            } else if (IMPORTANT_HEADERS.contains(normalized)) {
                important.put(name, List.copyOf(values));
            } else {
                other.put(name, List.copyOf(values));
            }
        }
        ctx.setImportantHeaders(important);
        ctx.setOtherHeaders(other);
    }
}
