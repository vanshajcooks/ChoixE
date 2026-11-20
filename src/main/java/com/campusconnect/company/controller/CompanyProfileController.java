package com.campusconnect.company.controller;

import com.campusconnect.company.dto.CompanyProfileUpdateRequest;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recruiter/profile")
public class CompanyProfileController {

    private final CompanyService companyService;

    public CompanyProfileController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public String viewProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Company company = companyService.getCompanyByRecruiterEmail(userDetails.getUsername());
        model.addAttribute("company", company);
        model.addAttribute("username", userDetails.getUsername());
        return "recruiter/profile";
    }

    @GetMapping("/edit")
    public String editProfileForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Company company = companyService.getCompanyByRecruiterEmail(userDetails.getUsername());
        if (!model.containsAttribute("request")) {
            CompanyProfileUpdateRequest request = new CompanyProfileUpdateRequest();
            request.setCompanyName(company.getCompanyName());
            request.setIndustry(company.getIndustry());
            request.setWebsite(company.getWebsite());
            request.setLocation(company.getLocation());
            request.setContactNumber(company.getContactNumber());
            request.setDescription(company.getDescription());
            model.addAttribute("request", request);
        }
        model.addAttribute("company", company);
        return "recruiter/profile-edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@Valid @ModelAttribute("request") CompanyProfileUpdateRequest request,
                                BindingResult result,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Company company = companyService.getCompanyByRecruiterEmail(userDetails.getUsername());
            model.addAttribute("company", company);
            return "recruiter/profile-edit";
        }

        try {
            companyService.updateCompanyProfile(userDetails.getUsername(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Organization profile updated successfully.");
            return "redirect:/recruiter/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            Company company = companyService.getCompanyByRecruiterEmail(userDetails.getUsername());
            model.addAttribute("company", company);
            return "recruiter/profile-edit";
        }
    }
}
