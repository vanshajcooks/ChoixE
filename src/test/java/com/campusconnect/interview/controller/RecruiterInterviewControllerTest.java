package com.campusconnect.interview.controller;

import com.campusconnect.application.entity.Application;
import com.campusconnect.application.entity.ApplicationStatus;
import com.campusconnect.application.service.ApplicationService;
import com.campusconnect.auth.entity.User;
import com.campusconnect.company.entity.Company;
import com.campusconnect.interview.entity.Interview;
import com.campusconnect.interview.entity.InterviewMode;
import com.campusconnect.interview.entity.InterviewStatus;
import com.campusconnect.interview.service.InterviewService;
import com.campusconnect.job.entity.Job;
import com.campusconnect.job.service.JobService;
import com.campusconnect.student.entity.StudentProfile;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecruiterInterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InterviewService interviewService;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private JobService jobService;

    private Application mockApplication;
    private Interview mockInterview;
    private Job mockJob;

    @BeforeEach
    void setUp() {
        User recruiter = new User();
        recruiter.setEmail("recruiter@acme.com");

        Company company = new Company();
        company.setId(1L);
        company.setCompanyName("Acme Corp");
        company.setRecruiter(recruiter);

        mockJob = new Job();
        mockJob.setId(50L);
        mockJob.setTitle("Backend Engineer");
        mockJob.setCompany(company);

        User studentUser = new User();
        studentUser.setFullName("Jane Student");
        studentUser.setEmail("jane@college.edu");

        StudentProfile student = new StudentProfile();
        student.setId(10L);
        student.setUser(studentUser);
        student.setRollNumber("2022CS102");
        student.setDepartment("Computer Science");

        mockApplication = new Application(student, mockJob);
        mockApplication.setId(100L);
        mockApplication.setStatus(ApplicationStatus.SHORTLISTED);

        mockInterview = new Interview(
                mockApplication,
                "Round 1: Technical Screening",
                LocalDateTime.now().plusDays(3),
                InterviewMode.VIRTUAL,
                "https://meet.google.com/xyz",
                "Be ready"
        );
        mockInterview.setId(500L);
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void listCompanyInterviews_ReturnsListView() throws Exception {
        when(interviewService.getCompanyInterviews(eq("recruiter@acme.com"), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockInterview)));
        when(jobService.getCompanyJobs(eq("recruiter@acme.com"), any()))
                .thenReturn(new PageImpl<>(List.of(mockJob)));

        mockMvc.perform(get("/recruiter/interviews"))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/interviews-list"))
                .andExpect(model().attributeExists("interviews"))
                .andExpect(model().attributeExists("jobs"));
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void showScheduleForm_ReturnsFormView() throws Exception {
        when(applicationService.getApplicationDetailForRecruiter("recruiter@acme.com", 100L))
                .thenReturn(mockApplication);

        mockMvc.perform(get("/recruiter/applications/100/interviews/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/interview-form"))
                .andExpect(model().attributeExists("app"))
                .andExpect(model().attributeExists("interviewRequest"));
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void scheduleInterview_ValidSubmission_RedirectsToApplication() throws Exception {
        when(applicationService.getApplicationDetailForRecruiter("recruiter@acme.com", 100L))
                .thenReturn(mockApplication);

        mockMvc.perform(post("/recruiter/applications/100/interviews")
                        .with(csrf())
                        .param("roundName", "Round 1: Technical Screening")
                        .param("scheduledAt", "2028-12-01T10:00")
                        .param("mode", "VIRTUAL")
                        .param("locationOrLink", "https://meet.google.com/abc-xyz")
                        .param("instructions", "Keep IDE ready")
                        .param("advanceApplicationStatus", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recruiter/applications/100"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void scheduleInterview_InvalidSubmission_ReturnsFormWithErrors() throws Exception {
        when(applicationService.getApplicationDetailForRecruiter("recruiter@acme.com", 100L))
                .thenReturn(mockApplication);

        mockMvc.perform(post("/recruiter/applications/100/interviews")
                        .with(csrf())
                        .param("roundName", "") // blank
                        .param("mode", "VIRTUAL"))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/interview-form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void showEditForm_ReturnsEditView() throws Exception {
        when(interviewService.getInterviewDetailForRecruiter("recruiter@acme.com", 500L))
                .thenReturn(mockInterview);

        mockMvc.perform(get("/recruiter/interviews/500/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/interview-edit"))
                .andExpect(model().attributeExists("interview"))
                .andExpect(model().attributeExists("interviewRequest"));
    }

    @Test
    @WithMockUser(username = "recruiter@acme.com", roles = {"RECRUITER"})
    void updateInterview_ValidSubmission_RedirectsToInterviewsList() throws Exception {
        when(interviewService.getInterviewDetailForRecruiter("recruiter@acme.com", 500L))
                .thenReturn(mockInterview);

        mockMvc.perform(post("/recruiter/interviews/500")
                        .with(csrf())
                        .param("roundName", "Round 1: Technical Screening")
                        .param("scheduledAt", "2028-12-01T14:00")
                        .param("mode", "VIRTUAL")
                        .param("locationOrLink", "https://meet.google.com/new")
                        .param("status", "SCHEDULED")
                        .param("feedback", "Candidate performed well"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recruiter/interviews"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void recruiterInterviews_AsStudent_Forbidden() throws Exception {
        mockMvc.perform(get("/recruiter/interviews"))
                .andExpect(status().isForbidden());
    }
}
