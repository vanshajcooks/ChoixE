package com.campusconnect.company.controller;

import com.campusconnect.company.entity.Company;
import com.campusconnect.company.repository.CompanyRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/recruiter")
public class RecruiterDashboardController {

    private final CompanyRepository companyRepository;

    public RecruiterDashboardController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            companyRepository.findByRecruiterEmail(userDetails.getUsername())
                    .ifPresent(company -> model.addAttribute("company", company));
            model.addAttribute("username", userDetails.getUsername());
        }
        return "recruiter/dashboard";
    }
}
