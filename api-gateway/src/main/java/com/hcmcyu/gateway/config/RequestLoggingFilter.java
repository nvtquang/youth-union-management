package com.hcmcyu.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startedAt = System.currentTimeMillis();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getRawPath();

        log.info("Gateway request started: {} {}", method, path);

        return chain.filter(exchange)
                .doOnSuccess(unused -> logCompleted(exchange, method, path, startedAt))
                .doOnError(error -> log.warn(
                        "Gateway request failed: {} {} after {}ms",
                        method,
                        path,
                        System.currentTimeMillis() - startedAt,
                        error
                ));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private void logCompleted(ServerWebExchange exchange, String method, String path, long startedAt) {
        HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
        int status = statusCode == null ? 200 : statusCode.value();
        log.info(
                "Gateway request completed: {} {} -> {} in {}ms",
                method,
                path,
                status,
                System.currentTimeMillis() - startedAt
        );
    }
}

