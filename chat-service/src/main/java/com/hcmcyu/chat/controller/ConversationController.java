package com.hcmcyu.chat.controller;

import com.hcmcyu.chat.dto.ConversationCreateRequest;
import com.hcmcyu.chat.dto.ConversationMemberRequest;
import com.hcmcyu.chat.dto.ConversationResponse;
import com.hcmcyu.chat.dto.MessageResponse;
import com.hcmcyu.chat.security.CurrentUser;
import com.hcmcyu.chat.service.ConversationService;
import com.hcmcyu.chat.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Chat", description = "Direct and group conversation REST APIs. WebSocket STOMP is available at /ws.")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token"),
        @ApiResponse(responseCode = "403", description = "Conversation membership denied"),
        @ApiResponse(responseCode = "404", description = "Conversation not found"),
        @ApiResponse(responseCode = "409", description = "Duplicate membership or invalid conversation state")
})
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    public ConversationController(ConversationService conversationService, MessageService messageService) {
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create conversation", description = "Creates DIRECT or GROUP conversation. The creator is taken from JWT identity.")
    public ConversationResponse create(
            @Valid @RequestBody ConversationCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return conversationService.create(request, currentUser);
    }

    @GetMapping
    @Operation(summary = "List my conversations", description = "Returns conversations where the current member is a participant.")
    public List<ConversationResponse> findMine(@AuthenticationPrincipal CurrentUser currentUser) {
        return conversationService.findMine(currentUser);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get conversation by id", description = "Only conversation members can access the conversation.")
    public ConversationResponse findById(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return conversationService.findById(id, currentUser);
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Get message history", description = "Paginated message history. Only conversation members can read messages.")
    public Page<MessageResponse> findMessages(
            @PathVariable("id") String id,
            Pageable pageable,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return messageService.findMessages(id, pageable, currentUser);
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to group", description = "Adds a member to a group conversation when the current member is allowed to manage membership.")
    public ConversationResponse addMember(
            @PathVariable("id") String id,
            @Valid @RequestBody ConversationMemberRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return conversationService.addMember(id, request, currentUser);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove member from group", description = "Removes a member from a group conversation when the current member is allowed to manage membership.")
    public void removeMember(
            @PathVariable("id") String id,
            @PathVariable("memberId") String memberId,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        conversationService.removeMember(id, memberId, currentUser);
    }
}
