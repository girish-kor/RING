package com.ring.Ring.model;

import com.ring.Ring.model.enums.SessionStatus;
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
@Document(collection = "sessions")
public class Session {
    @Id
    private String id;
    private String userOneId;
    private String userTwoId;
    private SessionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
    private String endReason;
}
