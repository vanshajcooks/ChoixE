package com.campusconnect.application.controller;

import com.campusconnect.application.entity.Application;
import com.campusconnect.application.service.ApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StudentApplicationController {

    private final ApplicationService applicationService;
    private final com.campusconnect.interview.service.InterviewService interviewService;

    public StudentApplicationController(ApplicationService applicationService,
                                      com.campusconnect.interview.service.InterviewService interviewService) {
        this.applicationService = applicationService;
        this.interviewService = interviewService;
    }

    @PostMapping("/student/jobs/{jobId}/apply")
    public String applyToJob(@PathVariable("jobId") Long jobId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            Application application = applicationService.applyToJob(userDetails.getUsername(), jobId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Application submitted successfully for " + application.getJob().getTitle() + " at "
                            + application.getJob().getCompany().getCompanyName() + "!");
            return "redirect:/student/applications/" + application.getId();
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/jobs/" + jobId;
        }
    }

    @GetMapping("/student/applications")
    public String listApplications(@RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "size", defaultValue = "10") int size,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        Page<Application> applications = applicationService.getStudentApplications(
                userDetails.getUsername(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"))
        );

        model.addAttribute("applications", applications);
        model.addAttribute("username", userDetails.getUsername());
        return "student/applications-list";
    }

    @GetMapping("/student/applications/{id}")
    public String viewApplicationDetail(@PathVariable("id") Long id,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        Model model) {
        try {
            Application application = applicationService.getStudentApplicationDetail(userDetails.getUsername(), id);
            model.addAttribute("app", application);
            model.addAttribute("application", application);
            model.addAttribute("interviews", interviewService.getInterviewsForApplication(id));
            model.addAttribute("username", userDetails.getUsername());
            return "student/application-detail";
        } catch (IllegalArgumentException e) {
            return "redirect:/student/applications";
        }
    }

    @PostMapping("/student/applications/{id}/withdraw")
    public String withdrawApplication(@PathVariable("id") Long id,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        try {
            applicationService.withdrawApplication(userDetails.getUsername(), id);
            redirectAttributes.addFlashAttribute("warningMessage", "Your application has been marked as Withdrawn.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/applications/" + id;
    }
}
