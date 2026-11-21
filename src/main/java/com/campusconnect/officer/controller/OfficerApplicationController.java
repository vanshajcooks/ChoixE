package com.campusconnect.officer.controller;

import com.campusconnect.application.entity.Application;
import com.campusconnect.application.entity.ApplicationStatus;
import com.campusconnect.application.service.ApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/officer/applications")
public class OfficerApplicationController {

    private final ApplicationService applicationService;
    private final com.campusconnect.interview.service.InterviewService interviewService;

    public OfficerApplicationController(ApplicationService applicationService,
                                      com.campusconnect.interview.service.InterviewService interviewService) {
        this.applicationService = applicationService;
        this.interviewService = interviewService;
    }

    @GetMapping
    public String listApplications(@RequestParam(value = "status", required = false) String statusParam,
                                   @RequestParam(value = "search", required = false) String search,
                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "size", defaultValue = "10") int size,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        ApplicationStatus status = null;
        if (statusParam != null && !statusParam.isBlank() && !"ALL".equalsIgnoreCase(statusParam)) {
            try {
                status = ApplicationStatus.valueOf(statusParam.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<Application> applications = applicationService.getOfficerApplications(status, search, pageable);

        model.addAttribute("applications", applications);
        model.addAttribute("currentStatus", status != null ? status.name() : "ALL");
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("username", userDetails.getUsername());

        return "officer/applications-list";
    }

    @GetMapping("/{id}")
    public String viewApplicationDetail(@PathVariable("id") Long id,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        Model model) {
        try {
            Application application = applicationService.getApplicationDetailForOfficer(id);
            model.addAttribute("app", application);
            model.addAttribute("application", application);
            model.addAttribute("interviews", interviewService.getInterviewsForApplication(id));
            model.addAttribute("username", userDetails.getUsername());
            return "officer/application-detail";
        } catch (IllegalArgumentException e) {
            return "redirect:/officer/applications";
        }
    }
}
