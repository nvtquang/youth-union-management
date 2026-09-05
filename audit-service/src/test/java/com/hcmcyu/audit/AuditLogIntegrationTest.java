package com.hcmcyu.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmcyu.audit.entity.AuditAction;
import com.hcmcyu.audit.entity.AuditLog;
import com.hcmcyu.audit.entity.AuditResult;
import com.hcmcyu.audit.entity.ResourceType;
import com.hcmcyu.audit.repository.AuditLogRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:hcmcyu_audit_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=validate",
        "audit.internal.secret=test-audit-secret"
})
class AuditLogIntegrationTest {

    private static final String INTERNAL_SECRET = "test-audit-secret";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
    }

    @Test
    void internalApiCreatesAuditLogWithoutSensitivePayload() throws Exception {
        mockMvc.perform(post("/internal/audit-logs")
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "actorUserId", "ward-secretary-user",
                                "actorRole", "WARD_SECRETARY",
                                "action", "CHANGE_ROLE",
                                "resourceType", "MEMBER",
                                "resourceId", "member-1",
                                "organizationId", "tdp-1",
                                "result", "SUCCESS"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.action").value("CHANGE_ROLE"))
                .andExpect(jsonPath("$.resourceType").value("MEMBER"));

        assertThat(auditLogRepository.findAll()).hasSize(1);
    }

    @Test
    void invalidInternalSecretIsRejected() throws Exception {
        mockMvc.perform(post("/internal/audit-logs")
                        .header("X-Internal-Secret", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "actorUserId", "ward-secretary-user",
                                "actorRole", "WARD_SECRETARY",
                                "action", "CREATE_MEMBER",
                                "resourceType", "MEMBER",
                                "resourceId", "member-1",
                                "result", "SUCCESS"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_INTERNAL_SECRET"));
    }

    @Test
    void wardSecretaryCanViewAllAuditLogsWithFilters() throws Exception {
        saveAuditLog(AuditAction.CHANGE_ROLE, ResourceType.MEMBER, "member-1", "tdp-1");
        saveAuditLog(AuditAction.CREATE_POST, ResourceType.POST, "post-1", "tdp-2");

        mockMvc.perform(withWardSecretary(get("/api/audit-logs?action=CHANGE_ROLE&page=0&size=10")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].action").value("CHANGE_ROLE"));
    }

    @Test
    void wardDeputySecretaryCanViewBusinessAuditButNotRoleChanges() throws Exception {
        saveAuditLog(AuditAction.CHANGE_ROLE, ResourceType.MEMBER, "member-1", "tdp-1");
        saveAuditLog(AuditAction.UPDATE_EVENT, ResourceType.EVENT, "event-1", "tdp-1");

        mockMvc.perform(withWardDeputySecretary(get("/api/audit-logs?page=0&size=10")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].action").value("UPDATE_EVENT"));
    }

    @Test
    void tdpSecretaryCannotViewAuditLogs() throws Exception {
        saveAuditLog(AuditAction.UPDATE_EVENT, ResourceType.EVENT, "event-1", "tdp-1");

        mockMvc.perform(withTdpSecretary(get("/api/audit-logs")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUDIT_LOG_FORBIDDEN"));
    }

    private void saveAuditLog(
            AuditAction action,
            ResourceType resourceType,
            String resourceId,
            String organizationId
    ) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId("actor-user");
        auditLog.setActorRole("WARD_SECRETARY");
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setOrganizationId(organizationId);
        auditLog.setResult(AuditResult.SUCCESS);
        auditLogRepository.save(auditLog);
    }

    private MockHttpServletRequestBuilder withWardSecretary(MockHttpServletRequestBuilder request) {
        return request
                .header("X-User-Id", "ward-secretary-user")
                .header("X-User-Role", "WARD_SECRETARY")
                .header("X-Organization-Id", "ward-thuong-cat");
    }

    private MockHttpServletRequestBuilder withWardDeputySecretary(MockHttpServletRequestBuilder request) {
        return request
                .header("X-User-Id", "ward-deputy-user")
                .header("X-User-Role", "WARD_DEPUTY_SECRETARY")
                .header("X-Organization-Id", "ward-thuong-cat");
    }

    private MockHttpServletRequestBuilder withTdpSecretary(MockHttpServletRequestBuilder request) {
        return request
                .header("X-User-Id", "tdp-secretary-user")
                .header("X-User-Role", "TDP_SECRETARY")
                .header("X-Organization-Id", "ward-thuong-cat")
                .header("X-Tdp-Id", "tdp-1");
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
