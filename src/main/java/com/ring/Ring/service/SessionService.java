package com.ring.Ring.service;

import com.ring.Ring.model.ChatMessage;
import com.ring.Ring.model.Session;
import com.ring.Ring.model.User;
import com.ring.Ring.model.enums.MessageType;
import com.ring.Ring.model.enums.SessionStatus;
import com.ring.Ring.model.enums.UserLocation;
import com.ring.Ring.model.enums.UserStatus;
import com.ring.Ring.repository.SessionRepository;
import com.ring.Ring.util.SessionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {
    private final SessionRepository sessionRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SessionUtils sessionUtils;

    public Optional<Session> createSession(String userId, UserLocation location) {
        // Check if user exists and is active
        User user = userService.getActiveUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found or not active: " + userId));

        // Find an available partner
        List<User> availableUsers = userService.findAvailableUsersByLocation(location, userId);

        if (availableUsers.isEmpty()) {
            log.info("No available users found for location: {}", location);
            return Optional.empty();
        }

        // Get the first available user
        User partner = availableUsers.get(0);

        // Check if partner is active
        if (partner.getStatus() != UserStatus.ACTIVE) {
            log.info("Partner is not active: {}", partner.getId());
            return Optional.empty();
        }

        // Create a new session
        Session session = Session.builder()
                .id(UUID.randomUUID().toString())
                .userOneId(userId)
                .userTwoId(partner.getId())
                .status(SessionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        Session savedSession = sessionRepository.save(session);

        // Update user session status
        userService.updateUserSessionStatus(userId, true);
        userService.updateUserSessionStatus(partner.getId(), true);

        log.info("Created new session: {} between users: {} and {}",
                savedSession.getId(), userId, partner.getId());

        return Optional.of(savedSession);
    }

    public Optional<Session> getSessionById(String sessionId) {
        return sessionRepository.findById(sessionId);
    }

    public Optional<Session> getActiveSessionByUserId(String userId) {
        return sessionRepository.findActiveSessionByUserId(userId, SessionStatus.ACTIVE);
    }

    public Session endSession(String sessionId, String reason) {
        return sessionRepository.findById(sessionId)
                .map(session -> {
                    if (session.getStatus() == SessionStatus.ACTIVE) {
                        session.setStatus(SessionStatus.ENDED);
                        session.setEndedAt(LocalDateTime.now());
                        session.setEndReason(reason);

                        // Update user session status
                        userService.updateUserSessionStatus(session.getUserOneId(), false);
                        userService.updateUserSessionStatus(session.getUserTwoId(), false);

                        // Notify users that session has ended
                        ChatMessage endMessage = ChatMessage.builder()
                                .sessionId(sessionId)
                                .senderId("system")
                                .content("Session has ended: " + reason)
                                .type(MessageType.SESSION_END)
                                .timestamp(LocalDateTime.now())
                                .build();

                        messagingTemplate.convertAndSendToUser(
                                session.getUserOneId(),
                                "/queue/messages",
                                endMessage);

                        messagingTemplate.convertAndSendToUser(
                                session.getUserTwoId(),
                                "/queue/messages",
                                endMessage);

                        log.info("Ended session: {} with reason: {}", sessionId, reason);
                        return sessionRepository.save(session);
                    }
                    return session;
                })
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + sessionId));
    }

    public Optional<Session> reconnectUser(String userId, UserLocation location) {
        // End any existing active session
        sessionRepository.findActiveSessionByUserId(userId, SessionStatus.ACTIVE)
                .ifPresent(session -> endSession(session.getId(), "User requested reconnection"));

        // Create a new session
        return createSession(userId, location);
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupOldSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);
        List<Session> oldSessions = sessionRepository.findOldActiveSessions(threshold, SessionStatus.ACTIVE);

        for (Session session : oldSessions) {
            log.info("Ending old session: {}", session.getId());
            endSession(session.getId(), "Session timeout");
        }
    }

    public void handleInappropriateContent(String sessionId, String reason) {
        log.warn("Inappropriate content detected in session: {}, reason: {}", sessionId, reason);
        endSession(sessionId, "Inappropriate content detected: " + reason);
    }
}
