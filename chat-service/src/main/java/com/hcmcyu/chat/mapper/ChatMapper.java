package com.hcmcyu.chat.mapper;

import com.hcmcyu.chat.dto.ConversationResponse;
import com.hcmcyu.chat.dto.MessageResponse;
import com.hcmcyu.chat.entity.Conversation;
import com.hcmcyu.chat.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    public ConversationResponse toResponse(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getTitle(),
                conversation.getCreatedBy(),
                conversation.getMembers().stream()
                        .map(member -> member.getMemberId())
                        .sorted()
                        .toList(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    public MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSenderId(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
