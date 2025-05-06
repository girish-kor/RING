package com.ring.Ring.controller;

import com.ring.Ring.service.FilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map; // Add this import

@RestController
@RequestMapping("/api/filter")
@RequiredArgsConstructor
@Slf4j
public class FilterController {
    private final FilterService filterService;

    @PostMapping("/text/{sessionId}")
    public ResponseEntity<?> filterTextContent(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) { // Change here
        try {
            String text = body.get("text");
            log.debug("Filtering text content for session: {}", sessionId);
            boolean inappropriate = filterService.filterTextContent(sessionId, text);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid input for filtering text: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid session ID or text input.");
        } catch (Exception e) {
            log.error("Error filtering text content for session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to filter text content.");
        }
    }

    @PostMapping("/video/{sessionId}")
    public ResponseEntity<?> filterVideoContent(
            @PathVariable String sessionId,
            @RequestBody(required = false) String base64Image) {
        try {
            if (base64Image == null || base64Image.isEmpty()) {
                log.warn("Base64 image data is missing or empty.");
                return ResponseEntity.badRequest().body("Base64 image data is required.");
            }

            log.debug("Filtering video content for session: {}", sessionId);
            boolean inappropriate = filterService.filterVideoContent(sessionId, base64Image);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid input for filtering video: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid session ID or video input.");
        } catch (Exception e) {
            log.error("Error filtering video content for session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to filter video content.");
        }
    }

}
