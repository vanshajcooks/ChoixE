package com.campusconnect.notification.controller;

import com.campusconnect.notification.entity.Notification;
import com.campusconnect.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String viewNotifications(@RequestParam(name = "page", defaultValue = "0") int page,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {
        Page<Notification> notifications = notificationService.getUserNotifications(
                userDetails.getUsername(),
                PageRequest.of(page, 15)
        );

        model.addAttribute("notifications", notifications);
        model.addAttribute("username", userDetails.getUsername());
        return "notifications/list";
    }

    @PostMapping("/{id}/read")
    public String markNotificationAsRead(@PathVariable("id") Long id,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         @RequestHeader(value = "Referer", required = false) String referer) {
        try {
            notificationService.markAsRead(userDetails.getUsername(), id);
        } catch (Exception ignored) {
        }
        return referer != null && !referer.isBlank() ? "redirect:" + referer : "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllAsRead(@AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        notificationService.markAllAsRead(userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read.");
        return "redirect:/notifications";
    }
}
