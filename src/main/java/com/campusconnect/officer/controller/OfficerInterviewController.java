package com.campusconnect.officer.controller;

import com.campusconnect.company.entity.Company;
import com.campusconnect.company.repository.CompanyRepository;
import com.campusconnect.interview.entity.Interview;
import com.campusconnect.interview.entity.InterviewStatus;
import com.campusconnect.interview.service.InterviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class OfficerInterviewController {

    private final InterviewService interviewService;
    private final CompanyRepository companyRepository;

    public OfficerInterviewController(InterviewService interviewService, CompanyRepository companyRepository) {
        this.interviewService = interviewService;
        this.companyRepository = companyRepository;
    }

    @GetMapping("/officer/interviews")
    public String listAllInterviews(@RequestParam(name = "companyId", required = false) Long companyId,
                                    @RequestParam(name = "jobId", required = false) Long jobId,
                                    @RequestParam(name = "status", required = false) String statusParam,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {
        InterviewStatus status = parseStatus(statusParam);
        Pageable pageable = PageRequest.of(page, 15);
        Page<Interview> interviews = interviewService.getOfficerInterviews(companyId, jobId, status, pageable);
        List<Company> companies = companyRepository.findAll();

        model.addAttribute("interviews", interviews);
        model.addAttribute("companies", companies);
        model.addAttribute("currentCompanyId", companyId);
        model.addAttribute("currentJobId", jobId);
        model.addAttribute("currentStatus", status != null ? status.name() : "ALL");
        model.addAttribute("statuses", InterviewStatus.values());
        model.addAttribute("username", userDetails.getUsername());

        return "officer/interviews-list";
    }

    @GetMapping("/officer/interviews/{id}")
    public String viewInterviewDetail(@PathVariable("id") Long id,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      Model model) {
        try {
            Interview interview = interviewService.getInterviewDetailForOfficer(id);
            model.addAttribute("interview", interview);
            model.addAttribute("app", interview.getApplication());
            model.addAttribute("username", userDetails.getUsername());
            return "officer/interview-detail";
        } catch (IllegalArgumentException e) {
            return "redirect:/officer/interviews";
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
