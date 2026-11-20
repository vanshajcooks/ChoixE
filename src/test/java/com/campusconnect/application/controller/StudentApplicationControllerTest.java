package com.campusconnect.application.controller;

import com.campusconnect.application.entity.Application;
import com.campusconnect.application.entity.ApplicationStatus;
import com.campusconnect.application.service.ApplicationService;
import com.campusconnect.auth.entity.User;
import com.campusconnect.company.entity.Company;
import com.campusconnect.job.entity.Job;
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
class StudentApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    private Application mockApplication;

    @BeforeEach
    void setUp() {
        User studentUser = new User();
        studentUser.setFullName("John Doe");
        studentUser.setEmail("student@college.edu");

        StudentProfile student = new StudentProfile();
        student.setId(1L);
        student.setUser(studentUser);
        student.setRollNumber("2022CS101");
        student.setDepartment("Computer Science");

        Company company = new Company();
        company.setId(10L);
        company.setCompanyName("Google Inc.");

        Job job = new Job();
        job.setId(100L);
        job.setTitle("Software Engineer");
        job.setCompany(company);

        mockApplication = new Application(student, job);
        mockApplication.setId(50L);
        mockApplication.setStatus(ApplicationStatus.APPLIED);
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void applyToJob_ValidSubmission_RedirectsToApplicationDetail() throws Exception {
        when(applicationService.applyToJob("student@college.edu", 100L)).thenReturn(mockApplication);

        mockMvc.perform(post("/student/jobs/100/apply")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/applications/50"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void applyToJob_WhenServiceThrowsIllegalState_RedirectsToJobWithErrorMessage() throws Exception {
        when(applicationService.applyToJob("student@college.edu", 100L))
                .thenThrow(new IllegalStateException("You do not meet eligibility requirements."));

        mockMvc.perform(post("/student/jobs/100/apply")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/jobs/100"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void listApplications_ReturnsListView() throws Exception {
        when(applicationService.getStudentApplications(eq("student@college.edu"), any()))
                .thenReturn(new PageImpl<>(List.of(mockApplication)));

        mockMvc.perform(get("/student/applications"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/applications-list"))
                .andExpect(model().attributeExists("applications"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void viewApplicationDetail_AuthorizedStudent_ReturnsDetailView() throws Exception {
        when(applicationService.getStudentApplicationDetail("student@college.edu", 50L))
                .thenReturn(mockApplication);

        mockMvc.perform(get("/student/applications/50"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/application-detail"))
                .andExpect(model().attributeExists("application"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void withdrawApplication_ValidCall_RedirectsWithWarning() throws Exception {
        mockMvc.perform(post("/student/applications/50/withdraw")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/applications/50"))
                .andExpect(flash().attributeExists("warningMessage"));

        verify(applicationService).withdrawApplication("student@college.edu", 50L);
    }
}
