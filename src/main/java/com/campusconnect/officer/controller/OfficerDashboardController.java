package com.campusconnect.officer.controller;

import com.campusconnect.job.service.JobService;
import com.campusconnect.officer.service.OfficerCompanyService;
import com.campusconnect.officer.service.OfficerStudentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/officer")
public class OfficerDashboardController {

    private final OfficerStudentService officerStudentService;
    private final OfficerCompanyService officerCompanyService;
    private final JobService jobService;

    public OfficerDashboardController(OfficerStudentService officerStudentService,
                                    OfficerCompanyService officerCompanyService,
                                    JobService jobService) {
        this.officerStudentService = officerStudentService;
        this.officerCompanyService = officerCompanyService;
        this.jobService = jobService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        }
        model.addAttribute("pendingStudentCount", officerStudentService.getPendingCount());
        model.addAttribute("pendingCompanyCount", officerCompanyService.getPendingCount());
        model.addAttribute("pendingJobCount", jobService.getPendingJobCount());
        return "officer/dashboard";
    }
}
