package com.hcmcyu.member.controller;

import com.hcmcyu.member.dto.InternalMemberRegistrationRequest;
import com.hcmcyu.member.dto.MemberResponse;
import com.hcmcyu.member.service.InternalMemberRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/members")
public class InternalMemberController {

    private final InternalMemberRegistrationService internalMemberRegistrationService;

    public InternalMemberController(InternalMemberRegistrationService internalMemberRegistrationService) {
        this.internalMemberRegistrationService = internalMemberRegistrationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse registerMemberProfile(
            @Valid @RequestBody InternalMemberRegistrationRequest request,
            @RequestHeader(name = "X-Internal-Secret", required = false) String internalSecret
    ) {
        return internalMemberRegistrationService.registerMemberProfile(request, internalSecret);
    }
}
