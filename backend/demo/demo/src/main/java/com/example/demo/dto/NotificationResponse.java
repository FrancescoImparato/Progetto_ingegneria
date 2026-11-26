// dto/NotificationResponse.java
package com.example.demo.dto;

import com.example.demo.model.Notification;
import com.example.demo.model.NotificationType;
import java.time.Instant;

public record NotificationResponse(
    Long id, NotificationType type, String message, Long referenceId, boolean read, Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
            n.getId(), n.getType(), n.getMessage(), n.getReferenceId(), n.isRead(), n.getCreatedAt()
        );
    }
}