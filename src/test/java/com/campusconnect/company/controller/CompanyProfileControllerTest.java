package com.campusconnect.company.controller;

import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.company.dto.CompanyProfileUpdateRequest;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.entity.CompanyApprovalStatus;
import com.campusconnect.company.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompanyProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    private Company mockCompany;

    @BeforeEach
    void setUp() {
        User recruiter = new User();
        recruiter.setId(10L);
        recruiter.setFullName("Emma Watson");
        recruiter.setEmail("recruiter@company.com");
        recruiter.setRole(Role.RECRUITER);

        mockCompany = new Company();
        mockCompany.setId(100L);
        mockCompany.setRecruiter(recruiter);
        mockCompany.setCompanyName("Watson Technologies");
        mockCompany.setIndustry("Software Engineering");
        mockCompany.setWebsite("https://watsontech.com");
        mockCompany.setLocation("Austin, TX");
        mockCompany.setContactNumber("+1 555-0144");
        mockCompany.setDescription("Pioneering modern enterprise applications.");
        mockCompany.setApprovalStatus(CompanyApprovalStatus.PENDING);
    }

    @Test
    @WithMockUser(username = "recruiter@company.com", roles = {"RECRUITER"})
    void viewProfile_AuthenticatedRecruiter_ReturnsProfileView() throws Exception {
        when(companyService.getCompanyByRecruiterEmail("recruiter@company.com")).thenReturn(mockCompany);

        mockMvc.perform(get("/recruiter/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/profile"))
                .andExpect(model().attributeExists("company"));
    }

    @Test
    void viewProfile_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/recruiter/profile"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void viewProfile_StudentRole_Forbidden() throws Exception {
        mockMvc.perform(get("/recruiter/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter@company.com", roles = {"RECRUITER"})
    void editProfileForm_ReturnsEditView() throws Exception {
        when(companyService.getCompanyByRecruiterEmail("recruiter@company.com")).thenReturn(mockCompany);

        mockMvc.perform(get("/recruiter/profile/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/profile-edit"))
                .andExpect(model().attributeExists("request"))
                .andExpect(model().attributeExists("company"));
    }

    @Test
    @WithMockUser(username = "recruiter@company.com", roles = {"RECRUITER"})
    void updateProfile_ValidData_RedirectsToProfileWithSuccess() throws Exception {
        when(companyService.getCompanyByRecruiterEmail("recruiter@company.com")).thenReturn(mockCompany);

        mockMvc.perform(post("/recruiter/profile/edit")
                .with(csrf())
                .param("companyName", "Watson Technologies Ltd")
                .param("industry", "Cloud Solutions")
                .param("website", "https://watsontech.io")
                .param("location", "Denver, CO")
                .param("contactNumber", "+1 555-0188")
                .param("description", "Updated cloud engineering services."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recruiter/profile"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(companyService).updateCompanyProfile(eq("recruiter@company.com"), any(CompanyProfileUpdateRequest.class));
    }

    @Test
    @WithMockUser(username = "recruiter@company.com", roles = {"RECRUITER"})
    void updateProfile_ValidationErrors_ReturnsFormWithErrors() throws Exception {
        when(companyService.getCompanyByRecruiterEmail("recruiter@company.com")).thenReturn(mockCompany);

        mockMvc.perform(post("/recruiter/profile/edit")
                .with(csrf())
                .param("companyName", "")
                .param("industry", "")
                .param("contactNumber", "invalid-phone!@#"))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/profile-edit"))
                .andExpect(model().hasErrors());
    }
}
