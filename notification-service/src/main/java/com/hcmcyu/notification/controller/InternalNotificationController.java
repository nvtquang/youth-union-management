package com.hcmcyu.notification.controller;

import com.hcmcyu.notification.dto.InternalNotificationCreateRequest;
import com.hcmcyu.notification.dto.InternalNotificationCreateResponse;
import com.hcmcyu.notification.service.NotificationManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/notifications")
public class InternalNotificationController {

    private final NotificationManagementService notificationManagementService;

    public InternalNotificationController(NotificationManagementService notificationManagementService) {
        this.notificationManagementService = notificationManagementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InternalNotificationCreateResponse create(
            @RequestHeader(name = "X-Internal-Secret", required = false) String internalSecret,
            @Valid @RequestBody InternalNotificationCreateRequest request
    ) {
        return notificationManagementService.createInternal(request, internalSecret);
    }
}
