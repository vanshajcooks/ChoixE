package com.campusconnect.application.service;

import com.campusconnect.application.entity.Application;
import com.campusconnect.application.entity.ApplicationStatus;
import com.campusconnect.application.exception.ApplicationStatusTransitionException;
import com.campusconnect.application.repository.ApplicationRepository;
import com.campusconnect.application.repository.ApplicationStatusHistoryRepository;
import com.campusconnect.auth.entity.User;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.service.CompanyService;
import com.campusconnect.job.entity.Job;
import com.campusconnect.job.entity.JobStatus;
import com.campusconnect.job.service.EligibilityResult;
import com.campusconnect.job.service.EligibilityService;
import com.campusconnect.job.service.JobService;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import com.campusconnect.student.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationStatusHistoryRepository historyRepository;

    @Mock
    private JobService jobService;

    @Mock
    private StudentService studentService;

    @Mock
    private EligibilityService eligibilityService;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private ApplicationService applicationService;

    private StudentProfile verifiedStudent;
    private StudentProfile unverifiedStudent;
    private Job openApprovedJob;
    private Company company;
    private User recruiterUser;

    @BeforeEach
    void setUp() {
        recruiterUser = new User();
        recruiterUser.setEmail("recruiter@acme.com");

        company = new Company();
        company.setId(10L);
        company.setCompanyName("Acme Corp");
        company.setRecruiter(recruiterUser);

        User studentUser = new User();
        studentUser.setEmail("student@college.edu");

        verifiedStudent = new StudentProfile();
        verifiedStudent.setId(1L);
        verifiedStudent.setUser(studentUser);
        verifiedStudent.setVerificationStatus(VerificationStatus.VERIFIED);
        verifiedStudent.setResumeFileName("123-resume.pdf");
        verifiedStudent.setResumeOriginalName("my_resume.pdf");

        unverifiedStudent = new StudentProfile();
        unverifiedStudent.setId(2L);
        unverifiedStudent.setUser(studentUser);
        unverifiedStudent.setVerificationStatus(VerificationStatus.PENDING);
        unverifiedStudent.setResumeFileName("123-resume.pdf");

        openApprovedJob = new Job();
        openApprovedJob.setId(100L);
        openApprovedJob.setTitle("Software Engineer");
        openApprovedJob.setStatus(JobStatus.APPROVED);
        openApprovedJob.setCompany(company);
        openApprovedJob.setApplicationOpenDate(LocalDate.now().minusDays(5));
        openApprovedJob.setApplicationCloseDate(LocalDate.now().plusDays(25));
    }

    @Test
    void applyToJob_WhenEligibleAndWithinDates_CreatesApplicationAndHistory() {
        when(studentService.getProfileByEmail("student@college.edu")).thenReturn(verifiedStudent);
        when(jobService.getJobById(100L)).thenReturn(openApprovedJob);
        when(applicationRepository.existsByStudentIdAndJobId(1L, 100L)).thenReturn(false);
        when(eligibilityService.evaluate(verifiedStudent, openApprovedJob)).thenReturn(EligibilityResult.eligible());
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Application application = applicationService.applyToJob("student@college.edu", 100L);

        assertThat(application).isNotNull();
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(application.getStudent()).isEqualTo(verifiedStudent);
        assertThat(application.getJob()).isEqualTo(openApprovedJob);

        verify(applicationRepository).save(any(Application.class));
        verify(historyRepository).save(any());
    }

    @Test
    void applyToJob_WhenStudentNotVerified_ThrowsIllegalStateException() {
        when(studentService.getProfileByEmail("student@college.edu")).thenReturn(unverifiedStudent);

        assertThatThrownBy(() -> applicationService.applyToJob("student@college.edu", 100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("profile must be verified");
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyToJob_WhenStudentHasNoResume_ThrowsIllegalStateException() {
        verifiedStudent.setResumeFileName(null);
        when(studentService.getProfileByEmail("student@college.edu")).thenReturn(verifiedStudent);

        assertThatThrownBy(() -> applicationService.applyToJob("student@college.edu", 100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must upload a resume");
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyToJob_WhenJobNotApproved_ThrowsIllegalStateException() {
        openApprovedJob.setStatus(JobStatus.PENDING_APPROVAL);
        when(studentService.getProfileByEmail("student@college.edu")).thenReturn(verifiedStudent);
        when(jobService.getJobById(100L)).thenReturn(openApprovedJob);

        assertThatThrownBy(() -> applicationService.applyToJob("student@college.edu", 100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not open for applications");
    }

    @Test
    void applyToJob_WhenApplicationWindowNotYetOpen_ThrowsIllegalStateException() {
        openApprovedJob.setApplicationOpenDate(LocalDate.now().plusDays(2));
        openApprovedJob.setApplicationCloseDate(LocalDate.now().plusDays(30));
        when(studentService.getProfileByEmail("student@college.edu")).thenReturn(verifiedStudent);
        when(jobService.getJobById(100L)).thenReturn(openApprovedJob);

        assertThatThrownBy(() -> applicationService.applyToJob("student@college.edu", 100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("have not opened yet");
    }

    @Test
    void applyToJob_WhenApplicationDeadlinePassed_ThrowsIllegalStateException() {
        openApprovedJob.setApplicationOpenDate(LocalDate.now().minusDays(30));
        openApprovedJob.setApplicationCloseDate(LocalDate.now().minusDays(1));
        when(studentService.getProfileByEmail("student@college.edu")).thenReturn(verifiedStudent);
        when(jobService.getJobById(100L)).thenReturn(openApprovedJob);

        assertThatThrownBy(() -> applicationService.applyToJob("student@college.edu", 100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("deadline for this job posting has passed");
    }

    @Test
    void applyToJob_WhenDuplicateApplication_ThrowsIllegalStateException() {
        when(studentService.getProfileByEmail("student@college.edu")).thenReturn(verifiedStudent);
        when(jobService.getJobById(100L)).thenReturn(openApprovedJob);
        when(applicationRepository.existsByStudentIdAndJobId(1L, 100L)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.applyToJob("student@college.edu", 100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already submitted an application");
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyToJob_WhenIneligible_ThrowsIllegalStateException() {
        when(studentService.getProfileByEmail("student@college.edu")).thenReturn(verifiedStudent);
        when(jobService.getJobById(100L)).thenReturn(openApprovedJob);
        when(applicationRepository.existsByStudentIdAndJobId(1L, 100L)).thenReturn(false);
        when(eligibilityService.evaluate(verifiedStudent, openApprovedJob))
                .thenReturn(EligibilityResult.ineligible(List.of("CGPA below cutoff"), true));

        assertThatThrownBy(() -> applicationService.applyToJob("student@college.edu", 100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("do not satisfy the eligibility requirements");
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void updateApplicationStatus_PermittedTransition_UpdatesAndRecordsHistory() {
        Application application = new Application(verifiedStudent, openApprovedJob);
        application.setId(500L);
        application.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(application));

        applicationService.updateApplicationStatus(
                "recruiter@acme.com", 500L, ApplicationStatus.UNDER_REVIEW, "Reviewing resume"
        );

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
        verify(applicationRepository).save(application);
        verify(historyRepository).save(any());
    }

    @Test
    void updateApplicationStatus_InvalidTransition_ThrowsTransitionException() {
        Application application = new Application(verifiedStudent, openApprovedJob);
        application.setId(500L);
        application.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(application));

        // APPLIED -> SELECTED is invalid
        assertThatThrownBy(() -> applicationService.updateApplicationStatus(
                "recruiter@acme.com", 500L, ApplicationStatus.SELECTED, "Direct select"
        )).isInstanceOf(ApplicationStatusTransitionException.class)
                .hasMessageContaining("Cannot transition application from APPLIED to SELECTED");
    }

    @Test
    void updateApplicationStatus_TerminalStatus_ThrowsTransitionException() {
        Application application = new Application(verifiedStudent, openApprovedJob);
        application.setId(500L);
        application.setStatus(ApplicationStatus.REJECTED);

        when(applicationRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(
                "recruiter@acme.com", 500L, ApplicationStatus.SHORTLISTED, "Revive candidate"
        )).isInstanceOf(ApplicationStatusTransitionException.class);
    }

    @Test
    void updateApplicationStatus_UnauthorizedRecruiter_ThrowsIllegalArgumentException() {
        Application application = new Application(verifiedStudent, openApprovedJob);
        application.setId(500L);

        when(applicationRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(
                "intruder@other.com", 500L, ApplicationStatus.UNDER_REVIEW, "Hacking status"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void withdrawApplication_WhenNonTerminal_SetsStatusWithdrawn() {
        Application application = new Application(verifiedStudent, openApprovedJob);
        application.setId(500L);
        application.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(application));

        applicationService.withdrawApplication("student@college.edu", 500L);

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.WITHDRAWN);
        verify(applicationRepository).save(application);
        verify(historyRepository).save(any());
    }

    @Test
    void withdrawApplication_WhenAlreadyTerminal_ThrowsIllegalStateException() {
        Application application = new Application(verifiedStudent, openApprovedJob);
        application.setId(500L);
        application.setStatus(ApplicationStatus.SELECTED);

        when(applicationRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.withdrawApplication("student@college.edu", 500L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be withdrawn");
    }
}
