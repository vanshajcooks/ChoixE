package com.campusconnect.analytics.controller;

import com.campusconnect.analytics.dto.PlacementAnalyticsReport;
import com.campusconnect.analytics.service.PlacementAnalyticsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student/analytics")
public class StudentAnalyticsController {

    private final PlacementAnalyticsService placementAnalyticsService;

    public StudentAnalyticsController(PlacementAnalyticsService placementAnalyticsService) {
        this.placementAnalyticsService = placementAnalyticsService;
    }

    @GetMapping
    public String viewCollegeAnalytics(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        PlacementAnalyticsReport report = placementAnalyticsService.getPlacementAnalyticsReport();

        model.addAttribute("report", report);
        model.addAttribute("overview", report.getOverview());
        model.addAttribute("departmentStats", report.getDepartmentStats());
        model.addAttribute("companyStats", report.getCompanyStats());
        model.addAttribute("salaryStats", report.getSalaryStats());
        model.addAttribute("username", userDetails != null ? userDetails.getUsername() : "");

        return "student/analytics";
    }
}
