package com.campusconnect.application.entity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum ApplicationStatus {

    APPLIED("Applied", "badge bg-info text-dark"),
    UNDER_REVIEW("Under Review", "badge bg-primary"),
    SHORTLISTED("Shortlisted", "badge bg-warning text-dark"),
    INTERVIEW_SCHEDULED("Interview Scheduled", "badge bg-secondary"),
    SELECTED("Selected", "badge bg-success"),
    REJECTED("Rejected", "badge bg-danger"),
    NOT_SELECTED("Not Selected", "badge bg-dark"),
    WITHDRAWN("Withdrawn", "badge bg-light text-secondary border");

    private final String displayName;
    private final String badgeClass;

    ApplicationStatus(String displayName, String badgeClass) {
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
        return this == SELECTED || this == REJECTED || this == NOT_SELECTED || this == WITHDRAWN;
    }

    public Set<ApplicationStatus> getAllowedNextStatuses() {
        return switch (this) {
            case APPLIED -> EnumSet.of(UNDER_REVIEW, REJECTED, WITHDRAWN);
            case UNDER_REVIEW -> EnumSet.of(SHORTLISTED, REJECTED, WITHDRAWN);
            case SHORTLISTED -> EnumSet.of(INTERVIEW_SCHEDULED, REJECTED, WITHDRAWN);
            case INTERVIEW_SCHEDULED -> EnumSet.of(SELECTED, NOT_SELECTED, REJECTED, WITHDRAWN);
            case SELECTED, REJECTED, NOT_SELECTED, WITHDRAWN -> Collections.emptySet();
        };
    }

    public boolean canTransitionTo(ApplicationStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return getAllowedNextStatuses().contains(target);
    }
}
