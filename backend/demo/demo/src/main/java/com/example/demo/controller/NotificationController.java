// controller/NotificationController.java
package com.example.demo.controller;

import com.example.demo.dto.NotificationResponse;
import com.example.demo.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getAll(Authentication auth) {
        return notificationService.getAllForUser(auth.getName());
    }

    @GetMapping("/unread")
    public List<NotificationResponse> getUnread(Authentication auth) {
        return notificationService.getUnreadForUser(auth.getName());
    }

    @PostMapping("/{id}/read")
    public void markRead(Authentication auth, @PathVariable Long id) {
        notificationService.markAsRead(auth.getName(), id);
    }

    @PostMapping("/read-all")
    public void markAllRead(Authentication auth) {
        notificationService.markAllAsRead(auth.getName());
    }
}