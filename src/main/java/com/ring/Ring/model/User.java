package com.ring.Ring.model;


import com.ring.Ring.model.enums.UserLocation;
import com.ring.Ring.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private UserLocation location;
    private UserStatus status;
    private LocalDateTime lastActiveTime;
    private boolean inSession;
}
