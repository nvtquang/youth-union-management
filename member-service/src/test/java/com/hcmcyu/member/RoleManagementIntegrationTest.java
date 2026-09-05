package com.hcmcyu.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmcyu.member.entity.Member;
import com.hcmcyu.member.entity.MemberRole;
import com.hcmcyu.member.entity.MemberStatus;
import com.hcmcyu.member.entity.OrganizationUnit;
import com.hcmcyu.member.entity.OrganizationUnitType;
import com.hcmcyu.member.repository.MemberRepository;
import com.hcmcyu.member.repository.MemberRoleChangeAuditRepository;
import com.hcmcyu.member.repository.OrganizationUnitRepository;
import com.hcmcyu.member.service.AuthRoleClient;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:hcmcyu_role_management_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class RoleManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationUnitRepository organizationUnitRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberRoleChangeAuditRepository auditRepository;

    @MockBean
    private AuthRoleClient authRoleClient;

    private OrganizationUnit ward;
    private OrganizationUnit tdp1;
    private Member targetMember;

    @BeforeEach
    void setUp() {
        auditRepository.deleteAll();
        memberRepository.deleteAll();
        organizationUnitRepository.deleteAll();

        ward = saveOrganization("Phuong Thuong Cat", "THUONG_CAT", OrganizationUnitType.WARD, null);
        tdp1 = saveOrganization("Chi doan TDP 1", "TDP_1", OrganizationUnitType.YOUTH_UNION_BRANCH, ward);
        targetMember = saveMember("Nguyen Van An", "target-user", tdp1, MemberRole.MEMBER);
    }

    @Test
    void wardSecretaryCanUpdateRoleAndSyncAuthService() throws Exception {
        mockMvc.perform(withWardSecretary(put("/api/members/{memberId}/role", targetMember.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("role", "TDP_SECRETARY"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetMember.getId()))
                .andExpect(jsonPath("$.memberRole").value("TDP_SECRETARY"));

        Member updated = memberRepository.findById(targetMember.getId()).orElseThrow();
        assertThat(updated.getMemberRole()).isEqualTo(MemberRole.TDP_SECRETARY);
        assertThat(auditRepository.findAll()).singleElement()
                .satisfies(audit -> {
                    assertThat(audit.getMemberId()).isEqualTo(targetMember.getId());
                    assertThat(audit.getOldRole()).isEqualTo(MemberRole.MEMBER);
                    assertThat(audit.getNewRole()).isEqualTo(MemberRole.TDP_SECRETARY);
                    assertThat(audit.getActorUserId()).isEqualTo("ward-secretary-user");
                });
        verify(authRoleClient).updateUserRole(
                "target-user",
                MemberRole.TDP_SECRETARY,
                "ward-secretary-user",
                targetMember.getId()
        );
    }

    @Test
    void wardDeputySecretaryCannotUpdateRole() throws Exception {
        mockMvc.perform(withWardDeputySecretary(put("/api/members/{memberId}/role", targetMember.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("role", "TDP_SECRETARY"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ROLE_UPDATE_FORBIDDEN"));

        assertThat(memberRepository.findById(targetMember.getId()).orElseThrow().getMemberRole())
                .isEqualTo(MemberRole.MEMBER);
        verify(authRoleClient, never()).updateUserRole(
                "target-user",
                MemberRole.TDP_SECRETARY,
                "ward-deputy-user",
                targetMember.getId()
        );
    }

    @Test
    void tdpSecretaryCannotUpdateRole() throws Exception {
        mockMvc.perform(withTdpSecretary(put("/api/members/{memberId}/role", targetMember.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("role", "TDP_SECRETARY"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ROLE_UPDATE_FORBIDDEN"));

        assertThat(memberRepository.findById(targetMember.getId()).orElseThrow().getMemberRole())
                .isEqualTo(MemberRole.MEMBER);
    }

    @Test
    void memberCannotUpdateRole() throws Exception {
        mockMvc.perform(withMember(put("/api/members/{memberId}/role", targetMember.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("role", "TDP_SECRETARY"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ROLE_UPDATE_FORBIDDEN"));

        assertThat(memberRepository.findById(targetMember.getId()).orElseThrow().getMemberRole())
                .isEqualTo(MemberRole.MEMBER);
    }

    @Test
    void cannotRemoveLastWardSecretaryRole() throws Exception {
        Member wardSecretary = saveMember("Bi thu phuong", "last-ward-secretary-user", tdp1, MemberRole.WARD_SECRETARY);

        mockMvc.perform(withWardSecretary(put("/api/members/{memberId}/role", wardSecretary.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("role", "MEMBER"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LAST_WARD_SECRETARY"));

        assertThat(memberRepository.findById(wardSecretary.getId()).orElseThrow().getMemberRole())
                .isEqualTo(MemberRole.WARD_SECRETARY);
    }

    private OrganizationUnit saveOrganization(
            String name,
            String code,
            OrganizationUnitType type,
            OrganizationUnit parent
    ) {
        OrganizationUnit organizationUnit = new OrganizationUnit();
        organizationUnit.setName(name);
        organizationUnit.setCode(code);
        organizationUnit.setType(type);
        organizationUnit.setParent(parent);
        return organizationUnitRepository.save(organizationUnit);
    }

    private Member saveMember(String fullName, String userId, OrganizationUnit organization, MemberRole role) {
        Member member = new Member();
        member.setFullName(fullName);
        member.setUserId(userId);
        member.setEmail(userId + "@example.com");
        member.setMemberStatus(MemberStatus.ACTIVE);
        member.setMemberRole(role);
        member.setOrganization(organization);
        return memberRepository.save(member);
    }

    private MockHttpServletRequestBuilder withWardSecretary(MockHttpServletRequestBuilder request) {
        return request
                .header("X-User-Id", "ward-secretary-user")
                .header("X-User-Role", "WARD_SECRETARY")
                .header("X-Organization-Id", ward.getId());
    }

    private MockHttpServletRequestBuilder withWardDeputySecretary(MockHttpServletRequestBuilder request) {
        return request
                .header("X-User-Id", "ward-deputy-user")
                .header("X-User-Role", "WARD_DEPUTY_SECRETARY")
                .header("X-Organization-Id", ward.getId());
    }

    private MockHttpServletRequestBuilder withTdpSecretary(MockHttpServletRequestBuilder request) {
        return request
                .header("X-User-Id", "tdp-secretary-user")
                .header("X-User-Role", "TDP_SECRETARY")
                .header("X-Organization-Id", ward.getId())
                .header("X-Tdp-Id", tdp1.getId());
    }

    private MockHttpServletRequestBuilder withMember(MockHttpServletRequestBuilder request) {
        return request
                .header("X-User-Id", "member-user")
                .header("X-Member-Id", targetMember.getId())
                .header("X-User-Role", "MEMBER")
                .header("X-Organization-Id", ward.getId())
                .header("X-Tdp-Id", tdp1.getId());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
