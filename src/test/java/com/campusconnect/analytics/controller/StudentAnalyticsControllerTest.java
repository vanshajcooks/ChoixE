package com.campusconnect.analytics.controller;

import com.campusconnect.analytics.dto.*;
import com.campusconnect.analytics.service.PlacementAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlacementAnalyticsService placementAnalyticsService;

    private PlacementAnalyticsReport mockReport;

    @BeforeEach
    void setUp() {
        PlacementOverviewStats overview = new PlacementOverviewStats(
                100L, 80L, 64L, 80.0, 64.0, 200L, 70L, 10L, 5L
        );
        List<DepartmentPlacementStat> deptStats = List.of(
                new DepartmentPlacementStat("Computer Science", 50L, 40L, 35L, 87.5)
        );
        List<CompanyPlacementStat> compStats = List.of(
                new CompanyPlacementStat("Google", "Technology", 10L, 8L)
        );
        SalaryPackageStats salaryStats = new SalaryPackageStats(
                20.0, 11.5, 10.0, 4, 3, 1, 8
        );

        mockReport = new PlacementAnalyticsReport(overview, deptStats, compStats, salaryStats);
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void viewCollegeAnalytics_AsStudent_ReturnsAnalyticsView() throws Exception {
        when(placementAnalyticsService.getPlacementAnalyticsReport()).thenReturn(mockReport);

        mockMvc.perform(get("/student/analytics"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/analytics"))
                .andExpect(model().attributeExists("report"))
                .andExpect(model().attributeExists("overview"))
                .andExpect(model().attributeExists("departmentStats"))
                .andExpect(model().attributeExists("companyStats"))
                .andExpect(model().attributeExists("salaryStats"));
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void viewCollegeAnalytics_AsRecruiter_Forbidden() throws Exception {
        mockMvc.perform(get("/student/analytics"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void viewCollegeAnalytics_AsOfficer_Forbidden() throws Exception {
        mockMvc.perform(get("/student/analytics"))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewCollegeAnalytics_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/student/analytics"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
