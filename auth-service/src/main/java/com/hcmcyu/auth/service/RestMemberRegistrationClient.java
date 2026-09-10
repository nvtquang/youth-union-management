package com.hcmcyu.auth.service;

import com.hcmcyu.auth.dto.MemberRegistrationRequest;
import com.hcmcyu.auth.dto.MemberRegistrationResponse;
import com.hcmcyu.auth.dto.RegisterRequest;
import com.hcmcyu.auth.entity.UserAccount;
import com.hcmcyu.auth.exception.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RestMemberRegistrationClient implements MemberRegistrationClient {

    private final RestClient restClient;
    private final String internalSecret;

    public RestMemberRegistrationClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.member.url:http://localhost:8082}") String memberServiceUrl,
            @Value("${auth.internal.secret:dev-internal-secret}") String internalSecret
    ) {
        this.restClient = restClientBuilder.baseUrl(memberServiceUrl).build();
        this.internalSecret = internalSecret;
    }

    @Override
    public MemberRegistrationResponse createMemberProfile(UserAccount user, RegisterRequest request) {
        try {
            return restClient.post()
                    .uri("/internal/members/register")
                    .header("X-Internal-Secret", internalSecret)
                    .body(new MemberRegistrationRequest(
                            user.getId(),
                            request.fullName().trim(),
                            user.getEmail(),
                            blankToNull(request.phone()),
                            request.organizationId().trim()
                    ))
                    .retrieve()
                    .body(MemberRegistrationResponse.class);
        } catch (RestClientException exception) {
            throw new AuthException(
                    HttpStatus.BAD_GATEWAY,
                    "MEMBER_PROFILE_REGISTRATION_FAILED",
                    "Could not create member profile"
            );
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
