package com.campusconnect.application.dto;

import com.campusconnect.application.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ApplicationStatusUpdateRequest {

    @NotNull(message = "New application status is required")
    private ApplicationStatus status;

    @Size(max = 500, message = "Status update remarks cannot exceed 500 characters")
    private String comments;

    public ApplicationStatusUpdateRequest() {
    }

    public ApplicationStatusUpdateRequest(ApplicationStatus status, String comments) {
        this.status = status;
        this.comments = comments;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
