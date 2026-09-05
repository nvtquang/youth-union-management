package com.hcmcyu.chat.controller;

import com.hcmcyu.chat.dto.ConversationCreateRequest;
import com.hcmcyu.chat.dto.ConversationMemberRequest;
import com.hcmcyu.chat.dto.ConversationResponse;
import com.hcmcyu.chat.dto.MessageResponse;
import com.hcmcyu.chat.security.CurrentUser;
import com.hcmcyu.chat.service.ConversationService;
import com.hcmcyu.chat.service.MessageService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    public ConversationController(ConversationService conversationService, MessageService messageService) {
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse create(
            @Valid @RequestBody ConversationCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return conversationService.create(request, currentUser);
    }

    @GetMapping
    public List<ConversationResponse> findMine(@AuthenticationPrincipal CurrentUser currentUser) {
        return conversationService.findMine(currentUser);
    }

    @GetMapping("/{id}")
    public ConversationResponse findById(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return conversationService.findById(id, currentUser);
    }

    @GetMapping("/{id}/messages")
    public Page<MessageResponse> findMessages(
            @PathVariable("id") String id,
            Pageable pageable,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return messageService.findMessages(id, pageable, currentUser);
    }

    @PostMapping("/{id}/members")
    public ConversationResponse addMember(
            @PathVariable("id") String id,
            @Valid @RequestBody ConversationMemberRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return conversationService.addMember(id, request, currentUser);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @PathVariable("id") String id,
            @PathVariable("memberId") String memberId,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        conversationService.removeMember(id, memberId, currentUser);
    }
}
