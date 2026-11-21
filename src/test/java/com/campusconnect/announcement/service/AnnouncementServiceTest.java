package com.campusconnect.announcement.service;

import com.campusconnect.announcement.dto.AnnouncementRequest;
import com.campusconnect.announcement.entity.Announcement;
import com.campusconnect.announcement.entity.AnnouncementPriority;
import com.campusconnect.announcement.entity.TargetAudience;
import com.campusconnect.announcement.repository.AnnouncementRepository;
import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.auth.entity.UserStatus;
import com.campusconnect.auth.repository.UserRepository;
import com.campusconnect.notification.entity.NotificationType;
import com.campusconnect.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AnnouncementService announcementService;

    private User officerUser;
    private Announcement announcement;

    @BeforeEach
    void setUp() {
        officerUser = new User();
        officerUser.setId(1L);
        officerUser.setEmail("officer@campusconnect.edu");
        officerUser.setFullName("Placement Officer");
        officerUser.setRole(Role.PLACEMENT_OFFICER);

        announcement = new Announcement(
                "Placement Drive Guidelines",
                "Mandatory orientation for all final year students.",
                TargetAudience.ALL,
                AnnouncementPriority.HIGH,
                true,
                true,
                false,
                officerUser,
                LocalDateTime.now().plusDays(10)
        );
        announcement.setId(100L);
    }

    @Test
    void createAnnouncement_Success_WithoutBroadcast() {
        AnnouncementRequest request = new AnnouncementRequest(
                "Placement Drive Guidelines",
                "Mandatory orientation for all final year students.",
                TargetAudience.ALL,
                AnnouncementPriority.HIGH,
                true,
                true,
                false,
                LocalDateTime.now().plusDays(10)
        );

        when(userRepository.findByEmail("officer@campusconnect.edu")).thenReturn(Optional.of(officerUser));
        when(announcementRepository.save(any(Announcement.class))).thenAnswer(inv -> {
            Announcement a = inv.getArgument(0);
            a.setId(101L);
            return a;
        });

        Announcement result = announcementService.createAnnouncement("officer@campusconnect.edu", request);

        assertNotNull(result);
        assertEquals("Placement Drive Guidelines", result.getTitle());
        assertEquals(TargetAudience.ALL, result.getTargetAudience());
        assertEquals(AnnouncementPriority.HIGH, result.getPriority());
        assertTrue(result.isPinned());
        assertTrue(result.isActive());
        verify(announcementRepository, times(1)).save(any(Announcement.class));
        verifyNoInteractions(notificationService);
    }

    @Test
    void createAnnouncement_WithBroadcast_DispatchesNotifications() {
        AnnouncementRequest request = new AnnouncementRequest(
                "Urgent Drive Rescheduled",
                "Google interview process has been rescheduled to Monday.",
                TargetAudience.ALL,
                AnnouncementPriority.URGENT,
                true,
                true,
                true,
                LocalDateTime.now().plusDays(2)
        );

        User student1 = new User();
        student1.setId(2L);
        student1.setEmail("student1@college.edu");

        User recruiter1 = new User();
        recruiter1.setId(3L);
        recruiter1.setEmail("recruiter@google.com");

        when(userRepository.findByEmail("officer@campusconnect.edu")).thenReturn(Optional.of(officerUser));
        when(announcementRepository.save(any(Announcement.class))).thenAnswer(inv -> {
            Announcement a = inv.getArgument(0);
            a.setId(102L);
            return a;
        });
        when(userRepository.findByRoleInAndStatus(anyCollection(), eq(UserStatus.ACTIVE)))
                .thenReturn(List.of(student1, recruiter1));

        Announcement result = announcementService.createAnnouncement("officer@campusconnect.edu", request);

        assertNotNull(result);
        verify(userRepository, times(1)).findByRoleInAndStatus(argThat(roles ->
                roles.contains(Role.STUDENT) && roles.contains(Role.RECRUITER)), eq(UserStatus.ACTIVE));
        verify(notificationService, times(2)).createNotification(
                any(User.class),
                contains("Urgent Drive Rescheduled"),
                anyString(),
                eq(NotificationType.ANNOUNCEMENT),
                eq("/announcements/102")
        );
    }

    @Test
    void updateAnnouncement_Success() {
        AnnouncementRequest updateRequest = new AnnouncementRequest(
                "Updated Guidelines",
                "New instructions for online rounds.",
                TargetAudience.STUDENTS_ONLY,
                AnnouncementPriority.NORMAL,
                false,
                true,
                false,
                null
        );

        when(announcementRepository.findById(100L)).thenReturn(Optional.of(announcement));
        when(announcementRepository.save(any(Announcement.class))).thenAnswer(inv -> inv.getArgument(0));

        Announcement updated = announcementService.updateAnnouncement(100L, updateRequest);

        assertEquals("Updated Guidelines", updated.getTitle());
        assertEquals(TargetAudience.STUDENTS_ONLY, updated.getTargetAudience());
        assertEquals(AnnouncementPriority.NORMAL, updated.getPriority());
        assertFalse(updated.isPinned());
    }

    @Test
    void updateAnnouncement_NotFound_ThrowsException() {
        AnnouncementRequest updateRequest = new AnnouncementRequest();
        when(announcementRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                announcementService.updateAnnouncement(999L, updateRequest));
    }

    @Test
    void toggleStatus_FlipsActiveFlag() {
        when(announcementRepository.findById(100L)).thenReturn(Optional.of(announcement));
        when(announcementRepository.save(any(Announcement.class))).thenAnswer(inv -> inv.getArgument(0));

        assertTrue(announcement.isActive());
        Announcement toggled1 = announcementService.toggleStatus(100L);
        assertFalse(toggled1.isActive());

        Announcement toggled2 = announcementService.toggleStatus(100L);
        assertTrue(toggled2.isActive());
    }

    @Test
    void deleteAnnouncement_Success() {
        when(announcementRepository.existsById(100L)).thenReturn(true);
        doNothing().when(announcementRepository).deleteById(100L);

        announcementService.deleteAnnouncement(100L);

        verify(announcementRepository, times(1)).deleteById(100L);
    }

    @Test
    void deleteAnnouncement_NotFound_ThrowsException() {
        when(announcementRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                announcementService.deleteAnnouncement(999L));
    }

    @Test
    void getAnnouncementsForStudent_ReturnsOnlyAllAndStudentsOnly() {
        Pageable pageable = PageRequest.of(0, 10);
        when(announcementRepository.findActiveByAudience(argThat(c ->
                c.contains(TargetAudience.ALL) && c.contains(TargetAudience.STUDENTS_ONLY) && !c.contains(TargetAudience.RECRUITERS_ONLY)), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(announcement)));

        Page<Announcement> page = announcementService.getAnnouncementsForStudent(pageable);

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void getAnnouncementsForRecruiter_ReturnsOnlyAllAndRecruitersOnly() {
        Pageable pageable = PageRequest.of(0, 10);
        when(announcementRepository.findActiveByAudience(argThat(c ->
                c.contains(TargetAudience.ALL) && c.contains(TargetAudience.RECRUITERS_ONLY) && !c.contains(TargetAudience.STUDENTS_ONLY)), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(announcement)));

        Page<Announcement> page = announcementService.getAnnouncementsForRecruiter(pageable);

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void getPinnedAnnouncementsForStudent_Success() {
        when(announcementRepository.findPinnedByAudience(anyCollection()))
                .thenReturn(List.of(announcement));

        List<Announcement> pinned = announcementService.getPinnedAnnouncementsForStudent();

        assertNotNull(pinned);
        assertEquals(1, pinned.size());
    }

    @Test
    void getOfficerAnnouncements_Success() {
        Pageable pageable = PageRequest.of(0, 15);
        when(announcementRepository.findOfficerAnnouncements(eq(true), eq(TargetAudience.ALL), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(announcement)));

        Page<Announcement> page = announcementService.getOfficerAnnouncements(true, TargetAudience.ALL, pageable);

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void getAnnouncementById_Success() {
        when(announcementRepository.findByIdWithAuthor(100L)).thenReturn(Optional.of(announcement));

        Announcement found = announcementService.getAnnouncementById(100L);

        assertNotNull(found);
        assertEquals(100L, found.getId());
    }

    @Test
    void getAnnouncementById_NotFound_ThrowsException() {
        when(announcementRepository.findByIdWithAuthor(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                announcementService.getAnnouncementById(999L));
    }
}
