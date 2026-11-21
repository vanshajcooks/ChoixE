package com.campusconnect.interview.entity;

public enum InterviewMode {
    VIRTUAL("Virtual / Online", "bi-camera-video", "bg-info"),
    IN_PERSON("In-Person / On-Campus", "bi-geo-alt", "bg-primary"),
    TELEPHONIC("Telephonic", "bi-telephone", "bg-secondary");

    private final String displayName;
    private final String iconClass;
    private final String badgeClass;

    InterviewMode(String displayName, String iconClass, String badgeClass) {
        this.displayName = displayName;
        this.iconClass = iconClass;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconClass() {
        return iconClass;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
