package com.hcmcyu.auth.controller;

import com.hcmcyu.auth.dto.InternalRoleUpdateRequest;
import com.hcmcyu.auth.dto.UserResponse;
import com.hcmcyu.auth.service.InternalUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final InternalUserService internalUserService;

    public InternalUserController(InternalUserService internalUserService) {
        this.internalUserService = internalUserService;
    }

    @PutMapping("/{userId}/role")
    public UserResponse updateRole(
            @PathVariable("userId") String userId,
            @Valid @RequestBody InternalRoleUpdateRequest request,
            @RequestHeader(name = "X-Internal-Secret", required = false) String internalSecret
    ) {
        return internalUserService.updateRole(userId, request, internalSecret);
    }
}
