package com.campusconnect.job.controller;

import com.campusconnect.company.entity.Company;
import com.campusconnect.company.service.CompanyService;
import com.campusconnect.job.dto.JobCreateRequest;
import com.campusconnect.job.entity.Job;
import com.campusconnect.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;

@Controller
@RequestMapping("/recruiter/jobs")
public class RecruiterJobController {

    private final JobService jobService;
    private final CompanyService companyService;

    public RecruiterJobController(JobService jobService, CompanyService companyService) {
        this.jobService = jobService;
        this.companyService = companyService;
    }

    @GetMapping
    public String listJobs(@RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "10") int size,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        Page<Job> jobs = jobService.getCompanyJobs(
                userDetails.getUsername(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        Company company = companyService.getCompanyByRecruiterEmail(userDetails.getUsername());

        model.addAttribute("jobs", jobs);
        model.addAttribute("company", company);
        model.addAttribute("username", userDetails.getUsername());

        return "recruiter/jobs-list";
    }

    @GetMapping("/new")
    public String newJobForm(@AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Company company = companyService.getCompanyByRecruiterEmail(userDetails.getUsername());
        if (!company.isApproved()) {
            redirectAttributes.addFlashAttribute("warningMessage",
                    "Your organization profile must be approved by the Placement Officer before you can post jobs.");
            return "redirect:/recruiter/dashboard";
        }

        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new JobCreateRequest());
        }
        model.addAttribute("company", company);
        return "recruiter/job-form";
    }

    @PostMapping("/new")
    public String createJob(@Valid @ModelAttribute("request") JobCreateRequest request,
                            BindingResult result,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (!request.isDateRangeValid()) {
            result.rejectValue("applicationCloseDate", "error.dateRange", "Closing date cannot be earlier than opening date.");
        }

        if (result.hasErrors()) {
            Company company = companyService.getCompanyByRecruiterEmail(userDetails.getUsername());
            model.addAttribute("company", company);
            return "recruiter/job-form";
        }

        try {
            Job job = jobService.createJob(userDetails.getUsername(), request);
            if (job.isPendingApproval()) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Job posting created and submitted for Placement Officer review.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Job draft saved successfully. You can edit and submit it whenever ready.");
            }
            return "redirect:/recruiter/jobs/" + job.getId();
        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            Company company = companyService.getCompanyByRecruiterEmail(userDetails.getUsername());
            model.addAttribute("company", company);
            return "recruiter/job-form";
        }
    }

    @GetMapping("/{id}")
    public String viewJob(@PathVariable("id") Long id,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        Job job = jobService.getJobById(id);
        if (!job.getCompany().getRecruiter().getEmail().equalsIgnoreCase(userDetails.getUsername())) {
            return "redirect:/recruiter/jobs";
        }

        model.addAttribute("job", job);
        model.addAttribute("username", userDetails.getUsername());
        return "recruiter/job-detail";
    }

    @GetMapping("/{id}/edit")
    public String editJobForm(@PathVariable("id") Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        Job job = jobService.getJobById(id);
        if (!job.getCompany().getRecruiter().getEmail().equalsIgnoreCase(userDetails.getUsername())) {
            return "redirect:/recruiter/jobs";
        }

        if (!job.isEditable()) {
            redirectAttributes.addFlashAttribute("warningMessage", "Job posting cannot be modified in status: " + job.getStatus());
            return "redirect:/recruiter/jobs/" + id;
        }

        if (!model.containsAttribute("request")) {
            JobCreateRequest request = new JobCreateRequest();
            request.setTitle(job.getTitle());
            request.setRoleType(job.getRoleType());
            request.setWorkMode(job.getWorkMode());
            request.setLocation(job.getLocation());
            request.setSalaryPackage(job.getSalaryPackage());
            request.setDescription(job.getDescription());
            request.setApplicationOpenDate(job.getApplicationOpenDate());
            request.setApplicationCloseDate(job.getApplicationCloseDate());

            if (job.getEligibilityCriteria() != null) {
                request.setMinCgpa(job.getEligibilityCriteria().getMinCgpa());
                request.setMaxBacklogs(job.getEligibilityCriteria().getMaxBacklogs());
                request.setGraduationYear(job.getEligibilityCriteria().getGraduationYear());
                request.setEligibleBranches(new ArrayList<>(job.getEligibilityCriteria().getEligibleBranches()));
            }

            model.addAttribute("request", request);
        }

        model.addAttribute("job", job);
        model.addAttribute("company", job.getCompany());
        return "recruiter/job-form";
    }

    @PostMapping("/{id}/edit")
    public String updateJob(@PathVariable("id") Long id,
                            @Valid @ModelAttribute("request") JobCreateRequest request,
                            BindingResult result,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (!request.isDateRangeValid()) {
            result.rejectValue("applicationCloseDate", "error.dateRange", "Closing date cannot be earlier than opening date.");
        }

        if (result.hasErrors()) {
            Job job = jobService.getJobById(id);
            model.addAttribute("job", job);
            model.addAttribute("company", job.getCompany());
            return "recruiter/job-form";
        }

        try {
            Job updated = jobService.updateJob(userDetails.getUsername(), id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Job posting updated successfully.");
            return "redirect:/recruiter/jobs/" + updated.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            Job job = jobService.getJobById(id);
            model.addAttribute("job", job);
            model.addAttribute("company", job.getCompany());
            return "recruiter/job-form";
        }
    }

    @PostMapping("/{id}/submit")
    public String submitJob(@PathVariable("id") Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        try {
            jobService.submitJobForApproval(userDetails.getUsername(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Job submitted for Placement Officer review.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/recruiter/jobs/" + id;
    }
}
