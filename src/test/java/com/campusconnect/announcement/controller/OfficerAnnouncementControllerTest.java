package com.campusconnect.announcement.controller;

import com.campusconnect.announcement.dto.AnnouncementRequest;
import com.campusconnect.announcement.entity.Announcement;
import com.campusconnect.announcement.entity.AnnouncementPriority;
import com.campusconnect.announcement.entity.TargetAudience;
import com.campusconnect.announcement.service.AnnouncementService;
import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OfficerAnnouncementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnnouncementService announcementService;

    private Announcement mockAnnouncement;

    @BeforeEach
    void setUp() {
        User officer = new User();
        officer.setId(1L);
        officer.setEmail("officer@campusconnect.edu");
        officer.setFullName("Placement Officer");
        officer.setRole(Role.PLACEMENT_OFFICER);

        mockAnnouncement = new Announcement(
                "Final Year Placement Guidelines",
                "Detailed instructions for company drives.",
                TargetAudience.ALL,
                AnnouncementPriority.HIGH,
                true,
                true,
                false,
                officer,
                LocalDateTime.now().plusDays(7)
        );
        mockAnnouncement.setId(50L);
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void listAnnouncements_AsOfficer_ReturnsListView() throws Exception {
        when(announcementService.getOfficerAnnouncements(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockAnnouncement)));

        mockMvc.perform(get("/officer/announcements"))
                .andExpect(status().isOk())
                .andExpect(view().name("officer/announcements-list"))
                .andExpect(model().attributeExists("announcements"))
                .andExpect(model().attributeExists("audiences"))
                .andExpect(model().attributeExists("priorities"));
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void showCreateForm_AsOfficer_ReturnsForm() throws Exception {
        mockMvc.perform(get("/officer/announcements/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("officer/announcement-form"))
                .andExpect(model().attributeExists("announcementRequest"))
                .andExpect(model().attribute("isEdit", false));
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void createAnnouncement_ValidInput_Redirects() throws Exception {
        when(announcementService.createAnnouncement(eq("officer@campusconnect.edu"), any(AnnouncementRequest.class)))
                .thenReturn(mockAnnouncement);

        mockMvc.perform(post("/officer/announcements")
                        .with(csrf())
                        .param("title", "New Drive Alert")
                        .param("content", "Amazon hiring drive starts next week.")
                        .param("targetAudience", "STUDENTS_ONLY")
                        .param("priority", "HIGH")
                        .param("pinned", "true")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/announcements"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(announcementService).createAnnouncement(eq("officer@campusconnect.edu"), any(AnnouncementRequest.class));
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void createAnnouncement_InvalidInput_ReturnsForm() throws Exception {
        mockMvc.perform(post("/officer/announcements")
                        .with(csrf())
                        .param("title", "")
                        .param("content", "")
                        .param("targetAudience", "ALL")
                        .param("priority", "NORMAL"))
                .andExpect(status().isOk())
                .andExpect(view().name("officer/announcement-form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void showEditForm_AsOfficer_ReturnsForm() throws Exception {
        when(announcementService.getAnnouncementById(50L)).thenReturn(mockAnnouncement);

        mockMvc.perform(get("/officer/announcements/50/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("officer/announcement-form"))
                .andExpect(model().attributeExists("announcementRequest"))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attribute("announcementId", 50L));
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void updateAnnouncement_ValidInput_Redirects() throws Exception {
        when(announcementService.updateAnnouncement(eq(50L), any(AnnouncementRequest.class)))
                .thenReturn(mockAnnouncement);

        mockMvc.perform(post("/officer/announcements/50")
                        .with(csrf())
                        .param("title", "Updated Drive Alert")
                        .param("content", "Updated instructions for all applicants.")
                        .param("targetAudience", "ALL")
                        .param("priority", "URGENT")
                        .param("pinned", "true")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/announcements"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(announcementService).updateAnnouncement(eq(50L), any(AnnouncementRequest.class));
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void toggleStatus_Redirects() throws Exception {
        when(announcementService.toggleStatus(50L)).thenReturn(mockAnnouncement);

        mockMvc.perform(post("/officer/announcements/50/toggle")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/announcements"));

        verify(announcementService).toggleStatus(50L);
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void deleteAnnouncement_Redirects() throws Exception {
        mockMvc.perform(post("/officer/announcements/50/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/announcements"));

        verify(announcementService).deleteAnnouncement(50L);
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void listAnnouncements_AsStudent_Forbidden() throws Exception {
        mockMvc.perform(get("/officer/announcements"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void listAnnouncements_AsRecruiter_Forbidden() throws Exception {
        mockMvc.perform(get("/officer/announcements"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listAnnouncements_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/officer/announcements"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
