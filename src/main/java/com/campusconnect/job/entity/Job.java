package com.campusconnect.job.entity;

import com.campusconnect.company.entity.Company;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 50)
    private String roleType; // e.g., Full-Time, Internship, FTE + Internship

    @Column(length = 50)
    private String workMode; // On-site, Remote, Hybrid

    @Column(length = 150)
    private String location;

    @Column(length = 100)
    private String salaryPackage; // e.g., 14 LPA, 35,000 / month

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate applicationOpenDate;

    @Column(nullable = false)
    private LocalDate applicationCloseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobStatus status = JobStatus.DRAFT;

    @Column(length = 500)
    private String rejectionReason;

    private LocalDateTime decisionAt;

    @Column(length = 100)
    private String decisionBy;

    @OneToOne(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private JobEligibilityCriteria eligibilityCriteria;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Job() {
    }

    public Job(Company company, String title, String roleType, String workMode, String location,
               String salaryPackage, String description, LocalDate applicationOpenDate, LocalDate applicationCloseDate) {
        this.company = company;
        this.title = title;
        this.roleType = roleType;
        this.workMode = workMode;
        this.location = location;
        this.salaryPackage = salaryPackage;
        this.description = description;
        this.applicationOpenDate = applicationOpenDate;
        this.applicationCloseDate = applicationCloseDate;
        this.status = JobStatus.DRAFT;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = JobStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isApproved() {
        return this.status == JobStatus.APPROVED;
    }

    public boolean isPendingApproval() {
        return this.status == JobStatus.PENDING_APPROVAL;
    }

    public boolean isDraft() {
        return this.status == JobStatus.DRAFT;
    }

    public boolean isRejected() {
        return this.status == JobStatus.REJECTED;
    }

    public boolean isClosed() {
        return this.status == JobStatus.CLOSED;
    }

    public boolean isOpenForApplication() {
        if (this.status != JobStatus.APPROVED) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !today.isBefore(applicationOpenDate) && !today.isAfter(applicationCloseDate);
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(applicationCloseDate);
    }

    public boolean isEditable() {
        return this.status == JobStatus.DRAFT || this.status == JobStatus.REJECTED || this.status == JobStatus.PENDING_APPROVAL;
    }

    public void setEligibilityCriteria(JobEligibilityCriteria criteria) {
        this.eligibilityCriteria = criteria;
        if (criteria != null) {
            criteria.setJob(this);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
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

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getDecisionAt() {
        return decisionAt;
    }

    public void setDecisionAt(LocalDateTime decisionAt) {
        this.decisionAt = decisionAt;
    }

    public String getDecisionBy() {
        return decisionBy;
    }

    public void setDecisionBy(String decisionBy) {
        this.decisionBy = decisionBy;
    }

    public JobEligibilityCriteria getEligibilityCriteria() {
        return eligibilityCriteria;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
