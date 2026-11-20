package com.campusconnect.application.exception;

import com.campusconnect.application.entity.ApplicationStatus;

public class ApplicationStatusTransitionException extends RuntimeException {

    private final ApplicationStatus currentStatus;
    private final ApplicationStatus attemptedStatus;

    public ApplicationStatusTransitionException(ApplicationStatus currentStatus, ApplicationStatus attemptedStatus) {
        super(String.format("Cannot transition application from %s to %s. Permitted next states are: %s",
                currentStatus, attemptedStatus, currentStatus.getAllowedNextStatuses()));
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }

    public ApplicationStatus getCurrentStatus() {
        return currentStatus;
    }

    public ApplicationStatus getAttemptedStatus() {
        return attemptedStatus;
    }
}
