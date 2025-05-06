package com.ring.Ring.repository;

import com.ring.Ring.model.Session;
import com.ring.Ring.model.enums.SessionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends MongoRepository<Session, String> {
    List<Session> findByStatus(SessionStatus status);

    @Query("{'$or': [{'userOneId': ?0}, {'userTwoId': ?0}], 'status': ?1}")
    Optional<Session> findActiveSessionByUserId(String userId, SessionStatus status);

    @Query("{'createdAt': {'$lt': ?0}, 'status': ?1}")
    List<Session> findOldActiveSessions(LocalDateTime threshold, SessionStatus status);
}
