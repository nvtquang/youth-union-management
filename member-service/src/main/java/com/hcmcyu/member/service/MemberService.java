package com.hcmcyu.member.service;

import com.hcmcyu.member.dto.MemberRequest;
import com.hcmcyu.member.dto.MemberResponse;
import com.hcmcyu.member.entity.Member;
import com.hcmcyu.member.entity.MemberRole;
import com.hcmcyu.member.entity.MemberStatus;
import com.hcmcyu.member.entity.OrganizationUnit;
import com.hcmcyu.member.entity.OrganizationUnitType;
import com.hcmcyu.member.exception.MemberServiceException;
import com.hcmcyu.member.mapper.MemberMapper;
import com.hcmcyu.member.repository.MemberRepository;
import com.hcmcyu.member.repository.MemberSpecifications;
import com.hcmcyu.member.repository.OrganizationUnitRepository;
import com.hcmcyu.member.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final OrganizationUnitRepository organizationUnitRepository;
    private final MemberMapper memberMapper;
    private final MemberScopeService memberScopeService;

    public MemberService(
            MemberRepository memberRepository,
            OrganizationUnitRepository organizationUnitRepository,
            MemberMapper memberMapper,
            MemberScopeService memberScopeService
    ) {
        this.memberRepository = memberRepository;
        this.organizationUnitRepository = organizationUnitRepository;
        this.memberMapper = memberMapper;
        this.memberScopeService = memberScopeService;
    }

    @Transactional(readOnly = true)
    public Page<MemberResponse> findAll(
            String keyword,
            String organizationId,
            MemberStatus status,
            Pageable pageable,
            CurrentUser currentUser
    ) {
        Specification<Member> specification = Specification
                .where(MemberSpecifications.withinScope(currentUser))
                .and(MemberSpecifications.keywordContains(keyword))
                .and(MemberSpecifications.hasOrganization(organizationId))
                .and(MemberSpecifications.hasStatus(status));

        return memberRepository.findAll(specification, pageable)
                .map(memberMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(String id, CurrentUser currentUser) {
        Member member = getMember(id);
        memberScopeService.requireRead(currentUser, member);
        return memberMapper.toResponse(member);
    }

    @Transactional
    public MemberResponse create(MemberRequest request, CurrentUser currentUser) {
        validateUniqueUserIdForCreate(request.userId());

        OrganizationUnit organization = getOrganization(request.organizationId());
        validateMemberOrganization(organization);
        memberScopeService.requireCreateInOrganization(currentUser, organization.getId());

        Member member = new Member();
        applyRequest(member, request, organization);
        member.setMemberRole(MemberRole.MEMBER);

        return memberMapper.toResponse(memberRepository.save(member));
    }

    @Transactional
    public MemberResponse update(String id, MemberRequest request, CurrentUser currentUser) {
        Member member = getMember(id);
        memberScopeService.requireWrite(currentUser, member);
        validateUniqueUserIdForUpdate(request.userId(), id);

        OrganizationUnit organization = getOrganization(request.organizationId());
        validateMemberOrganization(organization);
        memberScopeService.requireCreateInOrganization(currentUser, organization.getId());

        applyRequest(member, request, organization);
        memberScopeService.requireWrite(currentUser, member);
        return memberMapper.toResponse(member);
    }

    @Transactional
    public void softDelete(String id, CurrentUser currentUser) {
        Member member = getMember(id);
        memberScopeService.requireWrite(currentUser, member);
        member.setMemberStatus(MemberStatus.INACTIVE);
    }

    private void applyRequest(Member member, MemberRequest request, OrganizationUnit organization) {
        member.setUserId(blankToNull(request.userId()));
        member.setFullName(request.fullName().trim());
        member.setDateOfBirth(request.dateOfBirth());
        member.setGender(request.gender());
        member.setPhone(blankToNull(request.phone()));
        member.setEmail(request.email() == null ? null : request.email().trim().toLowerCase());
        member.setAddress(blankToNull(request.address()));
        member.setAvatarUrl(blankToNull(request.avatarUrl()));
        member.setYouthUnionJoinDate(request.youthUnionJoinDate());
        member.setMemberStatus(request.memberStatus() == null ? MemberStatus.ACTIVE : request.memberStatus());
        member.setOrganization(organization);
    }

    private OrganizationUnit getOrganization(String organizationId) {
        return organizationUnitRepository.findById(organizationId)
                .orElseThrow(() -> new MemberServiceException(
                        HttpStatus.NOT_FOUND,
                        "ORGANIZATION_NOT_FOUND",
                        "Organization unit not found"
                ));
    }

    private Member getMember(String id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberServiceException(
                        HttpStatus.NOT_FOUND,
                        "MEMBER_NOT_FOUND",
                        "Member not found"
                ));
    }

    private void validateMemberOrganization(OrganizationUnit organization) {
        if (organization.getType() != OrganizationUnitType.YOUTH_UNION_BRANCH) {
            throw new MemberServiceException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_MEMBER_ORGANIZATION",
                    "Member must belong to a youth union branch"
            );
        }
    }

    private void validateUniqueUserIdForCreate(String userId) {
        String normalizedUserId = blankToNull(userId);
        if (normalizedUserId != null && memberRepository.existsByUserId(normalizedUserId)) {
            throw duplicateUserId();
        }
    }

    private void validateUniqueUserIdForUpdate(String userId, String memberId) {
        String normalizedUserId = blankToNull(userId);
        if (normalizedUserId != null && memberRepository.existsByUserIdAndIdNot(normalizedUserId, memberId)) {
            throw duplicateUserId();
        }
    }

    private MemberServiceException duplicateUserId() {
        return new MemberServiceException(
                HttpStatus.CONFLICT,
                "DUPLICATE_MEMBER_USER_ID",
                "Member userId already exists"
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
