package com.hcmcyu.content.service;

import com.hcmcyu.content.entity.Post;
import com.hcmcyu.content.security.CurrentUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class RestAuditClient implements AuditClient {

    private final RestClient restClient;
    private final String auditServiceUrl;
    private final String internalSecret;

    public RestAuditClient(
            RestClient.Builder restClientBuilder,
            @Value("${audit.service.url:http://localhost:8087}") String auditServiceUrl,
            @Value("${audit.internal.secret:dev-audit-internal-secret}") String internalSecret
    ) {
        this.restClient = restClientBuilder.build();
        this.auditServiceUrl = auditServiceUrl;
        this.internalSecret = internalSecret;
    }

    @Override
    public void record(AuditAction action, Post post, CurrentUser currentUser) {
        AuditLogRequest request = new AuditLogRequest(
                currentUser.userId(),
                currentUser.role().name(),
                action,
                AuditResourceType.POST,
                post.getId(),
                post.getOrganizationId(),
                AuditResult.SUCCESS
        );
        try {
            restClient.post()
                    .uri(normalize(auditServiceUrl) + "/internal/audit-logs")
                    .header("X-Internal-Secret", internalSecret)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ignored) {
            // Keep local development usable when audit-service is not running.
        }
    }

    private String normalize(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
