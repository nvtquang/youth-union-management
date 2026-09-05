package com.hcmcyu.audit.controller;

import com.hcmcyu.audit.dto.AuditLogCreateRequest;
import com.hcmcyu.audit.dto.AuditLogResponse;
import com.hcmcyu.audit.entity.AuditAction;
import com.hcmcyu.audit.entity.AuditResult;
import com.hcmcyu.audit.entity.ResourceType;
import com.hcmcyu.audit.security.CurrentUser;
import com.hcmcyu.audit.service.AuditLogService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @PostMapping("/internal/audit-logs")
    @ResponseStatus(HttpStatus.CREATED)
    public AuditLogResponse createInternal(
            @RequestHeader(name = "X-Internal-Secret", required = false) String internalSecret,
            @Valid @RequestBody AuditLogCreateRequest request
    ) {
        return auditLogService.createInternal(request, internalSecret);
    }

    @GetMapping("/api/audit-logs")
    public Page<AuditLogResponse> findAll(
            @RequestParam(required = false, name = "action") AuditAction action,
            @RequestParam(required = false, name = "resourceType") ResourceType resourceType,
            @RequestParam(required = false, name = "organizationId") String organizationId,
            @RequestParam(required = false, name = "result") AuditResult result,
            @RequestParam(required = false, name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return auditLogService.findAll(action, resourceType, organizationId, result, date, pageable, currentUser);
    }
}
