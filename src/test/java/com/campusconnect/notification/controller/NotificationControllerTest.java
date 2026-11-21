package com.campusconnect.notification.controller;

import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.notification.entity.Notification;
import com.campusconnect.notification.entity.NotificationType;
import com.campusconnect.notification.service.NotificationService;
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
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(5L);
        user.setEmail("user@campusconnect.edu");
        user.setRole(Role.STUDENT);

        mockNotification = new Notification(
                user,
                "Welcome",
                "Account verified",
                NotificationType.GENERAL,
                "/student/profile"
        );
        mockNotification.setId(10L);
    }

    @Test
    @WithMockUser(username = "user@campusconnect.edu", roles = {"STUDENT"})
    void viewNotifications_ReturnsListView() throws Exception {
        when(notificationService.getUserNotifications(eq("user@campusconnect.edu"), any()))
                .thenReturn(new PageImpl<>(List.of(mockNotification)));

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications/list"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(model().attributeExists("username"));
    }

    @Test
    @WithMockUser(username = "user@campusconnect.edu", roles = {"STUDENT"})
    void markNotificationAsRead_Redirects() throws Exception {
        mockMvc.perform(post("/notifications/10/read")
                        .with(csrf())
                        .header("Referer", "/notifications"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(notificationService).markAsRead("user@campusconnect.edu", 10L);
    }

    @Test
    @WithMockUser(username = "user@campusconnect.edu", roles = {"STUDENT"})
    void markAllAsRead_RedirectsWithSuccess() throws Exception {
        mockMvc.perform(post("/notifications/read-all")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(notificationService).markAllAsRead("user@campusconnect.edu");
    }

    @Test
    void viewNotifications_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
