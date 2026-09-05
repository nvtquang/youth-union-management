package com.hcmcyu.chat.dto;

import com.hcmcyu.chat.entity.ConversationType;
import java.time.LocalDateTime;
import java.util.List;

public record ConversationResponse(
        String id,
        ConversationType type,
        String title,
        String createdBy,
        List<String> memberIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
