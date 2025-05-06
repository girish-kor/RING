package com.ring.Ring.util;

import com.ring.Ring.model.ChatMessage;
import com.ring.Ring.model.enums.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SessionUtils {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendSystemMessage(String userId, String sessionId, String content, MessageType type) {
        ChatMessage message = ChatMessage.builder()
                .sessionId(sessionId)
                .senderId("system")
                .receiverId(userId)
                .content(content)
                .type(type)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSendToUser(userId, "/queue/messages", message);
    }

    public void notifyUsersAboutSessionEnd(String userOneId, String userTwoId, String sessionId, String reason) {
        String content = "Session ended: " + reason;

        sendSystemMessage(userOneId, sessionId, content, MessageType.SESSION_END);
        sendSystemMessage(userTwoId, sessionId, content, MessageType.SESSION_END);
    }

    public void notifyUserAboutReconnection(String userId, String sessionId) {
        String content = "Partner disconnected. Finding a new partner...";
        sendSystemMessage(userId, sessionId, content, MessageType.RECONNECT);
    }
}
