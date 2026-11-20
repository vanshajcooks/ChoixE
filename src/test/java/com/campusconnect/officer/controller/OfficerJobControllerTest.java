package com.campusconnect.officer.controller;

import com.campusconnect.company.entity.Company;
import com.campusconnect.job.entity.Job;
import com.campusconnect.job.entity.JobStatus;
import com.campusconnect.job.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OfficerJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    private Job mockJob;

    @BeforeEach
    void setUp() {
        com.campusconnect.auth.entity.User recruiter = new com.campusconnect.auth.entity.User();
        recruiter.setFullName("Jane Recruiter");
        recruiter.setEmail("recruiter@acme.com");

        Company company = new Company();
        company.setId(1L);
        company.setCompanyName("Acme Corp");
        company.setRecruiter(recruiter);

        mockJob = new Job();
        mockJob.setId(100L);
        mockJob.setTitle("Backend Engineer");
        mockJob.setRoleType("Full-Time");
        mockJob.setCompany(company);
        mockJob.setStatus(JobStatus.PENDING_APPROVAL);
        mockJob.setApplicationOpenDate(LocalDate.now());
        mockJob.setApplicationCloseDate(LocalDate.now().plusDays(30));
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void listJobs_AsOfficer_ReturnsListView() throws Exception {
        when(jobService.getOfficerJobs(any(), any(), any())).thenReturn(new PageImpl<>(List.of(mockJob)));
        when(jobService.getPendingJobCount()).thenReturn(1L);

        mockMvc.perform(get("/officer/jobs"))
                .andExpect(status().isOk())
                .andExpect(view().name("officer/jobs-list"))
                .andExpect(model().attributeExists("jobs"))
                .andExpect(model().attribute("pendingCount", 1L));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void listJobs_AsStudent_Forbidden() throws Exception {
        mockMvc.perform(get("/officer/jobs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void listJobs_AsRecruiter_Forbidden() throws Exception {
        mockMvc.perform(get("/officer/jobs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void viewJobDetail_AsOfficer_ReturnsDetailView() throws Exception {
        when(jobService.getJobById(100L)).thenReturn(mockJob);

        mockMvc.perform(get("/officer/jobs/100"))
                .andExpect(status().isOk())
                .andExpect(view().name("officer/job-detail"))
                .andExpect(model().attributeExists("job"))
                .andExpect(model().attributeExists("decisionRequest"));
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void approveJob_ValidCall_RedirectsWithSuccess() throws Exception {
        mockMvc.perform(post("/officer/jobs/100/approve")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/jobs/100"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(jobService).approveJob(100L, "officer@campusconnect.edu");
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void rejectJob_WithReason_RedirectsWithWarning() throws Exception {
        mockMvc.perform(post("/officer/jobs/100/reject")
                        .with(csrf())
                        .param("reason", "Compensation criteria do not meet institutional standards."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/jobs/100"))
                .andExpect(flash().attributeExists("warningMessage"));

        verify(jobService).rejectJob(100L, "Compensation criteria do not meet institutional standards.", "officer@campusconnect.edu");
    }
}
