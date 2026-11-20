package com.campusconnect.job.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "job_eligibility_criteria")
public class JobEligibilityCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false, unique = true)
    private Job job;

    @Column(precision = 4, scale = 2)
    private BigDecimal minCgpa;

    private Integer maxBacklogs;

    private Integer graduationYear;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_eligible_branches", joinColumns = @JoinColumn(name = "criteria_id"))
    @Column(name = "branch_name")
    private Set<String> eligibleBranches = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public JobEligibilityCriteria() {
    }

    public JobEligibilityCriteria(Job job, BigDecimal minCgpa, Integer maxBacklogs, Integer graduationYear, Set<String> eligibleBranches) {
        this.job = job;
        this.minCgpa = minCgpa;
        this.maxBacklogs = maxBacklogs;
        this.graduationYear = graduationYear;
        if (eligibleBranches != null) {
            this.eligibleBranches = new HashSet<>(eligibleBranches);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isBranchEligible(String branch) {
        if (eligibleBranches == null || eligibleBranches.isEmpty()) {
            return true; // No branch restrictions configured
        }
        if (branch == null || branch.isBlank()) {
            return false;
        }
        return eligibleBranches.stream().anyMatch(b -> b.equalsIgnoreCase(branch.trim()));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
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

    public Set<String> getEligibleBranches() {
        return eligibleBranches;
    }

    public void setEligibleBranches(Set<String> eligibleBranches) {
        this.eligibleBranches = eligibleBranches != null ? eligibleBranches : new HashSet<>();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
