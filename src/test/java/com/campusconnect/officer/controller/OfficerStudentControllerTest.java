package com.campusconnect.officer.controller;

import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.officer.service.OfficerStudentService;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import com.campusconnect.student.service.StudentService.ResumeDownload;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OfficerStudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OfficerStudentService officerStudentService;

    private StudentProfile mockStudent;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(2L);
        user.setFullName("David Miller");
        user.setEmail("david.miller@college.edu");
        user.setRole(Role.STUDENT);

        mockStudent = new StudentProfile();
        mockStudent.setId(42L);
        mockStudent.setUser(user);
        mockStudent.setRollNumber("2022CS42");
        mockStudent.setDepartment("Computer Science");
        mockStudent.setGraduationYear(2026);
        mockStudent.setCgpa(new BigDecimal("8.70"));
        mockStudent.setActiveBacklogs(0);
        mockStudent.setVerificationStatus(VerificationStatus.PENDING);
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void listStudents_AsOfficer_ReturnsListView() throws Exception {
        when(officerStudentService.getStudents(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockStudent)));
        when(officerStudentService.getPendingCount()).thenReturn(1L);

        mockMvc.perform(get("/officer/students"))
                .andExpect(status().isOk())
                .andExpect(view().name("officer/students-list"))
                .andExpect(model().attributeExists("students"))
                .andExpect(model().attribute("pendingCount", 1L));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void listStudents_AsStudent_Forbidden() throws Exception {
        mockMvc.perform(get("/officer/students"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void viewStudentDetail_AsOfficer_ReturnsDetailView() throws Exception {
        when(officerStudentService.getStudentById(42L)).thenReturn(mockStudent);

        mockMvc.perform(get("/officer/students/42"))
                .andExpect(status().isOk())
                .andExpect(view().name("officer/student-detail"))
                .andExpect(model().attributeExists("student"))
                .andExpect(model().attributeExists("decisionRequest"));
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void verifyStudent_ValidCall_RedirectsWithSuccess() throws Exception {
        mockMvc.perform(post("/officer/students/42/verify")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/students/42"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(officerStudentService).verifyStudent(42L, "officer@campusconnect.edu");
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void rejectStudent_WithReason_RedirectsWithWarning() throws Exception {
        mockMvc.perform(post("/officer/students/42/reject")
                .with(csrf())
                .param("reason", "Marksheet transcript unverified."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/students/42"))
                .andExpect(flash().attributeExists("warningMessage"));

        verify(officerStudentService).rejectStudent(42L, "Marksheet transcript unverified.", "officer@campusconnect.edu");
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void downloadStudentResume_ReturnsResource() throws Exception {
        ResumeDownload download = new ResumeDownload(
                new ByteArrayResource("sample-pdf".getBytes()),
                "david_resume.pdf"
        );
        when(officerStudentService.getStudentResumeResource(42L)).thenReturn(download);

        mockMvc.perform(get("/officer/students/42/resume"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"david_resume.pdf\""));
    }
}
