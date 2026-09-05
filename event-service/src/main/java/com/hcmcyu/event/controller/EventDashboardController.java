package com.hcmcyu.event.controller;

import com.hcmcyu.event.dto.EventDashboardSummaryResponse;
import com.hcmcyu.event.security.CurrentUser;
import com.hcmcyu.event.service.EventDashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class EventDashboardController {

    private final EventDashboardService eventDashboardService;

    public EventDashboardController(EventDashboardService eventDashboardService) {
        this.eventDashboardService = eventDashboardService;
    }

    @GetMapping("/event-summary")
    public EventDashboardSummaryResponse getSummary(@AuthenticationPrincipal CurrentUser currentUser) {
        return eventDashboardService.getSummary(currentUser);
    }
}
