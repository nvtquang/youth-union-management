package com.hcmcyu.member.service;

import com.hcmcyu.member.dto.AuthRoleUpdateRequest;
import com.hcmcyu.member.entity.MemberRole;
import com.hcmcyu.member.exception.MemberServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class RestAuthRoleClient implements AuthRoleClient {

    private final RestClient restClient;
    private final String internalSecret;

    public RestAuthRoleClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.auth.url:http://localhost:8081}") String authServiceUrl,
            @Value("${services.auth.internal-secret:dev-internal-secret}") String internalSecret
    ) {
        this.restClient = restClientBuilder.baseUrl(authServiceUrl).build();
        this.internalSecret = internalSecret;
    }

    @Override
    public void updateUserRole(String userId, MemberRole role, String actorUserId, String memberId) {
        try {
            restClient.put()
                    .uri("/internal/users/{userId}/role", userId)
                    .header("X-Internal-Secret", internalSecret)
                    .body(new AuthRoleUpdateRequest(role, actorUserId, memberId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new MemberServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "AUTH_ROLE_SYNC_FAILED",
                    "Failed to synchronize role with auth-service"
            );
        }
    }
}
