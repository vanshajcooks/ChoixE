package com.campusconnect.job.service;

import com.campusconnect.job.entity.Job;
import com.campusconnect.job.entity.JobEligibilityCriteria;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityServiceTest {

    private EligibilityService eligibilityService;

    @BeforeEach
    void setUp() {
        eligibilityService = new EligibilityService();
    }

    private StudentProfile createVerifiedStudent(BigDecimal cgpa, int backlogs, int gradYear, String department) {
        StudentProfile student = new StudentProfile();
        student.setCgpa(cgpa);
        student.setActiveBacklogs(backlogs);
        student.setGraduationYear(gradYear);
        student.setDepartment(department);
        student.setVerificationStatus(VerificationStatus.VERIFIED);
        return student;
    }

    private Job createJobWithCriteria(BigDecimal minCgpa, Integer maxBacklogs, Integer gradYear, Set<String> branches) {
        Job job = new Job();
        JobEligibilityCriteria criteria = new JobEligibilityCriteria();
        criteria.setMinCgpa(minCgpa);
        criteria.setMaxBacklogs(maxBacklogs);
        criteria.setGraduationYear(gradYear);
        criteria.setEligibleBranches(branches);
        job.setEligibilityCriteria(criteria);
        return job;
    }

    @Test
    void studentMeetsAllCriteria_IsEligible() {
        StudentProfile student = createVerifiedStudent(new BigDecimal("8.50"), 0, 2026, "Computer Science");
        Job job = createJobWithCriteria(new BigDecimal("7.50"), 0, 2026, Set.of("Computer Science", "Information Technology"));

        EligibilityResult result = eligibilityService.evaluate(student, job);

        assertThat(result.isEligible()).isTrue();
        assertThat(result.getUnmetCriteria()).isEmpty();
        assertThat(result.isProfileVerified()).isTrue();
    }

    @Test
    void unverifiedStudent_IsIneligible() {
        StudentProfile student = createVerifiedStudent(new BigDecimal("8.50"), 0, 2026, "Computer Science");
        student.setVerificationStatus(VerificationStatus.PENDING);

        Job job = createJobWithCriteria(new BigDecimal("7.50"), 0, 2026, Set.of("Computer Science"));

        EligibilityResult result = eligibilityService.evaluate(student, job);

        assertThat(result.isEligible()).isFalse();
        assertThat(result.isProfileVerified()).isFalse();
        assertThat(result.getUnmetCriteria()).anyMatch(r -> r.contains("requires Placement Officer verification"));
    }

    @Test
    void studentCgpaBelowCutoff_IsIneligible() {
        StudentProfile student = createVerifiedStudent(new BigDecimal("6.50"), 0, 2026, "Computer Science");
        Job job = createJobWithCriteria(new BigDecimal("7.50"), 0, 2026, Set.of("Computer Science"));

        EligibilityResult result = eligibilityService.evaluate(student, job);

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getUnmetCriteria()).anyMatch(r -> r.contains("below the minimum required CGPA"));
    }

    @Test
    void studentBacklogsExceedLimit_IsIneligible() {
        StudentProfile student = createVerifiedStudent(new BigDecimal("8.50"), 2, 2026, "Computer Science");
        Job job = createJobWithCriteria(new BigDecimal("7.50"), 0, 2026, Set.of("Computer Science"));

        EligibilityResult result = eligibilityService.evaluate(student, job);

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getUnmetCriteria()).anyMatch(r -> r.contains("exceeds the maximum allowed"));
    }

    @Test
    void studentGraduationYearMismatch_IsIneligible() {
        StudentProfile student = createVerifiedStudent(new BigDecimal("8.50"), 0, 2025, "Computer Science");
        Job job = createJobWithCriteria(new BigDecimal("7.50"), 0, 2026, Set.of("Computer Science"));

        EligibilityResult result = eligibilityService.evaluate(student, job);

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getUnmetCriteria()).anyMatch(r -> r.contains("does not match the required batch"));
    }

    @Test
    void studentBranchNotEligible_IsIneligible() {
        StudentProfile student = createVerifiedStudent(new BigDecimal("8.50"), 0, 2026, "Mechanical Engineering");
        Job job = createJobWithCriteria(new BigDecimal("7.50"), 0, 2026, Set.of("Computer Science", "Information Technology"));

        EligibilityResult result = eligibilityService.evaluate(student, job);

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getUnmetCriteria()).anyMatch(r -> r.contains("is not among the eligible branches"));
    }

    @Test
    void branchMatching_IsCaseInsensitive() {
        StudentProfile student = createVerifiedStudent(new BigDecimal("8.50"), 0, 2026, "computer science");
        Job job = createJobWithCriteria(new BigDecimal("7.50"), 0, 2026, Set.of("Computer Science"));

        EligibilityResult result = eligibilityService.evaluate(student, job);

        assertThat(result.isEligible()).isTrue();
    }

    @Test
    void multipleCriteriaFailed_ReturnsAllReasons() {
        StudentProfile student = createVerifiedStudent(new BigDecimal("6.00"), 3, 2025, "Civil Engineering");
        student.setVerificationStatus(VerificationStatus.REJECTED);

        Job job = createJobWithCriteria(new BigDecimal("7.50"), 0, 2026, Set.of("Computer Science"));

        EligibilityResult result = eligibilityService.evaluate(student, job);

        assertThat(result.isEligible()).isFalse();
        // Should contain 5 failures: unverified, CGPA, backlogs, batch year, branch
        assertThat(result.getUnmetCriteria()).hasSize(5);
    }

    @Test
    void noCriteriaConfigured_VerifiedStudentIsEligible() {
        StudentProfile student = createVerifiedStudent(new BigDecimal("6.00"), 1, 2024, "Civil Engineering");
        Job job = new Job();
        job.setEligibilityCriteria(null);

        EligibilityResult result = eligibilityService.evaluate(student, job);

        assertThat(result.isEligible()).isTrue();
        assertThat(result.getUnmetCriteria()).isEmpty();
    }

    @Test
    void nullStudent_ReturnsIneligible() {
        Job job = new Job();
        EligibilityResult result = eligibilityService.evaluate(null, job);

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getUnmetCriteria()).contains("Student academic profile does not exist.");
    }
}
