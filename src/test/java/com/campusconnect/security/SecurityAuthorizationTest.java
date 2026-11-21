package com.campusconnect.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedStudentDashboard_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void unauthenticatedRecruiterDashboard_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/recruiter/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void unauthenticatedOfficerDashboard_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/officer/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCanAccessStudentDashboard() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCannotAccessOfficerDashboard() throws Exception {
        mockMvc.perform(get("/officer/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCannotAccessRecruiterDashboard() throws Exception {
        mockMvc.perform(get("/recruiter/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void recruiterCanAccessRecruiterDashboard() throws Exception {
        mockMvc.perform(get("/recruiter/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void recruiterCannotAccessOfficerDashboard() throws Exception {
        mockMvc.perform(get("/officer/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void officerCanAccessOfficerDashboard() throws Exception {
        mockMvc.perform(get("/officer/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedStudentProfile_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/student/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void unauthenticatedOfficerStudents_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/officer/students"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCannotAccessOfficerStudents() throws Exception {
        mockMvc.perform(get("/officer/students"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void recruiterCannotAccessOfficerStudents() throws Exception {
        mockMvc.perform(get("/officer/students"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void officerCanAccessOfficerStudents() throws Exception {
        mockMvc.perform(get("/officer/students"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedOfficerCompanies_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/officer/companies"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCannotAccessOfficerCompanies() throws Exception {
        mockMvc.perform(get("/officer/companies"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void recruiterCannotAccessOfficerCompanies() throws Exception {
        mockMvc.perform(get("/officer/companies"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void officerCanAccessOfficerCompanies() throws Exception {
        mockMvc.perform(get("/officer/companies"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedRecruiterProfile_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/recruiter/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCannotAccessRecruiterProfile() throws Exception {
        mockMvc.perform(get("/recruiter/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedStudentJobs_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/student/jobs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCannotAccessOfficerJobs() throws Exception {
        mockMvc.perform(get("/officer/jobs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void recruiterCannotAccessOfficerJobs() throws Exception {
        mockMvc.perform(get("/officer/jobs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void officerCanAccessOfficerJobs() throws Exception {
        mockMvc.perform(get("/officer/jobs"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedRecruiterJobs_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/recruiter/jobs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCannotAccessRecruiterJobs() throws Exception {
        mockMvc.perform(get("/recruiter/jobs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedStudentApplications_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/student/applications"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void recruiterCannotAccessStudentApplications() throws Exception {
        mockMvc.perform(get("/student/applications"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRecruiterApplications_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/recruiter/applications"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCannotAccessRecruiterApplications() throws Exception {
        mockMvc.perform(get("/recruiter/applications"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedOfficerApplications_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/officer/applications"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCannotAccessOfficerApplications() throws Exception {
        mockMvc.perform(get("/officer/applications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void officerCanAccessOfficerApplications() throws Exception {
        mockMvc.perform(get("/officer/applications"))
                .andExpect(status().isOk());
    }

    // ==================== Module 6: Interviews & Notifications Security ====================

    @Test
    void unauthenticatedNotifications_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void officerCanAccessNotifications() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedStudentInterviews_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/student/interviews"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void recruiterCannotAccessStudentInterviews() throws Exception {
        mockMvc.perform(get("/student/interviews"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void officerCannotAccessStudentInterviews() throws Exception {
        mockMvc.perform(get("/student/interviews"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRecruiterInterviews_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/recruiter/interviews"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCannotAccessRecruiterInterviews() throws Exception {
        mockMvc.perform(get("/recruiter/interviews"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void officerCannotAccessRecruiterInterviews() throws Exception {
        mockMvc.perform(get("/recruiter/interviews"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedOfficerInterviews_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/officer/interviews"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCannotAccessOfficerInterviews() throws Exception {
        mockMvc.perform(get("/officer/interviews"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void recruiterCannotAccessOfficerInterviews() throws Exception {
        mockMvc.perform(get("/officer/interviews"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void officerCanAccessOfficerInterviews() throws Exception {
        mockMvc.perform(get("/officer/interviews"))
                .andExpect(status().isOk());
    }

    // ==================== Module 7: Announcements Security ====================

    @Test
    void unauthenticatedOfficerAnnouncements_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/officer/announcements"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCannotAccessOfficerAnnouncements() throws Exception {
        mockMvc.perform(get("/officer/announcements"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void recruiterCannotAccessOfficerAnnouncements() throws Exception {
        mockMvc.perform(get("/officer/announcements"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void officerCanAccessOfficerAnnouncements() throws Exception {
        mockMvc.perform(get("/officer/announcements"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedPublicAnnouncements_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/announcements"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCanAccessPublicAnnouncements() throws Exception {
        mockMvc.perform(get("/announcements"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void recruiterCanAccessPublicAnnouncements() throws Exception {
        mockMvc.perform(get("/announcements"))
                .andExpect(status().isOk());
    }

    // ==================== Module 8: Analytics Security ====================

    @Test
    void unauthenticatedOfficerAnalytics_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/officer/analytics"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCannotAccessOfficerAnalytics() throws Exception {
        mockMvc.perform(get("/officer/analytics"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void recruiterCannotAccessOfficerAnalytics() throws Exception {
        mockMvc.perform(get("/officer/analytics"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void officerCanAccessOfficerAnalytics() throws Exception {
        mockMvc.perform(get("/officer/analytics"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedStudentAnalytics_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/student/analytics"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void studentCanAccessStudentAnalytics() throws Exception {
        mockMvc.perform(get("/student/analytics"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void recruiterCannotAccessStudentAnalytics() throws Exception {
        mockMvc.perform(get("/student/analytics"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void officerCannotAccessStudentAnalytics() throws Exception {
        mockMvc.perform(get("/student/analytics"))
                .andExpect(status().isForbidden());
    }
}
