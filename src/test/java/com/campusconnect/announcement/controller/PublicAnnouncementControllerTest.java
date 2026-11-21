package com.campusconnect.announcement.controller;

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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicAnnouncementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnnouncementService announcementService;

    private Announcement publicAnnouncement;
    private Announcement recruiterOnlyAnnouncement;
    private Announcement studentOnlyAnnouncement;
    private Announcement inactiveAnnouncement;

    @BeforeEach
    void setUp() {
        User officer = new User();
        officer.setId(1L);
        officer.setEmail("officer@campusconnect.edu");
        officer.setFullName("Placement Officer");
        officer.setRole(Role.PLACEMENT_OFFICER);

        publicAnnouncement = new Announcement(
                "Campus Drive 2026 Orientation",
                "Mandatory orientation for all candidates and companies.",
                TargetAudience.ALL,
                AnnouncementPriority.HIGH,
                true,
                true,
                false,
                officer,
                LocalDateTime.now().plusDays(5)
        );
        publicAnnouncement.setId(10L);

        recruiterOnlyAnnouncement = new Announcement(
                "Recruiter Hospitality & Interview Slot Guidelines",
                "Instructions for recruiters visiting campus.",
                TargetAudience.RECRUITERS_ONLY,
                AnnouncementPriority.NORMAL,
                false,
                true,
                false,
                officer,
                LocalDateTime.now().plusDays(5)
        );
        recruiterOnlyAnnouncement.setId(20L);

        studentOnlyAnnouncement = new Announcement(
                "Resume Formatting Guidelines",
                "How to prepare resumes for drives.",
                TargetAudience.STUDENTS_ONLY,
                AnnouncementPriority.NORMAL,
                false,
                true,
                false,
                officer,
                LocalDateTime.now().plusDays(5)
        );
        studentOnlyAnnouncement.setId(30L);

        inactiveAnnouncement = new Announcement(
                "Old Announcement",
                "Expired notice.",
                TargetAudience.ALL,
                AnnouncementPriority.NORMAL,
                false,
                false,
                false,
                officer,
                null
        );
        inactiveAnnouncement.setId(40L);
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void viewAnnouncements_AsStudent_ReturnsStudentAnnouncements() throws Exception {
        when(announcementService.getAnnouncementsForStudent(any()))
                .thenReturn(new PageImpl<>(List.of(publicAnnouncement, studentOnlyAnnouncement)));

        mockMvc.perform(get("/announcements"))
                .andExpect(status().isOk())
                .andExpect(view().name("announcements/list"))
                .andExpect(model().attributeExists("announcements"))
                .andExpect(model().attribute("isRecruiter", false));
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void viewAnnouncements_AsRecruiter_ReturnsRecruiterAnnouncements() throws Exception {
        when(announcementService.getAnnouncementsForRecruiter(any()))
                .thenReturn(new PageImpl<>(List.of(publicAnnouncement, recruiterOnlyAnnouncement)));

        mockMvc.perform(get("/announcements"))
                .andExpect(status().isOk())
                .andExpect(view().name("announcements/list"))
                .andExpect(model().attributeExists("announcements"))
                .andExpect(model().attribute("isRecruiter", true));
    }

    @Test
    @WithMockUser(username = "officer@campusconnect.edu", roles = {"PLACEMENT_OFFICER"})
    void viewAnnouncements_AsOfficer_RedirectsToOfficerAnnouncements() throws Exception {
        mockMvc.perform(get("/announcements"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/announcements"));
    }

    @Test
    void viewAnnouncements_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/announcements"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void viewAnnouncementDetail_AsStudent_Success() throws Exception {
        when(announcementService.getAnnouncementById(10L)).thenReturn(publicAnnouncement);

        mockMvc.perform(get("/announcements/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("announcements/detail"))
                .andExpect(model().attributeExists("announcement"))
                .andExpect(model().attribute("isOfficer", false));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void viewAnnouncementDetail_StudentAccessingRecruiterOnly_Redirects() throws Exception {
        when(announcementService.getAnnouncementById(20L)).thenReturn(recruiterOnlyAnnouncement);

        mockMvc.perform(get("/announcements/20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/announcements"));
    }

    @Test
    @WithMockUser(username = "recruiter@corp.com", roles = {"RECRUITER"})
    void viewAnnouncementDetail_RecruiterAccessingStudentOnly_Redirects() throws Exception {
        when(announcementService.getAnnouncementById(30L)).thenReturn(studentOnlyAnnouncement);

        mockMvc.perform(get("/announcements/30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/announcements"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void viewAnnouncementDetail_InactiveAnnouncement_Redirects() throws Exception {
        when(announcementService.getAnnouncementById(40L)).thenReturn(inactiveAnnouncement);

        mockMvc.perform(get("/announcements/40"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/announcements"));
    }

    @Test
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void viewAnnouncementDetail_NotFound_Redirects() throws Exception {
        when(announcementService.getAnnouncementById(999L)).thenThrow(new IllegalArgumentException("Not found"));

        mockMvc.perform(get("/announcements/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/announcements"));
    }
}
