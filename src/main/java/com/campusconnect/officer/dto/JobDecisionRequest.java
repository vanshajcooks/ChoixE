package com.campusconnect.officer.dto;

import jakarta.validation.constraints.Size;

public class JobDecisionRequest {

    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    private String reason;

    public JobDecisionRequest() {
    }

    public JobDecisionRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
