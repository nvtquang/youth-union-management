package com.hcmcyu.gateway.dto;

public record DashboardSummaryResponse(
        Object member,
        Object event,
        Object content,
        Object notification
) {
}
