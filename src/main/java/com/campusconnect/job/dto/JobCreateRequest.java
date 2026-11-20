package com.campusconnect.job.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JobCreateRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 150, message = "Job title cannot exceed 150 characters")
    private String title;

    @NotBlank(message = "Role type is required")
    @Size(max = 50, message = "Role type cannot exceed 50 characters")
    private String roleType; // Full-Time, Internship, FTE + Internship

    @Size(max = 50, message = "Work mode cannot exceed 50 characters")
    private String workMode; // On-site, Remote, Hybrid

    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String location;

    @Size(max = 100, message = "Salary / package cannot exceed 100 characters")
    private String salaryPackage;

    @NotBlank(message = "Job description is required")
    @Size(max = 5000, message = "Job description cannot exceed 5000 characters")
    private String description;

    @NotNull(message = "Application opening date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate applicationOpenDate;

    @NotNull(message = "Application closing date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate applicationCloseDate;

    // Action button pressed: "SAVE_DRAFT" or "SUBMIT"
    private String action = "SUBMIT";

    // Eligibility Criteria Fields
    @DecimalMin(value = "0.00", message = "Minimum CGPA cannot be negative")
    @DecimalMax(value = "10.00", message = "Minimum CGPA cannot exceed 10.00")
    private BigDecimal minCgpa;

    @Min(value = 0, message = "Maximum backlogs cannot be negative")
    @Max(value = 50, message = "Maximum backlogs cannot exceed 50")
    private Integer maxBacklogs;

    @Min(value = 2020, message = "Graduation year must be 2020 or later")
    @Max(value = 2035, message = "Graduation year must be 2035 or earlier")
    private Integer graduationYear;

    private List<String> eligibleBranches = new ArrayList<>();

    public JobCreateRequest() {
        this.applicationOpenDate = LocalDate.now();
        this.applicationCloseDate = LocalDate.now().plusWeeks(2);
    }

    public boolean isDateRangeValid() {
        if (applicationOpenDate == null || applicationCloseDate == null) {
            return false;
        }
        return !applicationCloseDate.isBefore(applicationOpenDate);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getWorkMode() {
        return workMode;
    }

    public void setWorkMode(String workMode) {
        this.workMode = workMode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSalaryPackage() {
        return salaryPackage;
    }

    public void setSalaryPackage(String salaryPackage) {
        this.salaryPackage = salaryPackage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getApplicationOpenDate() {
        return applicationOpenDate;
    }

    public void setApplicationOpenDate(LocalDate applicationOpenDate) {
        this.applicationOpenDate = applicationOpenDate;
    }

    public LocalDate getApplicationCloseDate() {
        return applicationCloseDate;
    }

    public void setApplicationCloseDate(LocalDate applicationCloseDate) {
        this.applicationCloseDate = applicationCloseDate;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public BigDecimal getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(BigDecimal minCgpa) {
        this.minCgpa = minCgpa;
    }

    public Integer getMaxBacklogs() {
        return maxBacklogs;
    }

    public void setMaxBacklogs(Integer maxBacklogs) {
        this.maxBacklogs = maxBacklogs;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public List<String> getEligibleBranches() {
        return eligibleBranches;
    }

    public void setEligibleBranches(List<String> eligibleBranches) {
        this.eligibleBranches = eligibleBranches != null ? eligibleBranches : new ArrayList<>();
    }
}
