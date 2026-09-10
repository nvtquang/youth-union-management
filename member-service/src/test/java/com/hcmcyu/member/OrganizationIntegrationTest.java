package com.hcmcyu.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmcyu.member.entity.OrganizationUnit;
import com.hcmcyu.member.entity.OrganizationUnitType;
import com.hcmcyu.member.repository.MemberRepository;
import com.hcmcyu.member.repository.OrganizationUnitRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:hcmcyu_member_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class OrganizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationUnitRepository organizationUnitRepository;

    @Autowired
    private MemberRepository memberRepository;

    private OrganizationUnit ward;
    private OrganizationUnit tdp1;
    private OrganizationUnit tdp2;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        organizationUnitRepository.deleteAll();

        ward = new OrganizationUnit();
        ward.setName("Phuong Thuong Cat");
        ward.setCode("THUONG_CAT");
        ward.setType(OrganizationUnitType.WARD);
        ward = organizationUnitRepository.save(ward);

        tdp1 = new OrganizationUnit();
        tdp1.setName("Chi doan TDP 1");
        tdp1.setCode("TDP_1");
        tdp1.setType(OrganizationUnitType.YOUTH_UNION_BRANCH);
        tdp1.setParent(ward);
        tdp1 = organizationUnitRepository.save(tdp1);

        tdp2 = new OrganizationUnit();
        tdp2.setName("Chi doan TDP 2");
        tdp2.setCode("TDP_2");
        tdp2.setType(OrganizationUnitType.YOUTH_UNION_BRANCH);
        tdp2.setParent(ward);
        tdp2 = organizationUnitRepository.save(tdp2);
    }

    @Test
    void wardSecretaryCanReadAllOrganizations() throws Exception {
        mockMvc.perform(withWardSecretary(get("/api/organizations")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void publicOrganizationListReturnsActiveTdpBranchesForRegistration() throws Exception {
        mockMvc.perform(get("/api/organizations/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(tdp1.getId())).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(tdp2.getId())).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(ward.getId())).doesNotExist());
    }

    @Test
    void internalRegisterCreatesMemberProfileInSelectedTdp() throws Exception {
        MvcResult result = mockMvc.perform(post("/internal/members/register")
                        .header("X-Internal-Secret", "dev-internal-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "userId", "registered-user-1",
                                "fullName", "Nguyen Van Dang Ky",
                                "email", "registered@example.com",
                                "phone", "0900000000",
                                "organizationId", tdp1.getId()
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("registered-user-1"))
                .andExpect(jsonPath("$.organizationId").value(tdp1.getId()))
                .andReturn();

        String memberId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
        var member = memberRepository.findById(memberId).orElseThrow();
        assertThat(member.getUserId()).isEqualTo("registered-user-1");
        assertThat(member.getOrganization().getId()).isEqualTo(tdp1.getId());
        assertThat(member.getMemberRole().name()).isEqualTo("MEMBER");
    }

    @Test
    void tdpSecretaryCanOnlySeeWardAndOwnTdpInList() throws Exception {
        MvcResult result = mockMvc.perform(withTdpSecretary(get("/api/organizations"), tdp1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.findValuesAsText("id")).contains(ward.getId(), tdp1.getId());
        assertThat(response.findValuesAsText("id")).doesNotContain(tdp2.getId());
    }

    @Test
    void tdpSecretaryOfTdp1CannotReadTdp2() throws Exception {
        mockMvc.perform(withTdpSecretary(get("/api/organizations/{id}", tdp2.getId()), tdp1.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("OUT_OF_SCOPE"));
    }

    @Test
    void tdpSecretaryOfTdp1CannotUpdateTdp2() throws Exception {
        mockMvc.perform(withTdpSecretary(put("/api/organizations/{id}", tdp2.getId()), tdp1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Chi doan TDP 2 updated",
                                "code", "TDP_2_UPDATED",
                                "type", "YOUTH_UNION_BRANCH",
                                "parentId", ward.getId()
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("OUT_OF_SCOPE"));

        OrganizationUnit unchanged = organizationUnitRepository.findById(tdp2.getId()).orElseThrow();
        assertThat(unchanged.getCode()).isEqualTo("TDP_2");
    }

    @Test
    void tdpSecretaryOfTdp1CannotDeleteTdp2() throws Exception {
        mockMvc.perform(withTdpSecretary(delete("/api/organizations/{id}", tdp2.getId()), tdp1.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("OUT_OF_SCOPE"));

        assertThat(organizationUnitRepository.existsById(tdp2.getId())).isTrue();
    }

    @Test
    void tdpSecretaryCanUpdateOwnTdp() throws Exception {
        mockMvc.perform(withTdpSecretary(put("/api/organizations/{id}", tdp1.getId()), tdp1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Chi doan TDP 1 moi",
                                "code", "TDP_1_NEW",
                                "type", "YOUTH_UNION_BRANCH",
                                "parentId", ward.getId()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tdp1.getId()))
                .andExpect(jsonPath("$.code").value("TDP_1_NEW"));
    }

    @Test
    void memberCannotUpdateOrganization() throws Exception {
        mockMvc.perform(withMember(put("/api/organizations/{id}", tdp1.getId()), tdp1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Chi doan TDP 1 moi",
                                "code", "TDP_1_NEW",
                                "type", "YOUTH_UNION_BRANCH",
                                "parentId", ward.getId()
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("OUT_OF_SCOPE"));
    }

    @Test
    void wardSecretaryCanCreateOrganization() throws Exception {
        mockMvc.perform(withWardSecretary(post("/api/organizations"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Chi doan TDP 3",
                                "code", "TDP_3",
                                "type", "YOUTH_UNION_BRANCH",
                                "parentId", ward.getId()
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Chi doan TDP 3"))
                .andExpect(jsonPath("$.type").value("YOUTH_UNION_BRANCH"));
    }

    @Test
    void tdpSecretaryCannotCreateOrganization() throws Exception {
        mockMvc.perform(withTdpSecretary(post("/api/organizations"), tdp1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Chi doan TDP 3",
                                "code", "TDP_3",
                                "type", "YOUTH_UNION_BRANCH",
                                "parentId", ward.getId()
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void getOrganizationMembersChecksScopeBeforeReturningEmptyList() throws Exception {
        mockMvc.perform(withTdpSecretary(get("/api/organizations/{id}/members", tdp2.getId()), tdp1.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("OUT_OF_SCOPE"));

        mockMvc.perform(withTdpSecretary(get("/api/organizations/{id}/members", tdp1.getId()), tdp1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void organizationApiWithoutUserContextReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private MockHttpServletRequestBuilder withWardSecretary(MockHttpServletRequestBuilder request) {
        return request
                .header("X-User-Id", "ward-secretary-user")
                .header("X-User-Role", "WARD_SECRETARY")
                .header("X-Organization-Id", ward.getId());
    }

    private MockHttpServletRequestBuilder withTdpSecretary(
            MockHttpServletRequestBuilder request,
            String tdpId
    ) {
        return request
                .header("X-User-Id", "tdp-secretary-user")
                .header("X-User-Role", "TDP_SECRETARY")
                .header("X-Organization-Id", ward.getId())
                .header("X-Tdp-Id", tdpId);
    }

    private MockHttpServletRequestBuilder withMember(MockHttpServletRequestBuilder request, String tdpId) {
        return request
                .header("X-User-Id", "member-user")
                .header("X-Member-Id", "member-1")
                .header("X-User-Role", "MEMBER")
                .header("X-Organization-Id", ward.getId())
                .header("X-Tdp-Id", tdpId);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
