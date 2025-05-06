package com.ring.Ring.controller;

import com.ring.Ring.model.User;
import com.ring.Ring.model.enums.UserLocation;
import com.ring.Ring.model.enums.UserStatus;
import com.ring.Ring.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestParam("location") UserLocation location) {
        try {
            log.info("Creating new user with location: {}", location);
            User user = userService.createUser(location);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            log.error("Invalid location: {}", location, e);
            return ResponseEntity.badRequest().body("Invalid location provided.");
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create user.");
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        try {
            return userService.getUserById(userId)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        log.warn("User not found with id: {}", userId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
                    });
        } catch (Exception e) {
            log.error("Error retrieving user with id: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving user.");
        }
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable String userId,
            @RequestParam("status") UserStatus status) {

        try {
            log.info("Updating user status: {} to {}", userId, status);
            User updatedUser = userService.updateUserStatus(userId, status);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            log.error("Invalid status update for user: {}", userId, e);
            return ResponseEntity.badRequest().body("Invalid status provided.");
        } catch (ResponseStatusException e) {
            log.warn("User not found: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (Exception e) {
            log.error("Error updating user status: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user status.");
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            log.info("Deleting user: {}", userId);
            userService.deleteUser(userId);
            return ResponseEntity.ok().body("User deleted successfully.");
        } catch (ResponseStatusException e) {
            log.warn("User not found for deletion: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (Exception e) {
            log.error("Error deleting user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete user.");
        }
    }
}
