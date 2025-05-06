package com.ring.Ring.repository;

import com.ring.Ring.model.User;
import com.ring.Ring.model.enums.UserLocation;
import com.ring.Ring.model.enums.UserStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByLocationAndStatusAndInSessionFalse(UserLocation location, UserStatus status);

    @Query("{'location': ?0, 'status': ?1, 'inSession': false, '_id': {'$ne': ?2}}")
    List<User> findAvailableUsersExcludingUser(UserLocation location, UserStatus status, String userId);

    @Query("{'lastActiveTime': {'$lt': ?0}}")
    List<User> findInactiveUsers(LocalDateTime threshold);

    Optional<User> findByIdAndStatus(String id, UserStatus status);
}
