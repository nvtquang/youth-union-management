package com.hcmcyu.chat.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        String id,
        String conversationId,
        String senderId,
        String senderName,
        String content,
        LocalDateTime createdAt
) {
}
