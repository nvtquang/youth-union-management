package com.hcmcyu.content.controller;

import com.hcmcyu.content.dto.ContentDashboardSummaryResponse;
import com.hcmcyu.content.security.CurrentUser;
import com.hcmcyu.content.service.ContentDashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class ContentDashboardController {

    private final ContentDashboardService contentDashboardService;

    public ContentDashboardController(ContentDashboardService contentDashboardService) {
        this.contentDashboardService = contentDashboardService;
    }

    @GetMapping("/content-summary")
    public ContentDashboardSummaryResponse getSummary(@AuthenticationPrincipal CurrentUser currentUser) {
        return contentDashboardService.getSummary(currentUser);
    }
}
