package com.campusconnect.officer.controller;

import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.entity.CompanyApprovalStatus;
import com.campusconnect.officer.service.OfficerCompanyService;
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
class OfficerCompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OfficerCompanyService officerCompanyService;

    private Company mockCompany;

    @BeforeEach
    void setUp() {
        User recruiter = new User();
        recruiter.setId(5L);
        recruiter.setFullName("Alexander Pierce");
        recruiter.setEmail("alex@pierceindustries.com");
        recruiter.setRole(Role.RECRUITER);

        mockCompany = new Company();
        mockCompany.setId(50L);
        mockCompany.setRecruiter(recruiter);
        mockCompany.setCompanyName("Pierce Industries");
        mockCompany.setIndustry("Manufacturing & Tech");
        mockCompany.setApprovalStatus(CompanyApprovalStatus.PENDING);
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void listCompanies_AsOfficer_ReturnsListView() throws Exception {
        when(officerCompanyService.getCompanies(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockCompany)));
        when(officerCompanyService.getPendingCount()).thenReturn(1L);

        mockMvc.perform(get("/officer/companies"))
                .andExpect(status().isOk())
                .andExpect(view().name("officer/companies-list"))
                .andExpect(model().attributeExists("companies"))
                .andExpect(model().attribute("pendingCount", 1L));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void listCompanies_AsStudent_Forbidden() throws Exception {
        mockMvc.perform(get("/officer/companies"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void listCompanies_AsRecruiter_Forbidden() throws Exception {
        mockMvc.perform(get("/officer/companies"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void viewCompanyDetail_AsOfficer_ReturnsDetailView() throws Exception {
        when(officerCompanyService.getCompanyById(50L)).thenReturn(mockCompany);

        mockMvc.perform(get("/officer/companies/50"))
                .andExpect(status().isOk())
                .andExpect(view().name("officer/company-detail"))
                .andExpect(model().attributeExists("company"))
                .andExpect(model().attributeExists("decisionRequest"));
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void approveCompany_ValidCall_RedirectsWithSuccess() throws Exception {
        mockMvc.perform(post("/officer/companies/50/approve")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/companies/50"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(officerCompanyService).approveCompany(50L, "officer@campusconnect.edu");
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void rejectCompany_WithReason_RedirectsWithWarning() throws Exception {
        mockMvc.perform(post("/officer/companies/50/reject")
                .with(csrf())
                .param("reason", "Company registration documents could not be authenticated."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/companies/50"))
                .andExpect(flash().attributeExists("warningMessage"));

        verify(officerCompanyService).rejectCompany(50L, "Company registration documents could not be authenticated.", "officer@campusconnect.edu");
    }
}
