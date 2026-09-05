package com.hcmcyu.gateway.security;

public record GatewayPrincipal(
        String userId,
        String memberId,
        String username,
        String email,
        String role,
        String organizationId,
        String tdpId
) {
}
