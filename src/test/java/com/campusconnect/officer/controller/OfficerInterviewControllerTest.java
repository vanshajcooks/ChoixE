package com.campusconnect.officer.controller;

import com.campusconnect.application.entity.Application;
import com.campusconnect.auth.entity.User;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.repository.CompanyRepository;
import com.campusconnect.interview.entity.Interview;
import com.campusconnect.interview.entity.InterviewMode;
import com.campusconnect.interview.service.InterviewService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OfficerInterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InterviewService interviewService;

    @MockBean
    private CompanyRepository companyRepository;

    private Interview mockInterview;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setId(1L);
        company.setCompanyName("Amazon AWS");

        Job job = new Job();
        job.setId(20L);
        job.setTitle("Cloud Support Associate");
        job.setCompany(company);

        User studentUser = new User();
        studentUser.setEmail("alex@college.edu");
        studentUser.setFullName("Alex Morgan");

        StudentProfile student = new StudentProfile();
        student.setId(7L);
        student.setUser(studentUser);
        student.setRollNumber("2022CS204");

        Application app = new Application(student, job);
        app.setId(200L);

        mockInterview = new Interview(
                app,
                "Technical Assessment",
                LocalDateTime.now().plusDays(4),
                InterviewMode.VIRTUAL,
                "https://chime.aws/12345",
                "Chime credentials"
        );
        mockInterview.setId(700L);
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void listAllInterviews_AsOfficer_ReturnsListView() throws Exception {
        when(interviewService.getOfficerInterviews(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockInterview)));

        mockMvc.perform(get("/officer/interviews"))
                .andExpect(status().isOk())
                .andExpect(view().name("officer/interviews-list"))
                .andExpect(model().attributeExists("interviews"))
                .andExpect(model().attributeExists("companies"));
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void viewInterviewDetail_AsOfficer_ReturnsDetailView() throws Exception {
        when(interviewService.getInterviewDetailForOfficer(700L)).thenReturn(mockInterview);

        mockMvc.perform(get("/officer/interviews/700"))
                .andExpect(status().isOk())
                .andExpect(view().name("officer/interview-detail"))
                .andExpect(model().attributeExists("interview"))
                .andExpect(model().attributeExists("app"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void officerInterviews_AsStudent_Forbidden() throws Exception {
        mockMvc.perform(get("/officer/interviews"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void officerInterviews_AsRecruiter_Forbidden() throws Exception {
        mockMvc.perform(get("/officer/interviews"))
                .andExpect(status().isForbidden());
    }
}
