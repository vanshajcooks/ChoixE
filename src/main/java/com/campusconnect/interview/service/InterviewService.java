package com.campusconnect.interview.service;

import com.campusconnect.application.entity.Application;
import com.campusconnect.application.entity.ApplicationStatus;
import com.campusconnect.application.repository.ApplicationRepository;
import com.campusconnect.application.service.ApplicationService;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.repository.CompanyRepository;
import com.campusconnect.interview.dto.InterviewScheduleRequest;
import com.campusconnect.interview.dto.InterviewUpdateRequest;
import com.campusconnect.interview.entity.Interview;
import com.campusconnect.interview.entity.InterviewStatus;
import com.campusconnect.interview.repository.InterviewRepository;
import com.campusconnect.notification.entity.NotificationType;
import com.campusconnect.notification.service.NotificationService;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.repository.StudentProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;
    private final NotificationService notificationService;
    private final CompanyRepository companyRepository;
    private final StudentProfileRepository studentProfileRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public InterviewService(InterviewRepository interviewRepository,
                            ApplicationRepository applicationRepository,
                            ApplicationService applicationService,
                            NotificationService notificationService,
                            CompanyRepository companyRepository,
                            StudentProfileRepository studentProfileRepository) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.applicationService = applicationService;
        this.notificationService = notificationService;
        this.companyRepository = companyRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    // ==================== Recruiter Operations ====================

    @Transactional
    public Interview scheduleInterview(String recruiterEmail, Long applicationId, InterviewScheduleRequest request) {
        Application application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        if (!application.getJob().getCompany().getRecruiter().getEmail().equalsIgnoreCase(recruiterEmail)) {
            throw new IllegalArgumentException("You are not authorized to schedule interviews for this applicant.");
        }

        if (application.getStatus().isTerminal()) {
            throw new IllegalStateException("Cannot schedule interview for an application in terminal status: " + application.getStatus().getDisplayName());
        }

        if (request.isAdvanceApplicationStatus() && application.getStatus().canTransitionTo(ApplicationStatus.INTERVIEW_SCHEDULED)) {
            applicationService.updateApplicationStatus(
                    recruiterEmail,
                    applicationId,
                    ApplicationStatus.INTERVIEW_SCHEDULED,
                    "Interview scheduled: " + request.getRoundName()
            );
        }

        Interview interview = new Interview(
                application,
                request.getRoundName().trim(),
                request.getScheduledAt(),
                request.getMode(),
                request.getLocationOrLink().trim(),
                request.getInstructions() != null ? request.getInstructions().trim() : null
        );

        Interview saved = interviewRepository.save(interview);

        String message = String.format("An interview for %s at %s (%s) has been scheduled for %s. Venue/Link: %s",
                application.getJob().getTitle(),
                application.getJob().getCompany().getCompanyName(),
                request.getRoundName(),
                request.getScheduledAt().format(DATE_TIME_FORMATTER),
                request.getLocationOrLink());

        notificationService.createNotification(
                application.getStudent().getUser(),
                "Interview Scheduled: " + request.getRoundName(),
                message,
                NotificationType.INTERVIEW_SCHEDULED,
                "/student/interviews"
        );

        return saved;
    }

    @Transactional
    public Interview updateInterview(String recruiterEmail, Long interviewId, InterviewUpdateRequest request) {
        Interview interview = interviewRepository.findByIdWithDetails(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found: " + interviewId));

        Application application = interview.getApplication();
        if (!application.getJob().getCompany().getRecruiter().getEmail().equalsIgnoreCase(recruiterEmail)) {
            throw new IllegalArgumentException("You are not authorized to update this interview schedule.");
        }

        boolean timeChanged = !interview.getScheduledAt().isEqual(request.getScheduledAt());
        boolean statusChanged = interview.getStatus() != request.getStatus();

        interview.setRoundName(request.getRoundName().trim());
        interview.setScheduledAt(request.getScheduledAt());
        interview.setMode(request.getMode());
        interview.setLocationOrLink(request.getLocationOrLink().trim());
        interview.setInstructions(request.getInstructions() != null ? request.getInstructions().trim() : null);
        interview.setFeedback(request.getFeedback() != null ? request.getFeedback().trim() : null);

        if (timeChanged && request.getStatus() == InterviewStatus.SCHEDULED) {
            interview.setStatus(InterviewStatus.RESCHEDULED);
        } else {
            interview.setStatus(request.getStatus());
        }

        Interview updated = interviewRepository.save(interview);

        if (request.getStatus() == InterviewStatus.CANCELLED) {
            notificationService.createNotification(
                    application.getStudent().getUser(),
                    "Interview Cancelled: " + request.getRoundName(),
                    String.format("Your interview for %s at %s scheduled for %s has been cancelled.",
                            application.getJob().getTitle(),
                            application.getJob().getCompany().getCompanyName(),
                            request.getScheduledAt().format(DATE_TIME_FORMATTER)),
                    NotificationType.INTERVIEW_CANCELLED,
                    "/student/interviews"
            );
        } else if (timeChanged || statusChanged) {
            notificationService.createNotification(
                    application.getStudent().getUser(),
                    "Interview Updated: " + request.getRoundName(),
                    String.format("Your interview for %s at %s has been updated to %s (%s). Venue/Link: %s",
                            application.getJob().getTitle(),
                            application.getJob().getCompany().getCompanyName(),
                            request.getScheduledAt().format(DATE_TIME_FORMATTER),
                            request.getMode().getDisplayName(),
                            request.getLocationOrLink()),
                    NotificationType.INTERVIEW_UPDATED,
                    "/student/interviews"
            );
        }

        return updated;
    }

    @Transactional(readOnly = true)
    public Page<Interview> getCompanyInterviews(String recruiterEmail, Long jobId, InterviewStatus status, Pageable pageable) {
        Company company = companyRepository.findByRecruiterEmail(recruiterEmail)
                .orElseThrow(() -> new IllegalArgumentException("Company not found for recruiter: " + recruiterEmail));
        return interviewRepository.findCompanyInterviews(company.getId(), jobId, status, pageable);
    }

    @Transactional(readOnly = true)
    public Interview getInterviewDetailForRecruiter(String recruiterEmail, Long interviewId) {
        Interview interview = interviewRepository.findByIdWithDetails(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found: " + interviewId));

        if (!interview.getApplication().getJob().getCompany().getRecruiter().getEmail().equalsIgnoreCase(recruiterEmail)) {
            throw new IllegalArgumentException("You are not authorized to view this interview.");
        }
        return interview;
    }

    @Transactional(readOnly = true)
    public long getUpcomingCompanyInterviewCount(String recruiterEmail) {
        try {
            Company company = companyRepository.findByRecruiterEmail(recruiterEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found for recruiter: " + recruiterEmail));
            return interviewRepository.countUpcomingCompanyInterviews(company.getId(), LocalDateTime.now());
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== Student Operations ====================

    @Transactional(readOnly = true)
    public Page<Interview> getStudentInterviews(String studentEmail, Pageable pageable) {
        StudentProfile student = studentProfileRepository.findByUserEmail(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found for: " + studentEmail));
        return interviewRepository.findByStudentId(student.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public List<Interview> getUpcomingStudentInterviews(String studentEmail) {
        try {
            StudentProfile student = studentProfileRepository.findByUserEmail(studentEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Student profile not found for: " + studentEmail));
            return interviewRepository.findUpcomingStudentInterviews(student.getId(), LocalDateTime.now());
        } catch (Exception e) {
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<Interview> getInterviewsForApplication(Long applicationId) {
        return interviewRepository.findByApplicationIdOrderByScheduledAtAsc(applicationId);
    }

    // ==================== Officer Operations ====================

    @Transactional(readOnly = true)
    public Page<Interview> getOfficerInterviews(Long companyId, Long jobId, InterviewStatus status, Pageable pageable) {
        return interviewRepository.findOfficerInterviews(companyId, jobId, status, pageable);
    }

    @Transactional(readOnly = true)
    public Interview getInterviewDetailForOfficer(Long interviewId) {
        return interviewRepository.findByIdWithDetails(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found: " + interviewId));
    }
}
