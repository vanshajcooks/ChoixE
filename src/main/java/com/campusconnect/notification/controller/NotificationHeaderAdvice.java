package com.campusconnect.notification.controller;

import com.campusconnect.notification.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NotificationHeaderAdvice {

    private final NotificationService notificationService;

    public NotificationHeaderAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute("unreadNotificationCount")
    public long addUnreadNotificationCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null && userDetails.getUsername() != null) {
            try {
                return notificationService.getUnreadCount(userDetails.getUsername());
            } catch (Exception e) {
                return 0L;
            }
        }
        return 0L;
    }
}
