package com.campusconnect.job.service;

import com.campusconnect.company.entity.Company;
import com.campusconnect.company.entity.CompanyApprovalStatus;
import com.campusconnect.company.service.CompanyService;
import com.campusconnect.job.dto.JobCreateRequest;
import com.campusconnect.job.entity.Job;
import com.campusconnect.job.entity.JobEligibilityCriteria;
import com.campusconnect.job.entity.JobStatus;
import com.campusconnect.job.repository.JobRepository;
import com.campusconnect.student.service.StudentService;
import com.campusconnect.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private StudentService studentService;

    @Mock
    private EligibilityService eligibilityService;

    @InjectMocks
    private JobService jobService;

    private User recruiterUser;
    private Company approvedCompany;
    private Company pendingCompany;

    @BeforeEach
    void setUp() {
        recruiterUser = new User();
        recruiterUser.setEmail("recruiter@acme.com");

        approvedCompany = new Company();
        approvedCompany.setId(1L);
        approvedCompany.setCompanyName("Acme Corp");
        approvedCompany.setApprovalStatus(CompanyApprovalStatus.APPROVED);
        approvedCompany.setRecruiter(recruiterUser);

        pendingCompany = new Company();
        pendingCompany.setId(2L);
        pendingCompany.setCompanyName("Pending Corp");
        pendingCompany.setApprovalStatus(CompanyApprovalStatus.PENDING);
        pendingCompany.setRecruiter(recruiterUser);
    }

    private JobCreateRequest createValidRequest() {
        JobCreateRequest request = new JobCreateRequest();
        request.setTitle("Software Engineer");
        request.setRoleType("Full-Time");
        request.setWorkMode("Remote");
        request.setLocation("Bangalore");
        request.setSalaryPackage("15 LPA");
        request.setDescription("Design and build scalable services.");
        request.setApplicationOpenDate(LocalDate.now());
        request.setApplicationCloseDate(LocalDate.now().plusDays(30));
        request.setMinCgpa(new BigDecimal("7.00"));
        request.setMaxBacklogs(0);
        request.setGraduationYear(2026);
        request.setEligibleBranches(List.of("Computer Science", "Information Technology"));
        return request;
    }

    @Test
    void createJob_WhenCompanyApproved_SavesAndReturnsJob() {
        when(companyService.getCompanyByRecruiterEmail("recruiter@acme.com")).thenReturn(approvedCompany);
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobCreateRequest request = createValidRequest();
        request.setAction("SUBMIT");

        Job created = jobService.createJob("recruiter@acme.com", request);

        assertThat(created).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Software Engineer");
        assertThat(created.getStatus()).isEqualTo(JobStatus.PENDING_APPROVAL);
        assertThat(created.getEligibilityCriteria()).isNotNull();
        assertThat(created.getEligibilityCriteria().getMinCgpa()).isEqualByComparingTo("7.00");
        assertThat(created.getEligibilityCriteria().getEligibleBranches()).contains("Computer Science", "Information Technology");
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    void createJob_WhenCompanyApprovedAndDraft_SavesAsDraft() {
        when(companyService.getCompanyByRecruiterEmail("recruiter@acme.com")).thenReturn(approvedCompany);
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobCreateRequest request = createValidRequest();
        request.setAction("SAVE_DRAFT");

        Job created = jobService.createJob("recruiter@acme.com", request);

        assertThat(created.getStatus()).isEqualTo(JobStatus.DRAFT);
    }

    @Test
    void createJob_WhenCompanyNotApproved_ThrowsIllegalStateException() {
        when(companyService.getCompanyByRecruiterEmail("recruiter@acme.com")).thenReturn(pendingCompany);

        JobCreateRequest request = createValidRequest();

        assertThatThrownBy(() -> jobService.createJob("recruiter@acme.com", request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only approved organizations can create job postings");
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    void createJob_WhenCloseDateBeforeOpenDate_ThrowsIllegalArgumentException() {
        when(companyService.getCompanyByRecruiterEmail("recruiter@acme.com")).thenReturn(approvedCompany);

        JobCreateRequest request = createValidRequest();
        request.setApplicationOpenDate(LocalDate.now().plusDays(10));
        request.setApplicationCloseDate(LocalDate.now());

        assertThatThrownBy(() -> jobService.createJob("recruiter@acme.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("closing date cannot be earlier than the opening date");
    }

    @Test
    void updateJob_WhenAuthorizedAndEditable_UpdatesFieldsAndSaves() {
        Job job = new Job();
        job.setId(10L);
        job.setCompany(approvedCompany);
        job.setStatus(JobStatus.DRAFT);
        job.setTitle("Old Title");
        job.setDescription("Old Description");
        job.setApplicationOpenDate(LocalDate.now());
        job.setApplicationCloseDate(LocalDate.now().plusDays(20));
        job.setEligibilityCriteria(new JobEligibilityCriteria());

        when(jobRepository.findByIdWithCompanyAndCriteria(10L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobCreateRequest request = createValidRequest();
        request.setTitle("Updated Senior Engineer");

        Job updated = jobService.updateJob("recruiter@acme.com", 10L, request);

        assertThat(updated.getTitle()).isEqualTo("Updated Senior Engineer");
        verify(jobRepository).save(job);
    }

    @Test
    void updateJob_WhenUnauthorizedRecruiter_ThrowsIllegalArgumentException() {
        Job job = new Job();
        job.setId(10L);
        job.setCompany(approvedCompany); // owner is recruiter@acme.com

        when(jobRepository.findByIdWithCompanyAndCriteria(10L)).thenReturn(Optional.of(job));

        JobCreateRequest request = createValidRequest();

        assertThatThrownBy(() -> jobService.updateJob("intruder@other.com", 10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void updateJob_WhenStatusApproved_ThrowsIllegalStateException() {
        Job job = new Job();
        job.setId(10L);
        job.setCompany(approvedCompany);
        job.setStatus(JobStatus.APPROVED);

        when(jobRepository.findByIdWithCompanyAndCriteria(10L)).thenReturn(Optional.of(job));

        JobCreateRequest request = createValidRequest();

        assertThatThrownBy(() -> jobService.updateJob("recruiter@acme.com", 10L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be modified in status: APPROVED");
    }

    @Test
    void submitJobForApproval_WhenValid_TransitionsToPendingApproval() {
        Job job = new Job();
        job.setId(10L);
        job.setCompany(approvedCompany);
        job.setStatus(JobStatus.DRAFT);

        when(jobRepository.findByIdWithCompanyAndCriteria(10L)).thenReturn(Optional.of(job));

        jobService.submitJobForApproval("recruiter@acme.com", 10L);

        assertThat(job.getStatus()).isEqualTo(JobStatus.PENDING_APPROVAL);
        verify(jobRepository).save(job);
    }

    @Test
    void approveJob_SetsStatusApprovedAndAuditMetadata() {
        Job job = new Job();
        job.setId(10L);
        job.setStatus(JobStatus.PENDING_APPROVAL);

        when(jobRepository.findByIdWithCompanyAndCriteria(10L)).thenReturn(Optional.of(job));

        jobService.approveJob(10L, "officer@campusconnect.edu");

        assertThat(job.getStatus()).isEqualTo(JobStatus.APPROVED);
        assertThat(job.getDecisionBy()).isEqualTo("officer@campusconnect.edu");
        assertThat(job.getDecisionAt()).isNotNull();
        assertThat(job.getRejectionReason()).isNull();
        verify(jobRepository).save(job);
    }

    @Test
    void rejectJob_WithValidReason_SetsStatusRejectedAndAuditMetadata() {
        Job job = new Job();
        job.setId(10L);
        job.setStatus(JobStatus.PENDING_APPROVAL);

        when(jobRepository.findByIdWithCompanyAndCriteria(10L)).thenReturn(Optional.of(job));

        jobService.rejectJob(10L, "Incomplete job description and unrealistic criteria.", "officer@campusconnect.edu");

        assertThat(job.getStatus()).isEqualTo(JobStatus.REJECTED);
        assertThat(job.getRejectionReason()).isEqualTo("Incomplete job description and unrealistic criteria.");
        assertThat(job.getDecisionBy()).isEqualTo("officer@campusconnect.edu");
        assertThat(job.getDecisionAt()).isNotNull();
        verify(jobRepository).save(job);
    }

    @Test
    void rejectJob_WithBlankReason_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> jobService.rejectJob(10L, "   ", "officer@campusconnect.edu"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A rejection reason must be provided");
    }
}
