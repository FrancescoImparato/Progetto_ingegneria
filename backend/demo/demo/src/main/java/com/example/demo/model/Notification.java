// model/Notification.java
package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    private Long referenceId;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Notification() {}

    public Notification(User recipient, NotificationType type, String message, Long referenceId) {
        this.recipient = recipient;
        this.type = type;
        this.message = message;
        this.referenceId = referenceId;
    }

    public void markAsRead() {
        this.read = true;
    }

    public Long getId() { return id; }
    public User getRecipient() { return recipient; }
    public NotificationType getType() { return type; }
    public String getMessage() { return message; }
    public Long getReferenceId() { return referenceId; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
}