package com.psw.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestContext {
    private UUID requestId;
    private String peerIp;
    private String forwardedIp;
    private String importantHeaders;
    private String otherHeaders;
    private String path;
    private String method;
    private int status;
    private String statusMessage;
    private String responseBody;
    private Instant startedAt;
    private String elapsed;
    @Builder.Default
    private List<AlertRecord> alerts = new ArrayList<>();
}
