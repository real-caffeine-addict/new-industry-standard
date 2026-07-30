package com.psw.gateway.filter;

import com.psw.common.dto.RequestContext;
import com.psw.gateway.utility.ContextInitUtil;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ContextInitFilter implements WebFilter {
    private final ContextInitUtil util;

    public ContextInitFilter (ContextInitUtil util) { this.util = util; }
    @Override
    @NullMarked
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        RequestContext ctx = RequestContext.builder()
                .requestId(UUID.randomUUID())
                .path(request.getURI().getPath())
                .method(request.getMethod().name())
                .startedAt(Instant.now())
                .build();

        util.ipResolver(request, ctx);
        util.headerResolver(request.getHeaders(), ctx);

        return null;
    }
}
