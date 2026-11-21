package com.campusconnect.student.controller;

import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.repository.StudentProfileRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student")
public class StudentDashboardController {

    private final StudentProfileRepository studentProfileRepository;
    private final com.campusconnect.announcement.service.AnnouncementService announcementService;

    public StudentDashboardController(StudentProfileRepository studentProfileRepository,
                                    com.campusconnect.announcement.service.AnnouncementService announcementService) {
        this.studentProfileRepository = studentProfileRepository;
        this.announcementService = announcementService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            studentProfileRepository.findByUserEmail(userDetails.getUsername())
                    .ifPresent(profile -> model.addAttribute("profile", profile));
            model.addAttribute("username", userDetails.getUsername());
        }
        model.addAttribute("pinnedAnnouncements", announcementService.getPinnedAnnouncementsForStudent());
        return "student/dashboard";
    }
}
