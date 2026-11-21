package com.campusconnect.interview.controller;

import com.campusconnect.application.entity.Application;
import com.campusconnect.auth.entity.User;
import com.campusconnect.company.entity.Company;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentInterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InterviewService interviewService;

    private Interview mockInterview;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setId(1L);
        company.setCompanyName("Google Inc.");

        Job job = new Job();
        job.setId(10L);
        job.setTitle("Software Engineer");
        job.setCompany(company);

        User studentUser = new User();
        studentUser.setEmail("student@college.edu");
        studentUser.setFullName("John Student");

        StudentProfile student = new StudentProfile();
        student.setId(5L);
        student.setUser(studentUser);
        student.setRollNumber("2022CS101");

        Application app = new Application(student, job);
        app.setId(100L);

        mockInterview = new Interview(
                app,
                "Round 1: Screening",
                LocalDateTime.now().plusDays(2),
                InterviewMode.VIRTUAL,
                "https://meet.google.com/xyz",
                "Be on time"
        );
        mockInterview.setId(50L);
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void viewStudentInterviews_AsStudent_ReturnsInterviewsView() throws Exception {
        when(interviewService.getStudentInterviews(eq("student@college.edu"), any()))
                .thenReturn(new PageImpl<>(List.of(mockInterview)));
        when(interviewService.getUpcomingStudentInterviews("student@college.edu"))
                .thenReturn(List.of(mockInterview));

        mockMvc.perform(get("/student/interviews"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/interviews-list"))
                .andExpect(model().attributeExists("interviews"))
                .andExpect(model().attributeExists("upcomingInterviews"))
                .andExpect(model().attributeExists("username"));
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void viewStudentInterviews_AsRecruiter_Forbidden() throws Exception {
        mockMvc.perform(get("/student/interviews"))
                .andExpect(status().isForbidden());
    }
}
