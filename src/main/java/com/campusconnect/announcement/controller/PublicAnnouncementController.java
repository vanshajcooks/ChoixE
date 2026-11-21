package com.campusconnect.announcement.controller;

import com.campusconnect.announcement.entity.Announcement;
import com.campusconnect.announcement.entity.TargetAudience;
import com.campusconnect.announcement.service.AnnouncementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PublicAnnouncementController {

    private final AnnouncementService announcementService;

    public PublicAnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping("/announcements")
    public String viewAnnouncements(@RequestParam(name = "page", defaultValue = "0") int page,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Authentication authentication,
                                    Model model) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PLACEMENT_OFFICER"))) {
            return "redirect:/officer/announcements";
        }

        Page<Announcement> announcements;
        boolean isRecruiter = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECRUITER"));

        if (isRecruiter) {
            announcements = announcementService.getAnnouncementsForRecruiter(PageRequest.of(page, 10));
        } else {
            announcements = announcementService.getAnnouncementsForStudent(PageRequest.of(page, 10));
        }

        model.addAttribute("announcements", announcements);
        model.addAttribute("username", userDetails != null ? userDetails.getUsername() : "");
        model.addAttribute("isRecruiter", isRecruiter);

        return "announcements/list";
    }

    @GetMapping("/announcements/{id}")
    public String viewAnnouncementDetail(@PathVariable("id") Long id,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         Authentication authentication,
                                         Model model) {
        try {
            Announcement announcement = announcementService.getAnnouncementById(id);

            boolean isOfficer = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PLACEMENT_OFFICER"));
            boolean isRecruiter = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_RECRUITER"));
            boolean isStudent = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));

            // Check access rules for non-officers
            if (!isOfficer) {
                if (!announcement.isActive()) {
                    return "redirect:/announcements";
                }
                if (isStudent && announcement.getTargetAudience() == TargetAudience.RECRUITERS_ONLY) {
                    return "redirect:/announcements";
                }
                if (isRecruiter && announcement.getTargetAudience() == TargetAudience.STUDENTS_ONLY) {
                    return "redirect:/announcements";
                }
            }

            model.addAttribute("announcement", announcement);
            model.addAttribute("username", userDetails != null ? userDetails.getUsername() : "");
            model.addAttribute("isOfficer", isOfficer);
            model.addAttribute("isRecruiter", isRecruiter);

            return "announcements/detail";
        } catch (IllegalArgumentException e) {
            return "redirect:/announcements";
        }
    }
}
