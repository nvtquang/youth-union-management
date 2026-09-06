package com.hcmcyu.gateway.controller;

import com.hcmcyu.gateway.dto.DashboardSummaryResponse;
import com.hcmcyu.gateway.service.DashboardAggregationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Admin", description = "Gateway-level aggregation APIs.")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token"),
        @ApiResponse(responseCode = "403", description = "Role or organization scope denied"),
        @ApiResponse(responseCode = "502", description = "A downstream service failed")
})
public class DashboardController {

    private final DashboardAggregationService dashboardAggregationService;

    public DashboardController(DashboardAggregationService dashboardAggregationService) {
        this.dashboardAggregationService = dashboardAggregationService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary", description = "Aggregates member, event, content, and notification summaries without returning banking/QR data.")
    public Mono<DashboardSummaryResponse> getSummary(ServerWebExchange exchange) {
        return dashboardAggregationService.getSummary(exchange);
    }
}
