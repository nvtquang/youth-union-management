package com.hcmcyu.chat.service;

import com.hcmcyu.chat.dto.ChatMessageRequest;
import com.hcmcyu.chat.dto.MessageResponse;
import com.hcmcyu.chat.entity.Conversation;
import com.hcmcyu.chat.entity.Message;
import com.hcmcyu.chat.exception.ChatServiceException;
import com.hcmcyu.chat.mapper.ChatMapper;
import com.hcmcyu.chat.repository.ConversationRepository;
import com.hcmcyu.chat.repository.MessageRepository;
import com.hcmcyu.chat.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ChatAuthorizationService authorizationService;
    private final ChatMapper chatMapper;

    public MessageService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            ChatAuthorizationService authorizationService,
            ChatMapper chatMapper
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.authorizationService = authorizationService;
        this.chatMapper = chatMapper;
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> findMessages(String conversationId, Pageable pageable, CurrentUser currentUser) {
        authorizationService.requireConversationMember(conversationId, currentUser);
        return messageRepository.findByConversation_Id(conversationId, pageable).map(chatMapper::toResponse);
    }

    @Transactional
    public MessageResponse sendMessage(String conversationId, ChatMessageRequest request, CurrentUser currentUser) {
        String senderId = authorizationService.requireMemberContext(currentUser);
        authorizationService.requireConversationMember(conversationId, currentUser);
        String content = validateContent(request);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ChatServiceException(
                        HttpStatus.NOT_FOUND,
                        "CONVERSATION_NOT_FOUND",
                        "Conversation not found"
                ));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderId(senderId);
        message.setContent(content);
        return chatMapper.toResponse(messageRepository.save(message));
    }

    private String validateContent(ChatMessageRequest request) {
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new ChatServiceException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_MESSAGE_CONTENT",
                    "Message content is required"
            );
        }
        String content = request.content().trim();
        if (content.length() > 2000) {
            throw new ChatServiceException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_MESSAGE_CONTENT",
                    "Message content must not exceed 2000 characters"
            );
        }
        return content;
    }
}
