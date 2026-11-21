package com.campusconnect.announcement.entity;

import com.campusconnect.auth.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience", nullable = false, length = 30)
    private TargetAudience targetAudience = TargetAudience.ALL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnnouncementPriority priority = AnnouncementPriority.NORMAL;

    @Column(nullable = false)
    private boolean pinned = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "broadcast_notification", nullable = false)
    private boolean broadcastNotification = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public Announcement() {
    }

    public Announcement(String title, String content, TargetAudience targetAudience,
                        AnnouncementPriority priority, boolean pinned, boolean active,
                        boolean broadcastNotification, User author, LocalDateTime expiresAt) {
        this.title = title;
        this.content = content;
        this.targetAudience = targetAudience != null ? targetAudience : TargetAudience.ALL;
        this.priority = priority != null ? priority : AnnouncementPriority.NORMAL;
        this.pinned = pinned;
        this.active = active;
        this.broadcastNotification = broadcastNotification;
        this.author = author;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
