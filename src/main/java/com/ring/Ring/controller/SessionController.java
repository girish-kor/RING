package com.ring.Ring.controller;

import com.ring.Ring.model.Session;
import com.ring.Ring.model.enums.UserLocation;
import com.ring.Ring.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {
    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<?> createSession(
            @RequestParam("userId") String userId,
            @RequestParam("location") UserLocation location) {
        try {
            log.info("Creating new session for user: {} in location: {}", userId, location);
            return sessionService.createSession(userId, location)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        log.warn("Failed to create session for user: {}", userId);
                        return ResponseEntity.badRequest().body("Session could not be created.");
                    });
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameters for creating session", e);
            return ResponseEntity.badRequest().body("Invalid user ID or location.");
        } catch (Exception e) {
            log.error("Error creating session for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create session.");
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSessionById(@PathVariable String sessionId) {
        try {
            return sessionService.getSessionById(sessionId)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        log.warn("Session not found: {}", sessionId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found.");
                    });
        } catch (Exception e) {
            log.error("Error retrieving session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving session.");
        }
    }

    @PutMapping("/{sessionId}/end")
    public ResponseEntity<?> endSession(
            @PathVariable String sessionId,
            @RequestParam(value = "reason", defaultValue = "User ended session") String reason) {
        try {
            log.info("Ending session: {} with reason: {}", sessionId, reason);
            Session endedSession = sessionService.endSession(sessionId, reason);
            return ResponseEntity.ok(endedSession);
        } catch (IllegalArgumentException e) {
            log.error("Invalid session ID for ending session: {}", sessionId, e);
            return ResponseEntity.badRequest().body("Invalid session ID.");
        } catch (ResponseStatusException e) {
            log.warn("Session not found to end: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found.");
        } catch (Exception e) {
            log.error("Error ending session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to end session.");
        }
    }

    @PostMapping("/reconnect")
    public ResponseEntity<?> reconnectUser(
            @RequestParam("userId") String userId,
            @RequestParam("location") UserLocation location) {
        try {
            log.info("Reconnecting user: {} in location: {}", userId, location);
            return sessionService.reconnectUser(userId, location)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        log.warn("Failed to reconnect user: {}", userId);
                        return ResponseEntity.badRequest().body("Reconnection failed.");
                    });
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameters for reconnecting user", e);
            return ResponseEntity.badRequest().body("Invalid user ID or location.");
        } catch (Exception e) {
            log.error("Error reconnecting user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reconnect user.");
        }
    }
}
