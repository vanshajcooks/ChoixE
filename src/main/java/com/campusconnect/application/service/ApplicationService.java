package com.campusconnect.application.service;

import com.campusconnect.application.entity.Application;
import com.campusconnect.application.entity.ApplicationStatus;
import com.campusconnect.application.entity.ApplicationStatusHistory;
import com.campusconnect.application.exception.ApplicationStatusTransitionException;
import com.campusconnect.application.repository.ApplicationRepository;
import com.campusconnect.application.repository.ApplicationStatusHistoryRepository;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.service.CompanyService;
import com.campusconnect.job.entity.Job;
import com.campusconnect.job.service.EligibilityResult;
import com.campusconnect.job.service.EligibilityService;
import com.campusconnect.job.service.JobService;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import com.campusconnect.student.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final JobService jobService;
    private final StudentService studentService;
    private final EligibilityService eligibilityService;
    private final CompanyService companyService;
    private final com.campusconnect.notification.service.NotificationService notificationService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ApplicationStatusHistoryRepository historyRepository,
                              JobService jobService,
                              StudentService studentService,
                              EligibilityService eligibilityService,
                              CompanyService companyService,
                              com.campusconnect.notification.service.NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.jobService = jobService;
        this.studentService = studentService;
        this.eligibilityService = eligibilityService;
        this.companyService = companyService;
        this.notificationService = notificationService;
    }

    // ==================== Student Operations ====================

    @Transactional
    public Application applyToJob(String studentEmail, Long jobId) {
        StudentProfile student = studentService.getProfileByEmail(studentEmail);

        if (student.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new IllegalStateException("Your profile must be verified by the Placement Officer before you can apply for jobs (Current status: "
                    + student.getVerificationStatus() + ").");
        }

        if (!student.hasResume()) {
            throw new IllegalStateException("You must upload a resume to your academic profile before submitting job applications.");
        }

        Job job = jobService.getJobById(jobId);

        if (!job.isApproved()) {
            throw new IllegalStateException("This job posting is not open for applications (Status: " + job.getStatus() + ").");
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(job.getApplicationOpenDate())) {
            throw new IllegalStateException("Applications for this job posting have not opened yet (Opening date: "
                    + job.getApplicationOpenDate() + ").");
        }
        if (today.isAfter(job.getApplicationCloseDate())) {
            throw new IllegalStateException("The application deadline for this job posting has passed (Closing date: "
                    + job.getApplicationCloseDate() + ").");
        }

        if (applicationRepository.existsByStudentIdAndJobId(student.getId(), job.getId())) {
            throw new IllegalStateException("You have already submitted an application for this job posting.");
        }

        EligibilityResult eligibility = eligibilityService.evaluate(student, job);
        if (!eligibility.isEligible()) {
            throw new IllegalStateException("You do not satisfy the eligibility requirements for this job: "
                    + String.join("; ", eligibility.getUnmetCriteria()));
        }

        Application application = new Application(student, job);
        Application saved = applicationRepository.save(application);

        ApplicationStatusHistory initialHistory = new ApplicationStatusHistory(
                saved,
                null,
                ApplicationStatus.APPLIED,
                studentEmail,
                "Application submitted by student."
        );
        historyRepository.save(initialHistory);

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Application> getStudentApplications(String studentEmail, Pageable pageable) {
        StudentProfile student = studentService.getProfileByEmail(studentEmail);
        return applicationRepository.findByStudentId(student.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Application getStudentApplicationDetail(String studentEmail, Long applicationId) {
        Application application = getApplicationById(applicationId);
        if (!application.getStudent().getUser().getEmail().equalsIgnoreCase(studentEmail)) {
            throw new IllegalArgumentException("You are not authorized to view this application.");
        }
        return application;
    }

    @Transactional(readOnly = true)
    public boolean hasStudentApplied(String studentEmail, Long jobId) {
        try {
            StudentProfile student = studentService.getProfileByEmail(studentEmail);
            return applicationRepository.existsByStudentIdAndJobId(student.getId(), jobId);
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public Optional<Application> getStudentApplicationForJob(String studentEmail, Long jobId) {
        try {
            StudentProfile student = studentService.getProfileByEmail(studentEmail);
            return applicationRepository.findByStudentIdAndJobId(student.getId(), jobId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void withdrawApplication(String studentEmail, Long applicationId) {
        Application application = getApplicationById(applicationId);

        if (!application.getStudent().getUser().getEmail().equalsIgnoreCase(studentEmail)) {
            throw new IllegalArgumentException("You are not authorized to withdraw this application.");
        }

        if (!application.canBeWithdrawn()) {
            throw new IllegalStateException("Application in status '" + application.getStatus().getDisplayName()
                    + "' cannot be withdrawn.");
        }

        ApplicationStatus previousStatus = application.getStatus();
        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);

        ApplicationStatusHistory history = new ApplicationStatusHistory(
                application,
                previousStatus,
                ApplicationStatus.WITHDRAWN,
                studentEmail,
                "Application withdrawn by student."
        );
        historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public long getStudentApplicationCount(String studentEmail) {
        try {
            StudentProfile student = studentService.getProfileByEmail(studentEmail);
            return applicationRepository.countByStudentId(student.getId());
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== Recruiter Operations ====================

    @Transactional(readOnly = true)
    public Page<Application> getJobApplicants(String recruiterEmail, Long jobId, ApplicationStatus status, String search, Pageable pageable) {
        Job job = jobService.getJobById(jobId);
        if (!job.getCompany().getRecruiter().getEmail().equalsIgnoreCase(recruiterEmail)) {
            throw new IllegalArgumentException("You are not authorized to view applicants for this job posting.");
        }
        return applicationRepository.searchCompanyApplicants(job.getCompany().getId(), jobId, status, search, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Application> getCompanyApplications(String recruiterEmail, Long jobId, ApplicationStatus status, String search, Pageable pageable) {
        Company company = companyService.getCompanyByRecruiterEmail(recruiterEmail);
        return applicationRepository.searchCompanyApplicants(company.getId(), jobId, status, search, pageable);
    }

    @Transactional(readOnly = true)
    public Application getApplicationDetailForRecruiter(String recruiterEmail, Long applicationId) {
        Application application = getApplicationById(applicationId);
        if (!application.getJob().getCompany().getRecruiter().getEmail().equalsIgnoreCase(recruiterEmail)) {
            throw new IllegalArgumentException("You are not authorized to view this applicant.");
        }
        return application;
    }

    @Transactional
    public void updateApplicationStatus(String recruiterEmail, Long applicationId, ApplicationStatus newStatus, String comments) {
        Application application = getApplicationById(applicationId);

        if (!application.getJob().getCompany().getRecruiter().getEmail().equalsIgnoreCase(recruiterEmail)) {
            throw new IllegalArgumentException("You are not authorized to update this application.");
        }

        ApplicationStatus currentStatus = application.getStatus();
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new ApplicationStatusTransitionException(currentStatus, newStatus);
        }

        application.setStatus(newStatus);
        applicationRepository.save(application);

        ApplicationStatusHistory history = new ApplicationStatusHistory(
                application,
                currentStatus,
                newStatus,
                recruiterEmail,
                comments != null && !comments.isBlank() ? comments.trim() : null
        );
        historyRepository.save(history);

        notificationService.createNotification(
                application.getStudent().getUser(),
                "Application Status Updated: " + application.getJob().getTitle(),
                String.format("Your application for %s at %s has been updated to %s.",
                        application.getJob().getTitle(),
                        application.getJob().getCompany().getCompanyName(),
                        newStatus.getDisplayName()),
                com.campusconnect.notification.entity.NotificationType.APPLICATION_STATUS_CHANGED,
                "/student/applications/" + application.getId()
        );
    }

    @Transactional(readOnly = true)
    public long getCompanyApplicantCount(String recruiterEmail) {
        try {
            Company company = companyService.getCompanyByRecruiterEmail(recruiterEmail);
            return applicationRepository.countByJobCompanyId(company.getId());
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== Placement Officer Operations ====================

    @Transactional(readOnly = true)
    public Page<Application> getOfficerApplications(ApplicationStatus status, String search, Pageable pageable) {
        return applicationRepository.searchOfficerApplications(status, search, pageable);
    }

    @Transactional(readOnly = true)
    public Application getApplicationDetailForOfficer(Long applicationId) {
        return getApplicationById(applicationId);
    }

    // ==================== Shared Helper ====================

    @Transactional(readOnly = true)
    public Application getApplicationById(Long id) {
        Application application = applicationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + id));
        org.hibernate.Hibernate.initialize(application.getStatusHistory());
        return application;
    }
}
