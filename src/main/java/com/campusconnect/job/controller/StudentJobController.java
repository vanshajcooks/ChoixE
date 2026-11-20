package com.campusconnect.job.controller;

import com.campusconnect.job.service.JobService;
import com.campusconnect.job.service.JobService.StudentJobItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/student/jobs")
public class StudentJobController {

    private final JobService jobService;

    public StudentJobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public String listJobs(@RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "9") int size,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        Page<StudentJobItem> jobs = jobService.getStudentJobs(
                userDetails.getUsername(),
                search,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "applicationCloseDate"))
        );

        model.addAttribute("jobs", jobs);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("username", userDetails.getUsername());

        return "student/jobs-list";
    }

    @GetMapping("/{id}")
    public String viewJobDetail(@PathVariable("id") Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        try {
            StudentJobItem item = jobService.getStudentJobDetail(userDetails.getUsername(), id);
            model.addAttribute("job", item.job());
            model.addAttribute("eligibility", item.eligibility());
            model.addAttribute("username", userDetails.getUsername());
            return "student/job-detail";
        } catch (IllegalArgumentException e) {
            return "redirect:/student/jobs";
        }
    }
}
