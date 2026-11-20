package com.campusconnect.officer.controller;

import com.campusconnect.company.entity.Company;
import com.campusconnect.company.entity.CompanyApprovalStatus;
import com.campusconnect.officer.dto.CompanyDecisionRequest;
import com.campusconnect.officer.service.OfficerCompanyService;
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
@RequestMapping("/officer/companies")
public class OfficerCompanyController {

    private final OfficerCompanyService officerCompanyService;

    public OfficerCompanyController(OfficerCompanyService officerCompanyService) {
        this.officerCompanyService = officerCompanyService;
    }

    @GetMapping
    public String listCompanies(@RequestParam(value = "status", required = false) String statusParam,
                                @RequestParam(value = "search", required = false) String search,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                Model model) {
        CompanyApprovalStatus status = null;
        if (statusParam != null && !statusParam.isBlank() && !"ALL".equalsIgnoreCase(statusParam)) {
            try {
                status = CompanyApprovalStatus.valueOf(statusParam.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Page<Company> companies = officerCompanyService.getCompanies(
                status,
                search,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        model.addAttribute("companies", companies);
        model.addAttribute("currentStatus", status != null ? status.name() : "ALL");
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("pendingCount", officerCompanyService.getPendingCount());

        return "officer/companies-list";
    }

    @GetMapping("/{id}")
    public String viewCompanyDetail(@PathVariable("id") Long id, Model model) {
        Company company = officerCompanyService.getCompanyById(id);
        model.addAttribute("company", company);
        model.addAttribute("decisionRequest", new CompanyDecisionRequest());
        return "officer/company-detail";
    }

    @PostMapping("/{id}/approve")
    public String approveCompany(@PathVariable("id") Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            officerCompanyService.approveCompany(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Company has been approved successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/officer/companies/" + id;
    }

    @PostMapping("/{id}/reject")
    public String rejectCompany(@PathVariable("id") Long id,
                                @RequestParam("reason") String reason,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            officerCompanyService.rejectCompany(id, reason, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("warningMessage", "Company registration has been marked as Rejected.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/officer/companies/" + id;
    }
}
