package com.hcmcyu.gateway.exception;

import java.time.OffsetDateTime;

public record GatewayErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String code,
        String message,
        String path
) {
}

