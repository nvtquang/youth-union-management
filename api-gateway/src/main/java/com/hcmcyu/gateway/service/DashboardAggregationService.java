package com.hcmcyu.gateway.service;

import com.hcmcyu.gateway.dto.DashboardSummaryResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
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
    private final String memberServiceUrl;
    private final String eventServiceUrl;
    private final String contentServiceUrl;
    private final String notificationServiceUrl;

    public DashboardAggregationService(
            WebClient.Builder webClientBuilder,
            @Value("${MEMBER_SERVICE_URL:http://localhost:8082}") String memberServiceUrl,
            @Value("${EVENT_SERVICE_URL:http://localhost:8083}") String eventServiceUrl,
            @Value("${CONTENT_SERVICE_URL:http://localhost:8084}") String contentServiceUrl,
            @Value("${NOTIFICATION_SERVICE_URL:http://localhost:8086}") String notificationServiceUrl
    ) {
        this.webClient = webClientBuilder.build();
        this.memberServiceUrl = memberServiceUrl;
        this.eventServiceUrl = eventServiceUrl;
        this.contentServiceUrl = contentServiceUrl;
        this.notificationServiceUrl = notificationServiceUrl;
    }

    public Mono<DashboardSummaryResponse> getSummary(ServerWebExchange exchange) {
        Mono<Object> memberSummary = get(exchange, memberServiceUrl, "/api/dashboard/member-summary");
        Mono<Object> eventSummary = get(exchange, eventServiceUrl, "/api/dashboard/event-summary");
        Mono<Object> contentSummary = get(exchange, contentServiceUrl, "/api/dashboard/content-summary");
        Mono<Object> notificationSummary = get(exchange, notificationServiceUrl, "/api/notifications/unread-count");

        return Mono.zip(memberSummary, eventSummary, contentSummary, notificationSummary)
                .map(tuple -> new DashboardSummaryResponse(
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT4()
                ));
    }

    private Mono<Object> get(ServerWebExchange exchange, String serviceUrl, String path) {
        return webClient.get()
                .uri(normalize(serviceUrl) + path)
                .headers(headers -> forwardHeaders(exchange, headers))
                .retrieve()
                .bodyToMono(Object.class);
    }

    private void forwardHeaders(ServerWebExchange exchange, HttpHeaders targetHeaders) {
        HttpHeaders sourceHeaders = exchange.getRequest().getHeaders();
        FORWARDED_HEADERS.forEach(headerName -> {
            List<String> values = sourceHeaders.get(headerName);
            if (values != null && !values.isEmpty()) {
                targetHeaders.put(headerName, values);
            }
        });
    }

    private String normalize(String serviceUrl) {
        if (serviceUrl.endsWith("/")) {
            return serviceUrl.substring(0, serviceUrl.length() - 1);
        }
        return serviceUrl;
    }
}
