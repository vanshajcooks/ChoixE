package com.campusconnect.auth.controller;

import com.campusconnect.auth.dto.RecruiterRegisterRequest;
import com.campusconnect.auth.dto.StudentRegisterRequest;
import com.campusconnect.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void getLoginPage_ReturnsOk() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void getRegisterSelectPage_ReturnsOk() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-select"));
    }

    @Test
    void getStudentRegisterPage_ReturnsOk() throws Exception {
        mockMvc.perform(get("/register/student"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-student"))
                .andExpect(model().attributeExists("request"));
    }

    @Test
    void postStudentRegister_ValidData_RedirectsToLogin() throws Exception {
        mockMvc.perform(post("/register/student")
                .with(csrf())
                .param("fullName", "Alice Walker")
                .param("email", "alice@college.edu")
                .param("password", "SecretPass123")
                .param("confirmPassword", "SecretPass123")
                .param("rollNumber", "2022IT1020")
                .param("department", "Information Technology")
                .param("graduationYear", "2026")
                .param("cgpa", "8.90")
                .param("activeBacklogs", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        verify(authService).registerStudent(any(StudentRegisterRequest.class));
    }

    @Test
    void postStudentRegister_ValidationFailure_ReturnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/register/student")
                .with(csrf())
                .param("fullName", "")
                .param("email", "invalid-email")
                .param("password", "short")
                .param("confirmPassword", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-student"))
                .andExpect(model().hasErrors());
    }

    @Test
    void getRecruiterRegisterPage_ReturnsOk() throws Exception {
        mockMvc.perform(get("/register/recruiter"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-recruiter"))
                .andExpect(model().attributeExists("request"));
    }

    @Test
    void postRecruiterRegister_ValidData_RedirectsToLogin() throws Exception {
        mockMvc.perform(post("/register/recruiter")
                .with(csrf())
                .param("fullName", "Robert Downey")
                .param("email", "robert@globaltech.com")
                .param("password", "SecurePassword123")
                .param("confirmPassword", "SecurePassword123")
                .param("companyName", "GlobalTech Solutions")
                .param("industry", "Information Technology")
                .param("website", "https://globaltech.com")
                .param("description", "Global tech firm"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        verify(authService).registerRecruiter(any(RecruiterRegisterRequest.class));
    }
}
