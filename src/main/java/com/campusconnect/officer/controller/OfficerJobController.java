package com.campusconnect.officer.controller;

import com.campusconnect.job.entity.Job;
import com.campusconnect.job.entity.JobStatus;
import com.campusconnect.job.service.JobService;
import com.campusconnect.officer.dto.JobDecisionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/officer/jobs")
public class OfficerJobController {

    private final JobService jobService;

    public OfficerJobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public String listJobs(@RequestParam(value = "status", required = false) String statusParam,
                           @RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "10") int size,
                           Model model) {
        JobStatus status = null;
        if (statusParam != null && !statusParam.isBlank() && !"ALL".equalsIgnoreCase(statusParam)) {
            try {
                status = JobStatus.valueOf(statusParam.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Page<Job> jobs = jobService.getOfficerJobs(
                status,
                search,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        model.addAttribute("jobs", jobs);
        model.addAttribute("currentStatus", status != null ? status.name() : "ALL");
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("pendingCount", jobService.getPendingJobCount());

        return "officer/jobs-list";
    }

    @GetMapping("/{id}")
    public String viewJobDetail(@PathVariable("id") Long id, Model model) {
        Job job = jobService.getJobById(id);
        model.addAttribute("job", job);
        model.addAttribute("decisionRequest", new JobDecisionRequest());
        return "officer/job-detail";
    }

    @PostMapping("/{id}/approve")
    public String approveJob(@PathVariable("id") Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            jobService.approveJob(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Job posting has been approved for publication.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/officer/jobs/" + id;
    }

    @PostMapping("/{id}/reject")
    public String rejectJob(@PathVariable("id") Long id,
                            @RequestParam("reason") String reason,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        try {
            jobService.rejectJob(id, reason, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("warningMessage", "Job posting has been marked as Rejected.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/officer/jobs/" + id;
    }
}
