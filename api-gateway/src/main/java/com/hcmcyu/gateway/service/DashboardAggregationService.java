package com.hcmcyu.gateway.service;

import com.hcmcyu.gateway.dto.DashboardSummaryResponse;
import com.hcmcyu.gateway.security.GatewayJwtService;
import com.hcmcyu.gateway.security.GatewayPrincipal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
public class DashboardAggregationService {

    private static final List<String> FORWARDED_HEADERS = List.of(
            HttpHeaders.AUTHORIZATION,
            "X-User-Id",
            "X-Member-Id",
            "X-Username",
            "X-User-Role",
            "X-Organization-Id",
            "X-Tdp-Id"
    );

    private final WebClient webClient;
    private final GatewayJwtService gatewayJwtService;
    private final String memberServiceUrl;
    private final String eventServiceUrl;
    private final String contentServiceUrl;
    private final String notificationServiceUrl;

    public DashboardAggregationService(
            WebClient.Builder webClientBuilder,
            GatewayJwtService gatewayJwtService,
            @Value("${MEMBER_SERVICE_URL:http://localhost:8082}") String memberServiceUrl,
            @Value("${EVENT_SERVICE_URL:http://localhost:8083}") String eventServiceUrl,
            @Value("${CONTENT_SERVICE_URL:http://localhost:8084}") String contentServiceUrl,
            @Value("${NOTIFICATION_SERVICE_URL:http://localhost:8086}") String notificationServiceUrl
    ) {
        this.webClient = webClientBuilder.build();
        this.gatewayJwtService = gatewayJwtService;
        this.memberServiceUrl = memberServiceUrl;
        this.eventServiceUrl = eventServiceUrl;
        this.contentServiceUrl = contentServiceUrl;
        this.notificationServiceUrl = notificationServiceUrl;
    }

    public Mono<DashboardSummaryResponse> getSummary(ServerWebExchange exchange) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"));
        }

        GatewayPrincipal principal = gatewayJwtService.parseAccessToken(authorization.substring(7));
        Mono<Object> memberSummary = get(exchange, principal, memberServiceUrl, "/api/dashboard/member-summary");
        Mono<Object> eventSummary = get(exchange, principal, eventServiceUrl, "/api/dashboard/event-summary");
        Mono<Object> contentSummary = get(exchange, principal, contentServiceUrl, "/api/dashboard/content-summary");
        Mono<Object> notificationSummary = get(exchange, principal, notificationServiceUrl, "/api/notifications/unread-count");

        return Mono.zip(memberSummary, eventSummary, contentSummary, notificationSummary)
                .map(tuple -> new DashboardSummaryResponse(
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT4()
                ));
    }

    private Mono<Object> get(ServerWebExchange exchange, GatewayPrincipal principal, String serviceUrl, String path) {
        return webClient.get()
                .uri(normalize(serviceUrl) + path)
                .headers(headers -> forwardHeaders(exchange, principal, headers))
                .retrieve()
                .bodyToMono(Object.class);
    }

    private void forwardHeaders(ServerWebExchange exchange, GatewayPrincipal principal, HttpHeaders targetHeaders) {
        HttpHeaders sourceHeaders = exchange.getRequest().getHeaders();
        FORWARDED_HEADERS.forEach(headerName -> {
            List<String> values = sourceHeaders.get(headerName);
            if (values != null && !values.isEmpty()) {
                targetHeaders.put(headerName, values);
            }
        });
        putIfPresent(targetHeaders, "X-User-Id", principal.userId());
        putIfPresent(targetHeaders, "X-Member-Id", principal.memberId());
        putIfPresent(targetHeaders, "X-Username", principal.username());
        putIfPresent(targetHeaders, "X-User-Email", principal.email());
        putIfPresent(targetHeaders, "X-User-Role", principal.role());
        putIfPresent(targetHeaders, "X-Organization-Id", principal.organizationId());
        putIfPresent(targetHeaders, "X-Tdp-Id", principal.tdpId());
    }

    private void putIfPresent(HttpHeaders headers, String name, String value) {
        if (value != null && !value.isBlank()) {
            headers.set(name, value);
        }
    }

    private String normalize(String serviceUrl) {
        if (serviceUrl.endsWith("/")) {
            return serviceUrl.substring(0, serviceUrl.length() - 1);
        }
        return serviceUrl;
    }
}
