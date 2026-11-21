package com.campusconnect.notification.service;

import com.campusconnect.auth.entity.User;
import com.campusconnect.auth.repository.UserRepository;
import com.campusconnect.notification.entity.Notification;
import com.campusconnect.notification.entity.NotificationType;
import com.campusconnect.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Notification createNotification(User recipient, String title, String message,
                                           NotificationType type, String linkUrl) {
        Notification notification = new Notification(recipient, title, message, type, linkUrl);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .map(user -> notificationRepository.countByRecipientIdAndReadFalse(user.getId()))
                .orElse(0L);
    }

    @Transactional
    public void markAsRead(String userEmail, Long notificationId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied to notification.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        notificationRepository.markAllAsReadByRecipientId(user.getId());
    }

    @Transactional(readOnly = true)
    public List<Notification> getTop5Unread(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .map(user -> notificationRepository.findTop5UnreadByRecipientId(user.getId()))
                .orElse(List.of());
    }
}
