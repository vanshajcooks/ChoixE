package com.campusconnect.interview.dto;

import com.campusconnect.interview.entity.InterviewMode;
import com.campusconnect.interview.entity.InterviewStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class InterviewUpdateRequest {

    @NotBlank(message = "Round name is required.")
    @Size(max = 100, message = "Round name must not exceed 100 characters.")
    private String roundName;

    @NotNull(message = "Interview date and time is required.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime scheduledAt;

    @NotNull(message = "Interview mode is required.")
    private InterviewMode mode;

    @NotBlank(message = "Meeting link or location venue is required.")
    @Size(max = 255, message = "Location or link must not exceed 255 characters.")
    private String locationOrLink;

    private String instructions;

    @NotNull(message = "Interview status is required.")
    private InterviewStatus status;

    private String feedback;

    public InterviewUpdateRequest() {
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public InterviewMode getMode() {
        return mode;
    }

    public void setMode(InterviewMode mode) {
        this.mode = mode;
    }

    public String getLocationOrLink() {
        return locationOrLink;
    }

    public void setLocationOrLink(String locationOrLink) {
        this.locationOrLink = locationOrLink;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public InterviewStatus getStatus() {
        return status;
    }

    public void setStatus(InterviewStatus status) {
        this.status = status;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
