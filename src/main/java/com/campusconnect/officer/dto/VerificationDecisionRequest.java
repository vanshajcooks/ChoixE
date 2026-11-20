package com.campusconnect.officer.dto;

import jakarta.validation.constraints.Size;

public class VerificationDecisionRequest {

    @Size(max = 255, message = "Rejection reason cannot exceed 255 characters")
    private String reason;

    public VerificationDecisionRequest() {
    }

    public VerificationDecisionRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
