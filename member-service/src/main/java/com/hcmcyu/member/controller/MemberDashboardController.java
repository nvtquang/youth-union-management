package com.hcmcyu.member.controller;

import com.hcmcyu.member.dto.MemberDashboardSummaryResponse;
import com.hcmcyu.member.security.CurrentUser;
import com.hcmcyu.member.service.MemberDashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class MemberDashboardController {

    private final MemberDashboardService memberDashboardService;

    public MemberDashboardController(MemberDashboardService memberDashboardService) {
        this.memberDashboardService = memberDashboardService;
    }

    @GetMapping("/member-summary")
    public MemberDashboardSummaryResponse getSummary(@AuthenticationPrincipal CurrentUser currentUser) {
        return memberDashboardService.getSummary(currentUser);
    }
}
