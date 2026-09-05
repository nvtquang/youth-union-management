package com.hcmcyu.member.config;

import com.hcmcyu.member.entity.Gender;
import com.hcmcyu.member.entity.Member;
import com.hcmcyu.member.entity.MemberRole;
import com.hcmcyu.member.entity.MemberStatus;
import com.hcmcyu.member.entity.OrganizationUnit;
import com.hcmcyu.member.entity.OrganizationUnitType;
import com.hcmcyu.member.repository.MemberRepository;
import com.hcmcyu.member.repository.OrganizationUnitRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private static final String WARD_ID = "ward-thuong-cat";

    private final OrganizationUnitRepository organizationUnitRepository;
    private final MemberRepository memberRepository;

    public DevDataSeeder(
            OrganizationUnitRepository organizationUnitRepository,
            MemberRepository memberRepository
    ) {
        this.organizationUnitRepository = organizationUnitRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(String... args) {
        OrganizationUnit ward = seedOrganization(
                WARD_ID,
                "Phuong Thuong Cat",
                "THUONG_CAT",
                OrganizationUnitType.WARD,
                null
        );
        for (int tdp = 1; tdp <= 5; tdp++) {
            OrganizationUnit branch = seedOrganization(
                    "tdp-" + tdp,
                    "Chi doan TDP " + tdp,
                    "TDP_" + tdp,
                    OrganizationUnitType.YOUTH_UNION_BRANCH,
                    ward
            );
            seedTdpMembers(tdp, branch);
        }
        seedWardOfficers(organizationUnitRepository.findById("tdp-1").orElseThrow());
    }

    private OrganizationUnit seedOrganization(
            String id,
            String name,
            String code,
            OrganizationUnitType type,
            OrganizationUnit parent
    ) {
        return organizationUnitRepository.findById(id).orElseGet(() -> {
            OrganizationUnit organization = new OrganizationUnit();
            organization.setId(id);
            organization.setName(name);
            organization.setCode(code);
            organization.setType(type);
            organization.setParent(parent);
            organization.setActive(true);
            return organizationUnitRepository.save(organization);
        });
    }

    private void seedWardOfficers(OrganizationUnit defaultBranch) {
        seedMember(
                "ward-secretary-member",
                "user-ward-secretary",
                "Nguyen Van Bi Thu Phuong",
                "ward.secretary@hcmcyu.local",
                defaultBranch,
                MemberRole.WARD_SECRETARY,
                Gender.MALE,
                1
        );
        seedMember(
                "ward-deputy-member",
                "user-ward-deputy",
                "Tran Thi Pho Bi Thu Phuong",
                "ward.deputy@hcmcyu.local",
                defaultBranch,
                MemberRole.WARD_DEPUTY_SECRETARY,
                Gender.FEMALE,
                2
        );
    }

    private void seedTdpMembers(int tdp, OrganizationUnit branch) {
        seedMember(
                "tdp-" + tdp + "-secretary-member",
                "user-tdp-" + tdp + "-secretary",
                "Bi Thu TDP " + tdp,
                "tdp" + tdp + ".secretary@hcmcyu.local",
                branch,
                MemberRole.TDP_SECRETARY,
                Gender.MALE,
                tdp * 10 + 1
        );
        seedMember(
                "tdp-" + tdp + "-deputy-member",
                "user-tdp-" + tdp + "-deputy",
                "Pho Bi Thu TDP " + tdp,
                "tdp" + tdp + ".deputy@hcmcyu.local",
                branch,
                MemberRole.TDP_DEPUTY_SECRETARY,
                Gender.FEMALE,
                tdp * 10 + 2
        );
        for (int member = 1; member <= 5; member++) {
            seedMember(
                    "tdp-" + tdp + "-member-" + member,
                    "user-tdp-" + tdp + "-member-" + member,
                    "Doan Vien TDP " + tdp + "." + member,
                    "tdp" + tdp + ".member" + member + "@hcmcyu.local",
                    branch,
                    MemberRole.MEMBER,
                    member % 2 == 0 ? Gender.FEMALE : Gender.MALE,
                    tdp * 10 + member + 2
            );
        }
    }

    private void seedMember(
            String id,
            String userId,
            String fullName,
            String email,
            OrganizationUnit organization,
            MemberRole role,
            Gender gender,
            int sampleNumber
    ) {
        if (memberRepository.existsById(id) || memberRepository.existsByUserId(userId)) {
            return;
        }
        Member member = new Member();
        member.setId(id);
        member.setUserId(userId);
        member.setFullName(fullName);
        member.setDateOfBirth(LocalDate.of(2000 + (sampleNumber % 6), ((sampleNumber % 12) + 1), 10));
        member.setGender(gender);
        member.setPhone("0900%06d".formatted(sampleNumber));
        member.setEmail(email);
        member.setAddress("Phuong Thuong Cat, Bac Tu Liem, Ha Noi");
        member.setAvatarUrl("/uploads/avatars/dev-avatar-" + sampleNumber + ".png");
        member.setYouthUnionJoinDate(LocalDate.of(2022, 3, 26));
        member.setMemberStatus(MemberStatus.ACTIVE);
        member.setMemberRole(role);
        member.setOrganization(organization);
        member.setBankName("Demo Bank");
        member.setBankCode("DEMO");
        member.setAccountNumber("DEMO%08d".formatted(sampleNumber));
        member.setAccountHolderName(fullName.toUpperCase());
        member.setBankQrImageUrl("/uploads/bank-qr/dev-bank-qr-" + sampleNumber + ".png");
        memberRepository.save(member);
    }
}
