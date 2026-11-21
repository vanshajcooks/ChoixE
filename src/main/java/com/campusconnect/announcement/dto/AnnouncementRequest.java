package com.campusconnect.announcement.dto;

import com.campusconnect.announcement.entity.AnnouncementPriority;
import com.campusconnect.announcement.entity.TargetAudience;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class AnnouncementRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Target audience is required")
    private TargetAudience targetAudience = TargetAudience.ALL;

    @NotNull(message = "Priority is required")
    private AnnouncementPriority priority = AnnouncementPriority.NORMAL;

    private boolean pinned = false;

    private boolean active = true;

    private boolean broadcastNotification = false;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime expiresAt;

    public AnnouncementRequest() {
    }

    public AnnouncementRequest(String title, String content, TargetAudience targetAudience,
                               AnnouncementPriority priority, boolean pinned, boolean active,
                               boolean broadcastNotification, LocalDateTime expiresAt) {
        this.title = title;
        this.content = content;
        this.targetAudience = targetAudience;
        this.priority = priority;
        this.pinned = pinned;
        this.active = active;
        this.broadcastNotification = broadcastNotification;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TargetAudience getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(TargetAudience targetAudience) {
        this.targetAudience = targetAudience;
    }

    public AnnouncementPriority getPriority() {
        return priority;
    }

    public void setPriority(AnnouncementPriority priority) {
        this.priority = priority;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isBroadcastNotification() {
        return broadcastNotification;
    }

    public void setBroadcastNotification(boolean broadcastNotification) {
        this.broadcastNotification = broadcastNotification;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
