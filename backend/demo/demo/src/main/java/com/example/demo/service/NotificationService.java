// service/NotificationService.java
package com.example.demo.service;

import com.example.demo.dto.NotificationResponse;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.*;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void notify(User recipient, NotificationType type, String message, Long referenceId) {
        notificationRepository.save(new Notification(recipient, type, message, referenceId));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllForUser(String userEmail) {
        User user = findUser(userEmail);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId())
            .stream().map(NotificationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadForUser(String userEmail) {
        User user = findUser(userEmail);
        return notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(user.getId())
            .stream().map(NotificationResponse::from).toList();
    }

    @Transactional
    public void markAsRead(String userEmail, Long notificationId) {
        User user = findUser(userEmail);
        Notification n = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new BusinessException("Notifica non trovata", HttpStatus.NOT_FOUND));
        if (!n.getRecipient().getId().equals(user.getId())) {
            throw new BusinessException("Non autorizzato", HttpStatus.FORBIDDEN);
        }
        n.markAsRead();
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = findUser(userEmail);
        List<Notification> unread = notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(user.getId());
        unread.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unread);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("Utente non trovato", HttpStatus.NOT_FOUND));
    }
}