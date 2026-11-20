package com.campusconnect.job.controller;

import com.campusconnect.company.entity.Company;
import com.campusconnect.company.entity.CompanyApprovalStatus;
import com.campusconnect.company.service.CompanyService;
import com.campusconnect.job.entity.Job;
import com.campusconnect.job.entity.JobStatus;
import com.campusconnect.job.service.JobService;
import com.campusconnect.auth.entity.User;
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
class RecruiterJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @MockBean
    private CompanyService companyService;

    private User recruiterUser;
    private Company approvedCompany;
    private Company pendingCompany;
    private Job mockJob;

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

        mockJob = new Job();
        mockJob.setId(300L);
        mockJob.setTitle("Cloud Architect");
        mockJob.setRoleType("Full-Time");
        mockJob.setCompany(approvedCompany);
        mockJob.setStatus(JobStatus.DRAFT);
        mockJob.setApplicationOpenDate(LocalDate.now());
        mockJob.setApplicationCloseDate(LocalDate.now().plusDays(30));
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void listJobs_AsRecruiter_ReturnsJobsList() throws Exception {
        when(jobService.getCompanyJobs(eq("recruiter@acme.com"), any())).thenReturn(new PageImpl<>(List.of(mockJob)));
        when(companyService.getCompanyByRecruiterEmail("recruiter@acme.com")).thenReturn(approvedCompany);

        mockMvc.perform(get("/recruiter/jobs"))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/jobs-list"))
                .andExpect(model().attributeExists("jobs"))
                .andExpect(model().attributeExists("company"));
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void newJobForm_WhenCompanyApproved_ReturnsForm() throws Exception {
        when(companyService.getCompanyByRecruiterEmail("recruiter@acme.com")).thenReturn(approvedCompany);

        mockMvc.perform(get("/recruiter/jobs/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/job-form"))
                .andExpect(model().attributeExists("request"))
                .andExpect(model().attributeExists("company"));
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void newJobForm_WhenCompanyNotApproved_RedirectsWithWarning() throws Exception {
        when(companyService.getCompanyByRecruiterEmail("recruiter@acme.com")).thenReturn(pendingCompany);

        mockMvc.perform(get("/recruiter/jobs/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recruiter/dashboard"))
                .andExpect(flash().attributeExists("warningMessage"));
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void createJob_WithValidData_RedirectsToJobDetail() throws Exception {
        when(jobService.createJob(eq("recruiter@acme.com"), any())).thenReturn(mockJob);

        mockMvc.perform(post("/recruiter/jobs/new")
                        .with(csrf())
                        .param("title", "Cloud Architect")
                        .param("roleType", "Full-Time")
                        .param("workMode", "Hybrid")
                        .param("location", "Hyderabad")
                        .param("salaryPackage", "20 LPA")
                        .param("description", "Responsible for scalable cloud infrastructure.")
                        .param("applicationOpenDate", LocalDate.now().toString())
                        .param("applicationCloseDate", LocalDate.now().plusDays(25).toString())
                        .param("minCgpa", "7.50")
                        .param("maxBacklogs", "0")
                        .param("graduationYear", "2026")
                        .param("eligibleBranches", "Computer Science")
                        .param("action", "SAVE_DRAFT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recruiter/jobs/300"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void createJob_WithValidationErrors_RedisplaysForm() throws Exception {
        when(companyService.getCompanyByRecruiterEmail("recruiter@acme.com")).thenReturn(approvedCompany);

        mockMvc.perform(post("/recruiter/jobs/new")
                        .with(csrf())
                        .param("title", "") // blank title violates @NotBlank
                        .param("roleType", "Full-Time")
                        .param("description", "Some description")
                        .param("applicationOpenDate", LocalDate.now().toString())
                        .param("applicationCloseDate", LocalDate.now().plusDays(25).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/job-form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void viewJobDetail_AsOwner_ReturnsDetailView() throws Exception {
        when(jobService.getJobById(300L)).thenReturn(mockJob);

        mockMvc.perform(get("/recruiter/jobs/300"))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/job-detail"))
                .andExpect(model().attributeExists("job"));
    }

    @Test
    @WithMockUser(username = "intruder@other.com", roles = {"RECRUITER"})
    void viewJobDetail_WhenNotOwner_RedirectsToJobs() throws Exception {
        when(jobService.getJobById(300L)).thenReturn(mockJob);

        mockMvc.perform(get("/recruiter/jobs/300"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recruiter/jobs"));
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void submitJob_ValidCall_RedirectsWithSuccess() throws Exception {
        mockMvc.perform(post("/recruiter/jobs/300/submit")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recruiter/jobs/300"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(jobService).submitJobForApproval("recruiter@acme.com", 300L);
    }
}
