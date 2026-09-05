package com.hcmcyu.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GatewayErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GatewayErrorWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable exception) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(exception);
        }

        HttpStatus status = resolveStatus(exception);
        GatewayErrorResponse errorResponse = new GatewayErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                resolveCode(exception, status),
                resolveMessage(exception, status),
                exchange.getRequest().getURI().getRawPath()
        );

        byte[] body = writeBody(errorResponse);
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    private HttpStatus resolveStatus(Throwable exception) {
        if (exception instanceof ResponseStatusException responseStatusException) {
            return HttpStatus.valueOf(responseStatusException.getStatusCode().value());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveCode(Throwable exception, HttpStatus status) {
        if (status == HttpStatus.UNAUTHORIZED
                && exception instanceof ResponseStatusException responseStatusException
                && responseStatusException.getReason() != null) {
            return responseStatusException.getReason();
        }
        return switch (status) {
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case FORBIDDEN -> "FORBIDDEN";
            case NOT_FOUND -> "ROUTE_NOT_FOUND";
            case SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE";
            case GATEWAY_TIMEOUT -> "GATEWAY_TIMEOUT";
            default -> status.is5xxServerError() ? "GATEWAY_ERROR" : "REQUEST_ERROR";
        };
    }

    private String resolveMessage(Throwable exception, HttpStatus status) {
        if (status.is5xxServerError()) {
            return "Gateway could not process the request";
        }
        if (exception instanceof ResponseStatusException responseStatusException
                && responseStatusException.getReason() != null) {
            return responseStatusException.getReason();
        }
        return exception.getMessage() == null ? status.getReasonPhrase() : exception.getMessage();
    }

    private byte[] writeBody(GatewayErrorResponse errorResponse) {
        try {
            return objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException exception) {
            return "{\"code\":\"GATEWAY_ERROR\"}".getBytes(StandardCharsets.UTF_8);
        }
    }
}
