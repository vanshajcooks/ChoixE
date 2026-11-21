package com.campusconnect.interview.controller;

import com.campusconnect.application.entity.Application;
import com.campusconnect.application.service.ApplicationService;
import com.campusconnect.interview.dto.InterviewScheduleRequest;
import com.campusconnect.interview.dto.InterviewUpdateRequest;
import com.campusconnect.interview.entity.Interview;
import com.campusconnect.interview.entity.InterviewMode;
import com.campusconnect.interview.entity.InterviewStatus;
import com.campusconnect.interview.service.InterviewService;
import com.campusconnect.job.entity.Job;
import com.campusconnect.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RecruiterInterviewController {

    private final InterviewService interviewService;
    private final ApplicationService applicationService;
    private final JobService jobService;

    public RecruiterInterviewController(InterviewService interviewService,
                                        ApplicationService applicationService,
                                        JobService jobService) {
        this.interviewService = interviewService;
        this.applicationService = applicationService;
        this.jobService = jobService;
    }

    @GetMapping("/recruiter/interviews")
    public String listCompanyInterviews(@RequestParam(name = "jobId", required = false) Long jobId,
                                        @RequestParam(name = "status", required = false) String statusParam,
                                        @RequestParam(name = "page", defaultValue = "0") int page,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        Model model) {
        InterviewStatus status = parseStatus(statusParam);
        Pageable pageable = PageRequest.of(page, 15);
        Page<Interview> interviews = interviewService.getCompanyInterviews(userDetails.getUsername(), jobId, status, pageable);
        Page<Job> jobs = jobService.getCompanyJobs(userDetails.getUsername(), PageRequest.of(0, 100));

        model.addAttribute("interviews", interviews);
        model.addAttribute("jobs", jobs.getContent());
        model.addAttribute("currentJobId", jobId);
        model.addAttribute("currentStatus", status != null ? status.name() : "ALL");
        model.addAttribute("modes", InterviewMode.values());
        model.addAttribute("statuses", InterviewStatus.values());
        model.addAttribute("username", userDetails.getUsername());

        return "recruiter/interviews-list";
    }

    @GetMapping("/recruiter/applications/{appId}/interviews/new")
    public String showScheduleForm(@PathVariable("appId") Long appId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        try {
            Application application = applicationService.getApplicationDetailForRecruiter(userDetails.getUsername(), appId);
            if (application.getStatus().isTerminal()) {
                model.addAttribute("errorMessage", "Cannot schedule interview for application in terminal status: " + application.getStatus().getDisplayName());
                return "redirect:/recruiter/applications/" + appId;
            }

            model.addAttribute("app", application);
            model.addAttribute("interviewRequest", new InterviewScheduleRequest());
            model.addAttribute("modes", InterviewMode.values());
            model.addAttribute("username", userDetails.getUsername());
            return "recruiter/interview-form";
        } catch (IllegalArgumentException e) {
            return "redirect:/recruiter/applications";
        }
    }

    @PostMapping("/recruiter/applications/{appId}/interviews")
    public String scheduleInterview(@PathVariable("appId") Long appId,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    @Valid @ModelAttribute("interviewRequest") InterviewScheduleRequest request,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Application application;
        try {
            application = applicationService.getApplicationDetailForRecruiter(userDetails.getUsername(), appId);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/recruiter/applications";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("app", application);
            model.addAttribute("modes", InterviewMode.values());
            model.addAttribute("username", userDetails.getUsername());
            return "recruiter/interview-form";
        }

        try {
            interviewService.scheduleInterview(userDetails.getUsername(), appId, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Interview successfully scheduled and notification sent to candidate.");
            return "redirect:/recruiter/applications/" + appId;
        } catch (Exception e) {
            model.addAttribute("app", application);
            model.addAttribute("modes", InterviewMode.values());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("username", userDetails.getUsername());
            return "recruiter/interview-form";
        }
    }

    @GetMapping("/recruiter/interviews/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        try {
            Interview interview = interviewService.getInterviewDetailForRecruiter(userDetails.getUsername(), id);
            InterviewUpdateRequest form = new InterviewUpdateRequest();
            form.setRoundName(interview.getRoundName());
            form.setScheduledAt(interview.getScheduledAt());
            form.setMode(interview.getMode());
            form.setLocationOrLink(interview.getLocationOrLink());
            form.setInstructions(interview.getInstructions());
            form.setStatus(interview.getStatus());
            form.setFeedback(interview.getFeedback());

            model.addAttribute("interview", interview);
            model.addAttribute("app", interview.getApplication());
            model.addAttribute("interviewRequest", form);
            model.addAttribute("modes", InterviewMode.values());
            model.addAttribute("statuses", InterviewStatus.values());
            model.addAttribute("username", userDetails.getUsername());
            return "recruiter/interview-edit";
        } catch (IllegalArgumentException e) {
            return "redirect:/recruiter/interviews";
        }
    }

    @PostMapping("/recruiter/interviews/{id}")
    public String updateInterview(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  @Valid @ModelAttribute("interviewRequest") InterviewUpdateRequest request,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Interview interview;
        try {
            interview = interviewService.getInterviewDetailForRecruiter(userDetails.getUsername(), id);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/recruiter/interviews";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("interview", interview);
            model.addAttribute("app", interview.getApplication());
            model.addAttribute("modes", InterviewMode.values());
            model.addAttribute("statuses", InterviewStatus.values());
            model.addAttribute("username", userDetails.getUsername());
            return "recruiter/interview-edit";
        }

        try {
            interviewService.updateInterview(userDetails.getUsername(), id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Interview schedule updated successfully.");
            return "redirect:/recruiter/interviews";
        } catch (Exception e) {
            model.addAttribute("interview", interview);
            model.addAttribute("app", interview.getApplication());
            model.addAttribute("modes", InterviewMode.values());
            model.addAttribute("statuses", InterviewStatus.values());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("username", userDetails.getUsername());
            return "recruiter/interview-edit";
        }
    }

    private InterviewStatus parseStatus(String statusParam) {
        if (statusParam != null && !statusParam.isBlank() && !"ALL".equalsIgnoreCase(statusParam)) {
            try {
                return InterviewStatus.valueOf(statusParam.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }
}
