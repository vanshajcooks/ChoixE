package com.campusconnect.announcement.entity;

public enum AnnouncementPriority {
    NORMAL("Normal", "secondary"),
    HIGH("High", "warning"),
    URGENT("Urgent", "danger");

    private final String displayName;
    private final String badgeColor;

    AnnouncementPriority(String displayName, String badgeColor) {
        this.displayName = displayName;
        this.badgeColor = badgeColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeColor() {
        return badgeColor;
    }
}
