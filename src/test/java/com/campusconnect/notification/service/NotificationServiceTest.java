package com.campusconnect.notification.service;

import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.auth.repository.UserRepository;
import com.campusconnect.notification.entity.Notification;
import com.campusconnect.notification.entity.NotificationType;
import com.campusconnect.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User studentUser;
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        studentUser = new User();
        studentUser.setId(10L);
        studentUser.setEmail("student@college.edu");
        studentUser.setFullName("John Doe");
        studentUser.setRole(Role.STUDENT);

        mockNotification = new Notification(
                studentUser,
                "Interview Scheduled",
                "Your interview is on Friday.",
                NotificationType.INTERVIEW_SCHEDULED,
                "/student/interviews"
        );
        mockNotification.setId(100L);
    }

    @Test
    void createNotification_PersistsAndReturnsNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);

        Notification created = notificationService.createNotification(
                studentUser,
                "Interview Scheduled",
                "Your interview is on Friday.",
                NotificationType.INTERVIEW_SCHEDULED,
                "/student/interviews"
        );

        assertThat(created).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Interview Scheduled");
        assertThat(created.isRead()).isFalse();
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void getUserNotifications_ReturnsPagedNotifications() {
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(studentUser));
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(mockNotification)));

        Page<Notification> result = notificationService.getUserNotifications("student@college.edu", PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Interview Scheduled");
    }

    @Test
    void getUnreadCount_ReturnsCount() {
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(studentUser));
        when(notificationRepository.countByRecipientIdAndReadFalse(10L)).thenReturn(3L);

        long count = notificationService.getUnreadCount("student@college.edu");

        assertThat(count).isEqualTo(3L);
    }

    @Test
    void markAsRead_WhenOwner_SetsReadTrue() {
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(studentUser));
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(mockNotification));

        notificationService.markAsRead("student@college.edu", 100L);

        assertThat(mockNotification.isRead()).isTrue();
        verify(notificationRepository, times(1)).save(mockNotification);
    }

    @Test
    void markAsRead_WhenNotOwner_ThrowsException() {
        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setEmail("other@college.edu");

        when(userRepository.findByEmail("other@college.edu")).thenReturn(Optional.of(otherUser));
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(mockNotification));

        assertThatThrownBy(() -> notificationService.markAsRead("other@college.edu", 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void markAllAsRead_CallsRepositoryBulkUpdate() {
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(studentUser));

        notificationService.markAllAsRead("student@college.edu");

        verify(notificationRepository, times(1)).markAllAsReadByRecipientId(10L);
    }
}
