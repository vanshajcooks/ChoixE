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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               UserRepository userRepository,
                               NotificationService notificationService) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Announcement createAnnouncement(String officerEmail, AnnouncementRequest request) {
        User author = userRepository.findByEmail(officerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Author not found: " + officerEmail));

        Announcement announcement = new Announcement(
                request.getTitle().trim(),
                request.getContent().trim(),
                request.getTargetAudience(),
                request.getPriority(),
                request.isPinned(),
                request.isActive(),
                request.isBroadcastNotification(),
                author,
                request.getExpiresAt()
        );

        Announcement saved = announcementRepository.save(announcement);

        if (saved.isActive() && request.isBroadcastNotification()) {
            broadcastNotification(saved);
        }

        return saved;
    }

    @Transactional
    public Announcement updateAnnouncement(Long id, AnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found: " + id));

        announcement.setTitle(request.getTitle().trim());
        announcement.setContent(request.getContent().trim());
        announcement.setTargetAudience(request.getTargetAudience());
        announcement.setPriority(request.getPriority());
        announcement.setPinned(request.isPinned());
        announcement.setActive(request.isActive());
        announcement.setExpiresAt(request.getExpiresAt());

        return announcementRepository.save(announcement);
    }

    @Transactional
    public Announcement toggleStatus(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found: " + id));
        announcement.setActive(!announcement.isActive());
        return announcementRepository.save(announcement);
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new IllegalArgumentException("Announcement not found: " + id);
        }
        announcementRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Announcement getAnnouncementById(Long id) {
        return announcementRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Announcement> getAnnouncementsForStudent(Pageable pageable) {
        return announcementRepository.findActiveByAudience(
                List.of(TargetAudience.ALL, TargetAudience.STUDENTS_ONLY),
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<Announcement> getAnnouncementsForRecruiter(Pageable pageable) {
        return announcementRepository.findActiveByAudience(
                List.of(TargetAudience.ALL, TargetAudience.RECRUITERS_ONLY),
                pageable
        );
    }

    @Transactional(readOnly = true)
    public List<Announcement> getPinnedAnnouncementsForStudent() {
        return announcementRepository.findPinnedByAudience(
                List.of(TargetAudience.ALL, TargetAudience.STUDENTS_ONLY)
        );
    }

    @Transactional(readOnly = true)
    public List<Announcement> getPinnedAnnouncementsForRecruiter() {
        return announcementRepository.findPinnedByAudience(
                List.of(TargetAudience.ALL, TargetAudience.RECRUITERS_ONLY)
        );
    }

    @Transactional(readOnly = true)
    public Page<Announcement> getOfficerAnnouncements(Boolean active, TargetAudience audience, Pageable pageable) {
        return announcementRepository.findOfficerAnnouncements(active, audience, pageable);
    }

    @Transactional(readOnly = true)
    public long getActiveAnnouncementCount() {
        return announcementRepository.countByActiveTrue();
    }

    private void broadcastNotification(Announcement announcement) {
        List<Role> targetRoles = new ArrayList<>();
        if (announcement.getTargetAudience() == TargetAudience.ALL) {
            targetRoles.add(Role.STUDENT);
            targetRoles.add(Role.RECRUITER);
        } else if (announcement.getTargetAudience() == TargetAudience.STUDENTS_ONLY) {
            targetRoles.add(Role.STUDENT);
        } else if (announcement.getTargetAudience() == TargetAudience.RECRUITERS_ONLY) {
            targetRoles.add(Role.RECRUITER);
        }

        List<User> recipients = userRepository.findByRoleInAndStatus(targetRoles, UserStatus.ACTIVE);
        String prefix = announcement.getPriority() == AnnouncementPriority.URGENT ? "[URGENT] " :
                        announcement.getPriority() == AnnouncementPriority.HIGH ? "[IMPORTANT] " : "";
        String notifTitle = prefix + announcement.getTitle();
        String message = announcement.getContent().length() > 120 ?
                announcement.getContent().substring(0, 117) + "..." : announcement.getContent();

        for (User recipient : recipients) {
            notificationService.createNotification(
                    recipient,
                    notifTitle,
                    message,
                    NotificationType.ANNOUNCEMENT,
                    "/announcements/" + announcement.getId()
            );
        }
    }
}
