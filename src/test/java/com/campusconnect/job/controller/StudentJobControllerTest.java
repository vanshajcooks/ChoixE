package com.campusconnect.job.controller;

import com.campusconnect.company.entity.Company;
import com.campusconnect.job.entity.Job;
import com.campusconnect.job.entity.JobStatus;
import com.campusconnect.job.service.EligibilityResult;
import com.campusconnect.job.service.JobService;
import com.campusconnect.job.service.JobService.StudentJobItem;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @MockBean
    private com.campusconnect.application.service.ApplicationService applicationService;

    private Job mockJob;
    private StudentJobItem mockStudentJobItem;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setId(1L);
        company.setCompanyName("Google Inc.");

        mockJob = new Job();
        mockJob.setId(200L);
        mockJob.setTitle("Site Reliability Engineer");
        mockJob.setRoleType("Full-Time");
        mockJob.setCompany(company);
        mockJob.setStatus(JobStatus.APPROVED);
        mockJob.setApplicationOpenDate(LocalDate.now());
        mockJob.setApplicationCloseDate(LocalDate.now().plusDays(15));

        mockStudentJobItem = new StudentJobItem(mockJob, EligibilityResult.eligible());
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void listJobs_AsStudent_ReturnsJobsList() throws Exception {
        when(jobService.getStudentJobs(eq("student@college.edu"), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockStudentJobItem)));

        mockMvc.perform(get("/student/jobs"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/jobs-list"))
                .andExpect(model().attributeExists("jobs"))
                .andExpect(model().attributeExists("username"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void viewJobDetail_AsStudent_ReturnsJobDetail() throws Exception {
        when(jobService.getStudentJobDetail("student@college.edu", 200L))
                .thenReturn(mockStudentJobItem);

        mockMvc.perform(get("/student/jobs/200"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/job-detail"))
                .andExpect(model().attributeExists("job"))
                .andExpect(model().attributeExists("eligibility"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void viewJobDetail_WhenJobNotApproved_RedirectsToJobsList() throws Exception {
        when(jobService.getStudentJobDetail("student@college.edu", 999L))
                .thenThrow(new IllegalArgumentException("Job not approved"));

        mockMvc.perform(get("/student/jobs/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/jobs"));
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void listJobs_AsRecruiter_Forbidden() throws Exception {
        mockMvc.perform(get("/student/jobs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listJobs_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/student/jobs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
