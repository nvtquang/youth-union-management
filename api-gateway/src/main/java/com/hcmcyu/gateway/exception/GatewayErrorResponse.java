package com.hcmcyu.gateway.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Standard gateway error response")
public record GatewayErrorResponse(
        @Schema(description = "Time when the error response was created")
        OffsetDateTime timestamp,
        @Schema(description = "HTTP status code")
        int status,
        @Schema(description = "Stable gateway error code")
        String code,
        @Schema(description = "Human-readable error message")
        String message,
        @Schema(description = "Request path that produced the error")
        String path
) {
}
