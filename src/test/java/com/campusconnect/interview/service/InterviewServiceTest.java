package com.campusconnect.interview.service;

import com.campusconnect.application.entity.Application;
import com.campusconnect.application.entity.ApplicationStatus;
import com.campusconnect.application.repository.ApplicationRepository;
import com.campusconnect.application.service.ApplicationService;
import com.campusconnect.auth.entity.User;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.repository.CompanyRepository;
import com.campusconnect.interview.dto.InterviewScheduleRequest;
import com.campusconnect.interview.dto.InterviewUpdateRequest;
import com.campusconnect.interview.entity.Interview;
import com.campusconnect.interview.entity.InterviewMode;
import com.campusconnect.interview.entity.InterviewStatus;
import com.campusconnect.interview.repository.InterviewRepository;
import com.campusconnect.job.entity.Job;
import com.campusconnect.notification.entity.NotificationType;
import com.campusconnect.notification.service.NotificationService;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.repository.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @InjectMocks
    private InterviewService interviewService;

    private User recruiterUser;
    private User studentUser;
    private Company company;
    private Job job;
    private StudentProfile studentProfile;
    private Application application;
    private Interview mockInterview;

    @BeforeEach
    void setUp() {
        recruiterUser = new User();
        recruiterUser.setEmail("recruiter@acme.com");

        company = new Company();
        company.setId(1L);
        company.setCompanyName("Acme Corp");
        company.setRecruiter(recruiterUser);

        job = new Job();
        job.setId(10L);
        job.setTitle("Senior Java Developer");
        job.setCompany(company);

        studentUser = new User();
        studentUser.setEmail("jane@college.edu");
        studentUser.setFullName("Jane Doe");

        studentProfile = new StudentProfile();
        studentProfile.setId(20L);
        studentProfile.setUser(studentUser);
        studentProfile.setRollNumber("2022CS105");

        application = new Application(studentProfile, job);
        application.setId(100L);
        application.setStatus(ApplicationStatus.SHORTLISTED);

        mockInterview = new Interview(
                application,
                "Round 1: Technical Screening",
                LocalDateTime.now().plusDays(2),
                InterviewMode.VIRTUAL,
                "https://meet.google.com/abc-xyz",
                "Keep your IDE ready."
        );
        mockInterview.setId(500L);
    }

    @Test
    void scheduleInterview_ValidSubmission_CreatesInterviewAndNotifiesStudent() {
        InterviewScheduleRequest request = new InterviewScheduleRequest();
        request.setRoundName("Round 1: Technical Screening");
        request.setScheduledAt(LocalDateTime.now().plusDays(2));
        request.setMode(InterviewMode.VIRTUAL);
        request.setLocationOrLink("https://meet.google.com/abc-xyz");
        request.setInstructions("Keep your IDE ready.");
        request.setAdvanceApplicationStatus(true);

        when(applicationRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(application));
        when(interviewRepository.save(any(Interview.class))).thenReturn(mockInterview);

        Interview scheduled = interviewService.scheduleInterview("recruiter@acme.com", 100L, request);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.getRoundName()).isEqualTo("Round 1: Technical Screening");
        verify(applicationService, times(1)).updateApplicationStatus(
                eq("recruiter@acme.com"), eq(100L), eq(ApplicationStatus.INTERVIEW_SCHEDULED), any()
        );
        verify(notificationService, times(1)).createNotification(
                eq(studentUser),
                contains("Interview Scheduled"),
                any(),
                eq(NotificationType.INTERVIEW_SCHEDULED),
                eq("/student/interviews")
        );
    }

    @Test
    void scheduleInterview_WhenRecruiterDoesNotOwnCompany_ThrowsException() {
        InterviewScheduleRequest request = new InterviewScheduleRequest();
        when(applicationRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> interviewService.scheduleInterview("stranger@other.com", 100L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void scheduleInterview_WhenApplicationIsTerminal_ThrowsException() {
        application.setStatus(ApplicationStatus.REJECTED);
        InterviewScheduleRequest request = new InterviewScheduleRequest();

        when(applicationRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> interviewService.scheduleInterview("recruiter@acme.com", 100L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("terminal status");
    }

    @Test
    void updateInterview_WhenRescheduled_UpdatesStatusAndDispatchesNotification() {
        LocalDateTime newTime = LocalDateTime.now().plusDays(5);
        InterviewUpdateRequest request = new InterviewUpdateRequest();
        request.setRoundName("Round 1: Technical Screening");
        request.setScheduledAt(newTime);
        request.setMode(InterviewMode.VIRTUAL);
        request.setLocationOrLink("https://meet.google.com/new-link");
        request.setInstructions("Updated agenda");
        request.setStatus(InterviewStatus.SCHEDULED);

        when(interviewRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(mockInterview));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(i -> i.getArgument(0));

        Interview updated = interviewService.updateInterview("recruiter@acme.com", 500L, request);

        assertThat(updated.getStatus()).isEqualTo(InterviewStatus.RESCHEDULED);
        verify(notificationService, times(1)).createNotification(
                eq(studentUser),
                contains("Interview Updated"),
                any(),
                eq(NotificationType.INTERVIEW_UPDATED),
                eq("/student/interviews")
        );
    }

    @Test
    void updateInterview_WhenCancelled_DispatchesCancellationNotification() {
        InterviewUpdateRequest request = new InterviewUpdateRequest();
        request.setRoundName("Round 1: Technical Screening");
        request.setScheduledAt(mockInterview.getScheduledAt());
        request.setMode(InterviewMode.VIRTUAL);
        request.setLocationOrLink("https://meet.google.com/link");
        request.setStatus(InterviewStatus.CANCELLED);

        when(interviewRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(mockInterview));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(i -> i.getArgument(0));

        Interview updated = interviewService.updateInterview("recruiter@acme.com", 500L, request);

        assertThat(updated.getStatus()).isEqualTo(InterviewStatus.CANCELLED);
        verify(notificationService, times(1)).createNotification(
                eq(studentUser),
                contains("Interview Cancelled"),
                any(),
                eq(NotificationType.INTERVIEW_CANCELLED),
                eq("/student/interviews")
        );
    }

    @Test
    void getCompanyInterviews_ReturnsPage() {
        when(companyRepository.findByRecruiterEmail("recruiter@acme.com")).thenReturn(Optional.of(company));
        when(interviewRepository.findCompanyInterviews(eq(1L), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockInterview)));

        Page<Interview> page = interviewService.getCompanyInterviews("recruiter@acme.com", null, null, PageRequest.of(0, 10));

        assertThat(page).hasSize(1);
    }

    @Test
    void getStudentInterviews_ReturnsPage() {
        when(studentProfileRepository.findByUserEmail("jane@college.edu")).thenReturn(Optional.of(studentProfile));
        when(interviewRepository.findByStudentId(eq(20L), any()))
                .thenReturn(new PageImpl<>(List.of(mockInterview)));

        Page<Interview> page = interviewService.getStudentInterviews("jane@college.edu", PageRequest.of(0, 10));

        assertThat(page).hasSize(1);
    }
}
