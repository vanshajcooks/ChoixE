package com.campusconnect.job.service;

import com.campusconnect.company.entity.Company;
import com.campusconnect.company.service.CompanyService;
import com.campusconnect.job.dto.JobCreateRequest;
import com.campusconnect.job.entity.Job;
import com.campusconnect.job.entity.JobEligibilityCriteria;
import com.campusconnect.job.entity.JobStatus;
import com.campusconnect.job.repository.JobRepository;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyService companyService;
    private final StudentService studentService;
    private final EligibilityService eligibilityService;

    public JobService(JobRepository jobRepository,
                      CompanyService companyService,
                      StudentService studentService,
                      EligibilityService eligibilityService) {
        this.jobRepository = jobRepository;
        this.companyService = companyService;
        this.studentService = studentService;
        this.eligibilityService = eligibilityService;
    }

    // ==================== Recruiter Operations ====================

    @Transactional(readOnly = true)
    public Page<Job> getCompanyJobs(String recruiterEmail, Pageable pageable) {
        Company company = companyService.getCompanyByRecruiterEmail(recruiterEmail);
        return jobRepository.findByCompanyId(company.getId(), pageable);
    }

    @Transactional
    public Job createJob(String recruiterEmail, JobCreateRequest request) {
        Company company = companyService.getCompanyByRecruiterEmail(recruiterEmail);

        if (!company.isApproved()) {
            throw new IllegalStateException("Only approved organizations can create job postings. Your status is: " + company.getApprovalStatus());
        }

        if (!request.isDateRangeValid()) {
            throw new IllegalArgumentException("Application closing date cannot be earlier than the opening date.");
        }

        Job job = new Job(
                company,
                request.getTitle().trim(),
                request.getRoleType().trim(),
                request.getWorkMode() != null ? request.getWorkMode().trim() : null,
                request.getLocation() != null ? request.getLocation().trim() : null,
                request.getSalaryPackage() != null ? request.getSalaryPackage().trim() : null,
                request.getDescription().trim(),
                request.getApplicationOpenDate(),
                request.getApplicationCloseDate()
        );

        if ("SAVE_DRAFT".equalsIgnoreCase(request.getAction())) {
            job.setStatus(JobStatus.DRAFT);
        } else {
            job.setStatus(JobStatus.PENDING_APPROVAL);
        }

        JobEligibilityCriteria criteria = new JobEligibilityCriteria(
                job,
                request.getMinCgpa(),
                request.getMaxBacklogs(),
                request.getGraduationYear(),
                request.getEligibleBranches() != null ? new HashSet<>(request.getEligibleBranches()) : new HashSet<>()
        );
        job.setEligibilityCriteria(criteria);

        return jobRepository.save(job);
    }

    @Transactional
    public Job updateJob(String recruiterEmail, Long jobId, JobCreateRequest request) {
        Job job = getJobById(jobId);

        if (!job.getCompany().getRecruiter().getEmail().equalsIgnoreCase(recruiterEmail)) {
            throw new IllegalArgumentException("You are not authorized to edit this job posting.");
        }

        if (!job.isEditable()) {
            throw new IllegalStateException("Job posting cannot be modified in status: " + job.getStatus());
        }

        if (!request.isDateRangeValid()) {
            throw new IllegalArgumentException("Application closing date cannot be earlier than the opening date.");
        }

        job.setTitle(request.getTitle().trim());
        job.setRoleType(request.getRoleType().trim());
        job.setWorkMode(request.getWorkMode() != null ? request.getWorkMode().trim() : null);
        job.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        job.setSalaryPackage(request.getSalaryPackage() != null ? request.getSalaryPackage().trim() : null);
        job.setDescription(request.getDescription().trim());
        job.setApplicationOpenDate(request.getApplicationOpenDate());
        job.setApplicationCloseDate(request.getApplicationCloseDate());

        if ("SAVE_DRAFT".equalsIgnoreCase(request.getAction())) {
            job.setStatus(JobStatus.DRAFT);
        } else {
            job.setStatus(JobStatus.PENDING_APPROVAL);
            job.setRejectionReason(null);
        }

        JobEligibilityCriteria criteria = job.getEligibilityCriteria();
        if (criteria == null) {
            criteria = new JobEligibilityCriteria();
            job.setEligibilityCriteria(criteria);
        }
        criteria.setMinCgpa(request.getMinCgpa());
        criteria.setMaxBacklogs(request.getMaxBacklogs());
        criteria.setGraduationYear(request.getGraduationYear());
        criteria.setEligibleBranches(request.getEligibleBranches() != null ? new HashSet<>(request.getEligibleBranches()) : new HashSet<>());

        return jobRepository.save(job);
    }

    @Transactional
    public void submitJobForApproval(String recruiterEmail, Long jobId) {
        Job job = getJobById(jobId);

        if (!job.getCompany().getRecruiter().getEmail().equalsIgnoreCase(recruiterEmail)) {
            throw new IllegalArgumentException("You are not authorized to manage this job posting.");
        }

        if (!job.getCompany().isApproved()) {
            throw new IllegalStateException("Company must be approved before jobs can be submitted.");
        }

        job.setStatus(JobStatus.PENDING_APPROVAL);
        job.setRejectionReason(null);
        jobRepository.save(job);
    }

    // ==================== Student Operations ====================

    @Transactional(readOnly = true)
    public Page<StudentJobItem> getStudentJobs(String studentEmail, String search, Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<Job> jobPage = jobRepository.findApprovedOpenJobs(today, search, pageable);

        StudentProfile student = null;
        try {
            student = studentService.getProfileByEmail(studentEmail);
        } catch (IllegalArgumentException ignored) {
        }

        final StudentProfile finalStudent = student;
        List<StudentJobItem> items = jobPage.getContent().stream()
                .map(job -> new StudentJobItem(job, eligibilityService.evaluate(finalStudent, job)))
                .toList();

        return new PageImpl<>(items, pageable, jobPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public StudentJobItem getStudentJobDetail(String studentEmail, Long jobId) {
        Job job = getJobById(jobId);

        if (!job.isApproved()) {
            throw new IllegalArgumentException("Job posting is not published or available for viewing.");
        }

        StudentProfile student = null;
        try {
            student = studentService.getProfileByEmail(studentEmail);
        } catch (IllegalArgumentException ignored) {
        }

        EligibilityResult result = eligibilityService.evaluate(student, job);
        return new StudentJobItem(job, result);
    }

    // ==================== Placement Officer Operations ====================

    @Transactional(readOnly = true)
    public Page<Job> getOfficerJobs(JobStatus status, String search, Pageable pageable) {
        return jobRepository.searchOfficerJobs(status, search, pageable);
    }

    @Transactional(readOnly = true)
    public long getPendingJobCount() {
        return jobRepository.countByStatus(JobStatus.PENDING_APPROVAL);
    }

    @Transactional
    public void approveJob(Long id, String officerEmail) {
        Job job = getJobById(id);
        job.setStatus(JobStatus.APPROVED);
        job.setDecisionAt(LocalDateTime.now());
        job.setDecisionBy(officerEmail);
        job.setRejectionReason(null);
        jobRepository.save(job);
    }

    @Transactional
    public void rejectJob(Long id, String reason, String officerEmail) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A rejection reason must be provided.");
        }
        Job job = getJobById(id);
        job.setStatus(JobStatus.REJECTED);
        job.setRejectionReason(reason.trim());
        job.setDecisionAt(LocalDateTime.now());
        job.setDecisionBy(officerEmail);
        jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Job getJobById(Long id) {
        return jobRepository.findByIdWithCompanyAndCriteria(id)
                .orElseThrow(() -> new IllegalArgumentException("Job posting not found with ID: " + id));
    }

    public record StudentJobItem(Job job, EligibilityResult eligibility) {}
}
