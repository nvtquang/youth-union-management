package com.hcmcyu.gateway.controller;

import com.hcmcyu.gateway.dto.DashboardSummaryResponse;
import com.hcmcyu.gateway.service.DashboardAggregationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardAggregationService dashboardAggregationService;

    public DashboardController(DashboardAggregationService dashboardAggregationService) {
        this.dashboardAggregationService = dashboardAggregationService;
    }

    @GetMapping("/summary")
    public Mono<DashboardSummaryResponse> getSummary(ServerWebExchange exchange) {
        return dashboardAggregationService.getSummary(exchange);
    }
}
