package com.ring.Ring.controller;

import com.ring.Ring.model.ChatMessage;
import com.ring.Ring.model.enums.MessageType;
import com.ring.Ring.service.FilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final FilterService filterService;

    @MessageMapping("/chat.join")
    public void join(ChatMessage message) {
        try {
            // Automatically send the user ID as part of the join message
            message.setSenderId("User_" + message.getSessionId());
            message.setTimestamp(LocalDateTime.now());
            message.setType(MessageType.JOIN);

            log.info("User joined: {} in session: {}", message.getSenderId(), message.getSessionId());

            // Send join message to both users (automatically sending user ID)
            messagingTemplate.convertAndSendToUser(
                    message.getSenderId(),
                    "/queue/messages",
                    message);

            if (message.getReceiverId() != null) {
                messagingTemplate.convertAndSendToUser(
                        message.getReceiverId(),
                        "/queue/messages",
                        message);
            }
        } catch (Exception e) {
            log.error("Error handling user join for session: {}", message.getSessionId(), e);
            sendErrorMessage(message.getSenderId(), "Error processing join request.");
        }
    }


    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage message) {
        try {
            message.setTimestamp(LocalDateTime.now());
            message.setType(MessageType.CHAT);

            log.debug("Received message from user: {} in session: {}", message.getSenderId(), message.getSessionId());

            // Filter text content for inappropriate language or personal information
            if (filterService.filterTextContent(message.getSessionId(), message.getContent())) {
                log.warn("Message blocked due to inappropriate content from user: {}", message.getSenderId());
                sendErrorMessage(message.getSenderId(), "Inappropriate content detected.");
                return;
            }

            // Send message to the receiver
            messagingTemplate.convertAndSendToUser(
                    message.getReceiverId(),
                    "/queue/messages",
                    message);

            // Echo back to sender for confirmation
            messagingTemplate.convertAndSendToUser(
                    message.getSenderId(),
                    "/queue/messages",
                    message);

        } catch (Exception e) {
            log.error("Error processing message from user: {} in session: {}", message.getSenderId(), message.getSessionId(), e);
            sendErrorMessage(message.getSenderId(), "Error sending message.");
        }
    }

    @MessageMapping("/chat.webrtc")
    public void handleWebRTC(ChatMessage message) {
        try {
            message.setTimestamp(LocalDateTime.now());
            message.setType(MessageType.WEBRTC);

            log.debug("Received WebRTC signal from user: {} in session: {}", message.getSenderId(), message.getSessionId());

            // Forward WebRTC signaling data to the receiver
            messagingTemplate.convertAndSendToUser(
                    message.getReceiverId(),
                    "/queue/messages",
                    message);

        } catch (Exception e) {
            log.error("Error processing WebRTC message from user: {} in session: {}", message.getSenderId(), message.getSessionId(), e);
            sendErrorMessage(message.getSenderId(), "Error processing WebRTC request.");
        }
    }

    @MessageMapping("/chat.leave")
    public void leave(ChatMessage message) {
        try {
            message.setTimestamp(LocalDateTime.now());
            message.setType(MessageType.LEAVE);

            log.info("User left: {} from session: {}", message.getSenderId(), message.getSessionId());

            // Notify both users
            messagingTemplate.convertAndSendToUser(
                    message.getSenderId(),
                    "/queue/messages",
                    message);

            if (message.getReceiverId() != null) {
                messagingTemplate.convertAndSendToUser(
                        message.getReceiverId(),
                        "/queue/messages",
                        message);
            }
        } catch (Exception e) {
            log.error("Error handling user leave for session: {}", message.getSessionId(), e);
            sendErrorMessage(message.getSenderId(), "Error processing leave request.");
        }
    }

    private void sendErrorMessage(String userId, String errorMessage) {
        ChatMessage errorMsg = new ChatMessage();
        errorMsg.setContent(errorMessage);
        errorMsg.setType(MessageType.CHAT);  // Using CHAT as the type for error messages
        errorMsg.setTimestamp(LocalDateTime.now());
        messagingTemplate.convertAndSendToUser(userId, "/queue/messages", errorMsg);
    }
}
