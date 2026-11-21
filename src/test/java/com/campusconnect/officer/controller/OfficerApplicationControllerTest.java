package com.campusconnect.officer.controller;

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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OfficerApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private com.campusconnect.interview.service.InterviewService interviewService;

    private Application mockApplication;

    @BeforeEach
    void setUp() {
        User studentUser = new User();
        studentUser.setFullName("Alice Student");
        studentUser.setEmail("alice@college.edu");

        StudentProfile student = new StudentProfile();
        student.setId(1L);
        student.setUser(studentUser);
        student.setRollNumber("2022CS105");
        student.setDepartment("Computer Science");

        Company company = new Company();
        company.setId(10L);
        company.setCompanyName("Amazon");

        Job job = new Job();
        job.setId(200L);
        job.setTitle("Cloud Support Associate");
        job.setCompany(company);

        mockApplication = new Application(student, job);
        mockApplication.setId(90L);
        mockApplication.setStatus(ApplicationStatus.UNDER_REVIEW);
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void listApplications_AsOfficer_ReturnsListView() throws Exception {
        when(applicationService.getOfficerApplications(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockApplication)));

        mockMvc.perform(get("/officer/applications"))
                .andExpect(status().isOk())
                .andExpect(view().name("officer/applications-list"))
                .andExpect(model().attributeExists("applications"));
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void viewApplicationDetail_AsOfficer_ReturnsDetailView() throws Exception {
        when(applicationService.getApplicationDetailForOfficer(90L))
                .thenReturn(mockApplication);
        when(interviewService.getInterviewsForApplication(90L))
                .thenReturn(List.of());

        mockMvc.perform(get("/officer/applications/90"))
                .andExpect(status().isOk())
                .andExpect(view().name("officer/application-detail"))
                .andExpect(model().attributeExists("application"))
                .andExpect(model().attributeExists("interviews"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void listApplications_AsStudent_Forbidden() throws Exception {
        mockMvc.perform(get("/officer/applications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void listApplications_AsRecruiter_Forbidden() throws Exception {
        mockMvc.perform(get("/officer/applications"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listApplications_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/officer/applications"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
