package com.campusconnect.application.controller;

import com.campusconnect.application.dto.ApplicationStatusUpdateRequest;
import com.campusconnect.application.entity.Application;
import com.campusconnect.application.entity.ApplicationStatus;
import com.campusconnect.application.exception.ApplicationStatusTransitionException;
import com.campusconnect.application.service.ApplicationService;
import com.campusconnect.job.entity.Job;
import com.campusconnect.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.campusconnect.common.storage.FileStorageService;
import com.campusconnect.student.entity.StudentProfile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
public class RecruiterApplicantController {

    private final ApplicationService applicationService;
    private final JobService jobService;
    private final FileStorageService fileStorageService;
    private final com.campusconnect.interview.service.InterviewService interviewService;

    public RecruiterApplicantController(ApplicationService applicationService,
                                        JobService jobService,
                                        FileStorageService fileStorageService,
                                        com.campusconnect.interview.service.InterviewService interviewService) {
        this.applicationService = applicationService;
        this.jobService = jobService;
        this.fileStorageService = fileStorageService;
        this.interviewService = interviewService;
    }

    @GetMapping("/recruiter/applications")
    public String listCompanyApplications(@RequestParam(value = "jobId", required = false) Long jobId,
                                          @RequestParam(value = "status", required = false) String statusParam,
                                          @RequestParam(value = "search", required = false) String search,
                                          @RequestParam(value = "page", defaultValue = "0") int page,
                                          @RequestParam(value = "size", defaultValue = "10") int size,
                                          @AuthenticationPrincipal UserDetails userDetails,
                                          Model model) {
        ApplicationStatus status = parseStatus(statusParam);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));

        Page<Application> applications = applicationService.getCompanyApplications(
                userDetails.getUsername(), jobId, status, search, pageable
        );
        Page<Job> jobs = jobService.getCompanyJobs(userDetails.getUsername(), Pageable.unpaged());

        model.addAttribute("applications", applications);
        model.addAttribute("jobs", jobs.getContent());
        model.addAttribute("currentJobId", jobId);
        model.addAttribute("currentStatus", status != null ? status.name() : "ALL");
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("username", userDetails.getUsername());

        return "recruiter/applicants-list";
    }

    @GetMapping("/recruiter/jobs/{jobId}/applicants")
    public String listJobApplicants(@PathVariable("jobId") Long jobId,
                                    @RequestParam(value = "status", required = false) String statusParam,
                                    @RequestParam(value = "search", required = false) String search,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {
        ApplicationStatus status = parseStatus(statusParam);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));

        try {
            Page<Application> applications = applicationService.getJobApplicants(
                    userDetails.getUsername(), jobId, status, search, pageable
            );
            Job job = jobService.getJobById(jobId);

            model.addAttribute("applications", applications);
            model.addAttribute("job", job);
            model.addAttribute("currentJobId", jobId);
            model.addAttribute("currentStatus", status != null ? status.name() : "ALL");
            model.addAttribute("search", search != null ? search : "");
            model.addAttribute("username", userDetails.getUsername());

            return "recruiter/applicants-list";
        } catch (IllegalArgumentException e) {
            return "redirect:/recruiter/jobs";
        }
    }

    @GetMapping("/recruiter/applications/{id}")
    public String viewApplicantDetail(@PathVariable("id") Long id,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      Model model) {
        try {
            Application application = applicationService.getApplicationDetailForRecruiter(userDetails.getUsername(), id);
            model.addAttribute("app", application);
            model.addAttribute("application", application);
            model.addAttribute("interviews", interviewService.getInterviewsForApplication(id));
            model.addAttribute("statusUpdateRequest", new ApplicationStatusUpdateRequest());
            model.addAttribute("username", userDetails.getUsername());
            return "recruiter/applicant-detail";
        } catch (IllegalArgumentException e) {
            return "redirect:/recruiter/applications";
        }
    }

    @PostMapping("/recruiter/applications/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("statusUpdateRequest") ApplicationStatusUpdateRequest request,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid status update parameters.");
            return "redirect:/recruiter/applications/" + id;
        }

        try {
            applicationService.updateApplicationStatus(
                    userDetails.getUsername(), id, request.getStatus(), request.getComments()
            );
            redirectAttributes.addFlashAttribute("successMessage",
                    "Application status successfully updated to " + request.getStatus().getDisplayName() + ".");
        } catch (ApplicationStatusTransitionException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/recruiter/applications/" + id;
    }

    @GetMapping("/recruiter/applications/{id}/resume")
    public ResponseEntity<Resource> downloadApplicantResume(@PathVariable("id") Long id,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        Application application = applicationService.getApplicationDetailForRecruiter(userDetails.getUsername(), id);
        StudentProfile student = application.getStudent();
        if (!student.hasResume()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = fileStorageService.loadFileAsResource(student.getResumeFileName());
        String originalFilename = student.getResumeOriginalName() != null ? student.getResumeOriginalName() : "resume.pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"")
                .body(resource);
    }

    private ApplicationStatus parseStatus(String statusParam) {
        if (statusParam != null && !statusParam.isBlank() && !"ALL".equalsIgnoreCase(statusParam)) {
            try {
                return ApplicationStatus.valueOf(statusParam.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }
}
