package com.campusconnect.notification.entity;

public enum NotificationType {
    INTERVIEW_SCHEDULED("Interview Scheduled", "bi-calendar-event text-primary"),
    INTERVIEW_UPDATED("Interview Updated", "bi-calendar-check text-warning"),
    INTERVIEW_CANCELLED("Interview Cancelled", "bi-calendar-x text-danger"),
    APPLICATION_STATUS_CHANGED("Application Status Updated", "bi-check2-circle text-info"),
    GENERAL("Notification", "bi-bell text-secondary");

    private final String displayName;
    private final String iconClass;

    NotificationType(String displayName, String iconClass) {
        this.displayName = displayName;
        this.iconClass = iconClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconClass() {
        return iconClass;
    }
}
