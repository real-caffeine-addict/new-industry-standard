package com.psw.gateway.utility;

import com.psw.common.enums.AlertSeverity;
import com.psw.gateway.model.RequestContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContextInitUtilTests {
    private final ContextInitUtil util = new ContextInitUtil();

    @Test
    void classifiesHeadersAndRedactsSensitiveValuesCaseInsensitively() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/items");
        request.addHeader("User-Agent", "agent");
        request.addHeader("X-Custom", "one");
        request.addHeader("X-Custom", "two");
        request.addHeader("aUtHoRiZaTiOn", "Bearer secret");
        request.addHeader("Cookie", "session=secret");
        RequestContext context = RequestContext.builder().requestId(UUID.randomUUID()).build();

        util.headerResolver(request, context);

        assertThat(context.getImportantHeaders().get("User-Agent")).containsExactly("agent");
        assertThat(context.getImportantHeaders().get("aUtHoRiZaTiOn")).containsExactly("[redacted]");
        assertThat(context.getImportantHeaders().get("Cookie")).containsExactly("[redacted]");
        assertThat(context.getOtherHeaders().get("X-Custom")).containsExactly("one", "two");
    }

    @Test
    void capturesPeerAndForwardedAddresses() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.10");
        request.addHeader("X-Forwarded-For", "198.51.100.1, 198.51.100.2");
        RequestContext context = RequestContext.builder().requestId(UUID.randomUUID()).build();

        util.ipResolver(request, context);

        assertThat(context.getPeerIp()).isEqualTo("192.0.2.10");
        assertThat(context.getForwardedIp()).isEqualTo("198.51.100.1, 198.51.100.2");
        assertThat(context.getAlerts()).isEmpty();
    }

    @Test
    void missingRemoteAddressAddsOneCorrelatedMinorAlert() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(null);
        UUID requestId = UUID.randomUUID();
        RequestContext context = RequestContext.builder().requestId(requestId).build();

        util.ipResolver(request, context);

        assertThat(context.getAlerts()).singleElement().satisfies(alert -> {
            assertThat(alert.requestId()).isEqualTo(requestId);
            assertThat(alert.severity()).isEqualTo(AlertSeverity.MINOR);
        });
    }
}
