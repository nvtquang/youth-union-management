package com.hcmcyu.chat.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MemberDirectoryClient {

    private final RestClient restClient;
    private final String internalSecret;

    public MemberDirectoryClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.member.url:http://localhost:8082}") String memberServiceUrl,
            @Value("${services.member.internal-secret:dev-internal-secret}") String internalSecret
    ) {
        this.restClient = restClientBuilder.baseUrl(memberServiceUrl).build();
        this.internalSecret = internalSecret;
    }

    public Map<String, String> findDisplayNames(Collection<String> memberIds) {
        List<String> ids = memberIds.stream()
                .filter(memberId -> memberId != null && !memberId.isBlank())
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }

        try {
            List<MemberDisplayNameResponse> responses = restClient.post()
                    .uri("/internal/members/display-names")
                    .header("X-Internal-Secret", internalSecret)
                    .body(new MemberDisplayNameRequest(ids))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (responses == null) {
                return Map.of();
            }
            return responses.stream()
                    .collect(Collectors.toMap(
                            MemberDisplayNameResponse::memberId,
                            MemberDisplayNameResponse::fullName,
                            (left, right) -> left
                    ));
        } catch (RuntimeException exception) {
            return Map.of();
        }
    }

    private record MemberDisplayNameRequest(List<String> memberIds) {
    }

    private record MemberDisplayNameResponse(String memberId, String fullName) {
    }
}
