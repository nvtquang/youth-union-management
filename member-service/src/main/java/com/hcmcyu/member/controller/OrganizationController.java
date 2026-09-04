package com.hcmcyu.member.controller;

import com.hcmcyu.member.dto.MemberSummaryResponse;
import com.hcmcyu.member.dto.OrganizationUnitRequest;
import com.hcmcyu.member.dto.OrganizationUnitResponse;
import com.hcmcyu.member.security.CurrentUser;
import com.hcmcyu.member.service.OrganizationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public List<OrganizationUnitResponse> findAll(@AuthenticationPrincipal CurrentUser currentUser) {
        return organizationService.findAll(currentUser);
    }

    @GetMapping("/{id}")
    public OrganizationUnitResponse findById(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return organizationService.findById(id, currentUser);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationUnitResponse create(
            @Valid @RequestBody OrganizationUnitRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return organizationService.create(request, currentUser);
    }

    @PutMapping("/{id}")
    public OrganizationUnitResponse update(
            @PathVariable("id") String id,
            @Valid @RequestBody OrganizationUnitRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return organizationService.update(id, request, currentUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id, @AuthenticationPrincipal CurrentUser currentUser) {
        organizationService.delete(id, currentUser);
    }

    @GetMapping("/{id}/members")
    public List<MemberSummaryResponse> findMembers(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return organizationService.findMembers(id, currentUser);
    }
}
