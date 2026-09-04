package com.hcmcyu.member.controller;

import com.hcmcyu.member.dto.MemberRequest;
import com.hcmcyu.member.dto.MemberResponse;
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id, @AuthenticationPrincipal CurrentUser currentUser) {
        memberService.softDelete(id, currentUser);
    }
}
