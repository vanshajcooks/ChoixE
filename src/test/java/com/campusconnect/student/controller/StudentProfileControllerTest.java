package com.campusconnect.student.controller;

import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.student.dto.StudentProfileUpdateRequest;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import com.campusconnect.student.service.StudentService;
import com.campusconnect.student.service.StudentService.ResumeDownload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
class StudentProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    private StudentProfile mockProfile;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setFullName("Sam Student");
        user.setEmail("student@college.edu");
        user.setRole(Role.STUDENT);

        mockProfile = new StudentProfile();
        mockProfile.setId(10L);
        mockProfile.setUser(user);
        mockProfile.setRollNumber("2022CS501");
        mockProfile.setDepartment("Computer Science");
        mockProfile.setGraduationYear(2026);
        mockProfile.setCgpa(new BigDecimal("8.50"));
        mockProfile.setActiveBacklogs(0);
        mockProfile.setPhoneNumber("+91 9876543210");
        mockProfile.setVerificationStatus(VerificationStatus.PENDING);
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void viewProfile_AuthenticatedStudent_ReturnsProfileView() throws Exception {
        when(studentService.getProfileByEmail("student@college.edu")).thenReturn(mockProfile);

        mockMvc.perform(get("/student/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/profile"))
                .andExpect(model().attributeExists("profile"));
    }

    @Test
    void viewProfile_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/student/profile"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void viewProfile_RecruiterRole_Forbidden() throws Exception {
        mockMvc.perform(get("/student/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void editProfileForm_ReturnsEditView() throws Exception {
        when(studentService.getProfileByEmail("student@college.edu")).thenReturn(mockProfile);

        mockMvc.perform(get("/student/profile/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/profile-edit"))
                .andExpect(model().attributeExists("request"))
                .andExpect(model().attributeExists("profile"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void updateProfile_ValidAcademicChange_RedirectsWithWarningMessage() throws Exception {
        when(studentService.updateProfile(eq("student@college.edu"), any(StudentProfileUpdateRequest.class)))
                .thenReturn(true); // Academic changed!

        mockMvc.perform(post("/student/profile/edit")
                .with(csrf())
                .param("fullName", "Sam Updated")
                .param("phoneNumber", "+91 9876543210")
                .param("department", "Information Technology")
                .param("graduationYear", "2026")
                .param("cgpa", "9.00")
                .param("activeBacklogs", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/profile"))
                .andExpect(flash().attributeExists("warningMessage"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void updateProfile_ValidNonAcademicChange_RedirectsWithSuccessMessage() throws Exception {
        when(studentService.updateProfile(eq("student@college.edu"), any(StudentProfileUpdateRequest.class)))
                .thenReturn(false); // Only phone changed!

        mockMvc.perform(post("/student/profile/edit")
                .with(csrf())
                .param("fullName", "Sam Student")
                .param("phoneNumber", "+91 9999999999")
                .param("department", "Computer Science")
                .param("graduationYear", "2026")
                .param("cgpa", "8.50")
                .param("activeBacklogs", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/profile"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void updateProfile_ValidationFailure_ReturnsEditViewWithErrors() throws Exception {
        when(studentService.getProfileByEmail("student@college.edu")).thenReturn(mockProfile);

        mockMvc.perform(post("/student/profile/edit")
                .with(csrf())
                .param("fullName", "")
                .param("department", "")
                .param("graduationYear", "1990")
                .param("cgpa", "15.00")
                .param("activeBacklogs", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/profile-edit"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void uploadResume_ValidFile_RedirectsWithSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "resume",
                "resume.pdf",
                "application/pdf",
                "Dummy content".getBytes()
        );

        mockMvc.perform(multipart("/student/resume/upload")
                .file(file)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/profile"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(studentService).uploadResume(eq("student@college.edu"), any());
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void downloadResume_ReturnsFileResource() throws Exception {
        ResumeDownload download = new ResumeDownload(
                new ByteArrayResource("sample-pdf".getBytes()),
                "my_resume.pdf"
        );
        when(studentService.getResumeResource("student@college.edu")).thenReturn(download);

        mockMvc.perform(get("/student/resume/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"my_resume.pdf\""));
    }
}
