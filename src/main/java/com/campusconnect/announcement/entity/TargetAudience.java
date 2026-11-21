package com.campusconnect.announcement.entity;

public enum TargetAudience {
    ALL("All Users (Students & Recruiters)"),
    STUDENTS_ONLY("Students Only"),
    RECRUITERS_ONLY("Recruiters Only");

    private final String displayName;

    TargetAudience(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
