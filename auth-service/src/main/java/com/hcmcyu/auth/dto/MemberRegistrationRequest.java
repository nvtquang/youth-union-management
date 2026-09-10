package com.hcmcyu.auth.dto;

public record MemberRegistrationRequest(
        String userId,
        String fullName,
        String email,
        String phone,
        String organizationId
) {
}
