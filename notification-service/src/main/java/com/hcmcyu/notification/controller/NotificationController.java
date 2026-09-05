package com.hcmcyu.notification.controller;

import com.hcmcyu.notification.dto.NotificationResponse;
import com.hcmcyu.notification.dto.UnreadCountResponse;
import com.hcmcyu.notification.security.CurrentUser;
import com.hcmcyu.notification.service.NotificationManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationManagementService notificationManagementService;

    public NotificationController(NotificationManagementService notificationManagementService) {
        this.notificationManagementService = notificationManagementService;
    }

    @GetMapping
    public Page<NotificationResponse> findMine(
            @AuthenticationPrincipal CurrentUser currentUser,
            Pageable pageable
    ) {
        return notificationManagementService.findMine(currentUser, pageable);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse countUnread(@AuthenticationPrincipal CurrentUser currentUser) {
        return notificationManagementService.countUnread(currentUser);
    }

    @PutMapping("/{id}/read")
    public NotificationResponse markRead(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return notificationManagementService.markRead(id, currentUser);
    }

    @PutMapping("/read-all")
    public UnreadCountResponse markAllRead(@AuthenticationPrincipal CurrentUser currentUser) {
        return notificationManagementService.markAllRead(currentUser);
    }
}
