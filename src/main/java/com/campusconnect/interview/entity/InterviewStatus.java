package com.campusconnect.interview.entity;

public enum InterviewStatus {
    SCHEDULED("Scheduled", "bg-primary"),
    RESCHEDULED("Rescheduled", "bg-warning text-dark"),
    COMPLETED("Completed", "bg-success"),
    CANCELLED("Cancelled", "bg-danger");

    private final String displayName;
    private final String badgeClass;

    InterviewStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
