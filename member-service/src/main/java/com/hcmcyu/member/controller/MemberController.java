package com.hcmcyu.member.controller;

import com.hcmcyu.member.dto.MemberBankingRequest;
import com.hcmcyu.member.dto.MemberBankingResponse;
import com.hcmcyu.member.dto.MemberProfileUpdateRequest;
import com.hcmcyu.member.dto.MemberRequest;
import com.hcmcyu.member.dto.MemberResponse;
import com.hcmcyu.member.dto.MemberRoleUpdateRequest;
import com.hcmcyu.member.entity.MemberStatus;
import com.hcmcyu.member.security.CurrentUser;
import com.hcmcyu.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public Page<MemberResponse> findAll(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "organizationId", required = false) String organizationId,
            @RequestParam(name = "status", required = false) MemberStatus status,
            @PageableDefault(size = 20, sort = "fullName") Pageable pageable,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return memberService.findAll(keyword, organizationId, status, pageable, currentUser);
    }

    @GetMapping("/me")
    public MemberResponse me(@AuthenticationPrincipal CurrentUser currentUser) {
        return memberService.findCurrentProfile(currentUser);
    }

    @PutMapping("/me")
    public MemberResponse updateMe(
            @Valid @RequestBody MemberProfileUpdateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return memberService.updateCurrentProfile(request, currentUser);
    }

    @PostMapping("/me/avatar")
    public MemberResponse uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return memberService.uploadCurrentAvatar(file, currentUser);
    }

    @DeleteMapping("/me/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvatar(@AuthenticationPrincipal CurrentUser currentUser) {
        memberService.deleteCurrentAvatar(currentUser);
    }

    @GetMapping("/me/banking")
    public MemberBankingResponse myBanking(@AuthenticationPrincipal CurrentUser currentUser) {
        return memberService.findCurrentBanking(currentUser);
    }

    @PutMapping("/me/banking")
    public MemberBankingResponse updateMyBanking(
            @Valid @RequestBody MemberBankingRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return memberService.updateCurrentBanking(request, currentUser);
    }

    @PostMapping("/me/banking/qr")
    public MemberBankingResponse uploadMyBankQr(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return memberService.uploadCurrentBankQr(file, currentUser);
    }

    @DeleteMapping("/me/banking/qr")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyBankQr(@AuthenticationPrincipal CurrentUser currentUser) {
        memberService.deleteCurrentBankQr(currentUser);
    }

    @GetMapping("/{id}")
    public MemberResponse findById(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return memberService.findById(id, currentUser);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse create(
            @Valid @RequestBody MemberRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return memberService.create(request, currentUser);
    }

    @PutMapping("/{id}")
    public MemberResponse update(
            @PathVariable("id") String id,
            @Valid @RequestBody MemberRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return memberService.update(id, request, currentUser);
    }

    @PutMapping("/{memberId}/role")
    public MemberResponse updateRole(
            @PathVariable("memberId") String memberId,
            @Valid @RequestBody MemberRoleUpdateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return memberService.updateRole(memberId, request, currentUser);
    }

    @GetMapping("/{memberId}/banking")
    public MemberBankingResponse findBankingByMemberId(
            @PathVariable("memberId") String memberId,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return memberService.findBankingByMemberId(memberId, currentUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id, @AuthenticationPrincipal CurrentUser currentUser) {
        memberService.softDelete(id, currentUser);
    }
}
