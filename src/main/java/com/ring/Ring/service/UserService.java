package com.ring.Ring.service;

import com.ring.Ring.model.User;
import com.ring.Ring.model.enums.UserLocation;
import com.ring.Ring.model.enums.UserStatus;
import com.ring.Ring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public User createUser(UserLocation location) {
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .location(location)
                .status(UserStatus.ACTIVE)
                .lastActiveTime(LocalDateTime.now())
                .inSession(false)
                .build();

        return userRepository.save(user);
    }

    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getActiveUserById(String userId) {
        return userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE);
    }

    public List<User> findAvailableUsersByLocation(UserLocation location, String excludeUserId) {
        return userRepository.findAvailableUsersExcludingUser(location, UserStatus.ACTIVE, excludeUserId);
    }

    public User updateUserStatus(String userId, UserStatus status) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setStatus(status);
                    user.setLastActiveTime(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    public User updateUserSessionStatus(String userId, boolean inSession) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setInSession(inSession);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    public void cleanupInactiveUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        List<User> inactiveUsers = userRepository.findInactiveUsers(threshold);

        for (User user : inactiveUsers) {
            if (user.getStatus() == UserStatus.ACTIVE) {
                log.info("Marking user as inactive due to inactivity: {}", user.getId());
                user.setStatus(UserStatus.INACTIVE);
                userRepository.save(user);
            }
        }
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }
}