package com.ring.Ring.model;

import com.ring.Ring.model.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String sessionId;
    private String senderId;
    private String receiverId;
    private String content;
    private MessageType type;
    private LocalDateTime timestamp;
}