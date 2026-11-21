package com.campusconnect.application.controller;

import com.campusconnect.application.entity.Application;
import com.campusconnect.application.entity.ApplicationStatus;
import com.campusconnect.application.exception.ApplicationStatusTransitionException;
import com.campusconnect.application.service.ApplicationService;
import com.campusconnect.auth.entity.User;
import com.campusconnect.common.storage.FileStorageService;
import com.campusconnect.company.entity.Company;
import com.campusconnect.job.entity.Job;
import com.campusconnect.job.service.JobService;
import com.campusconnect.student.entity.StudentProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecruiterApplicantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private JobService jobService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private com.campusconnect.interview.service.InterviewService interviewService;

    private Application mockApplication;
    private Job mockJob;

    @BeforeEach
    void setUp() {
        User recruiter = new User();
        recruiter.setEmail("recruiter@acme.com");

        Company company = new Company();
        company.setId(10L);
        company.setCompanyName("Acme Corp");
        company.setRecruiter(recruiter);

        mockJob = new Job();
        mockJob.setId(100L);
        mockJob.setTitle("Backend Engineer");
        mockJob.setCompany(company);

        User studentUser = new User();
        studentUser.setFullName("Jane Student");
        studentUser.setEmail("jane@college.edu");

        StudentProfile student = new StudentProfile();
        student.setId(1L);
        student.setUser(studentUser);
        student.setRollNumber("2022CS102");
        student.setDepartment("Computer Science");
        student.setResumeFileName("jane-resume.pdf");
        student.setResumeOriginalName("jane_resume.pdf");

        mockApplication = new Application(student, mockJob);
        mockApplication.setId(75L);
        mockApplication.setStatus(ApplicationStatus.APPLIED);
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void listCompanyApplications_ReturnsListView() throws Exception {
        when(applicationService.getCompanyApplications(eq("recruiter@acme.com"), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockApplication)));
        when(jobService.getCompanyJobs(eq("recruiter@acme.com"), any()))
                .thenReturn(new PageImpl<>(List.of(mockJob)));

        mockMvc.perform(get("/recruiter/applications"))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/applicants-list"))
                .andExpect(model().attributeExists("applications"))
                .andExpect(model().attributeExists("jobs"));
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void listJobApplicants_ReturnsListView() throws Exception {
        when(applicationService.getJobApplicants(eq("recruiter@acme.com"), eq(100L), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockApplication)));
        when(jobService.getJobById(100L)).thenReturn(mockJob);

        mockMvc.perform(get("/recruiter/jobs/100/applicants"))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/applicants-list"))
                .andExpect(model().attributeExists("applications"))
                .andExpect(model().attributeExists("job"));
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void viewApplicantDetail_ReturnsDetailView() throws Exception {
        when(applicationService.getApplicationDetailForRecruiter("recruiter@acme.com", 75L))
                .thenReturn(mockApplication);

        mockMvc.perform(get("/recruiter/applications/75"))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/applicant-detail"))
                .andExpect(model().attributeExists("application"))
                .andExpect(model().attributeExists("statusUpdateRequest"));
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void updateStatus_ValidTransition_RedirectsWithSuccess() throws Exception {
        mockMvc.perform(post("/recruiter/applications/75/status")
                        .with(csrf())
                        .param("status", "UNDER_REVIEW")
                        .param("comments", "Reviewing technical background."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recruiter/applications/75"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(applicationService).updateApplicationStatus(
                "recruiter@acme.com", 75L, ApplicationStatus.UNDER_REVIEW, "Reviewing technical background."
        );
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void updateStatus_InvalidTransition_RedirectsWithErrorMessage() throws Exception {
        org.mockito.Mockito.doThrow(new ApplicationStatusTransitionException(ApplicationStatus.APPLIED, ApplicationStatus.SELECTED))
                .when(applicationService).updateApplicationStatus("recruiter@acme.com", 75L, ApplicationStatus.SELECTED, null);

        mockMvc.perform(post("/recruiter/applications/75/status")
                        .with(csrf())
                        .param("status", "SELECTED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recruiter/applications/75"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void downloadApplicantResume_ReturnsResumeFile() throws Exception {
        when(applicationService.getApplicationDetailForRecruiter("recruiter@acme.com", 75L))
                .thenReturn(mockApplication);
        when(fileStorageService.loadFileAsResource("jane-resume.pdf"))
                .thenReturn(new ByteArrayResource("dummy pdf content".getBytes()));

        mockMvc.perform(get("/recruiter/applications/75/resume"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"jane_resume.pdf\""));
    }
}
