package com.campusconnect.interview.controller;

import com.campusconnect.interview.entity.Interview;
import com.campusconnect.interview.service.InterviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class StudentInterviewController {

    private final InterviewService interviewService;

    public StudentInterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping("/student/interviews")
    public String viewStudentInterviews(@RequestParam(name = "page", defaultValue = "0") int page,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        Model model) {
        Page<Interview> interviews = interviewService.getStudentInterviews(userDetails.getUsername(), PageRequest.of(page, 15));
        List<Interview> upcomingInterviews = interviewService.getUpcomingStudentInterviews(userDetails.getUsername());

        model.addAttribute("interviews", interviews);
        model.addAttribute("upcomingInterviews", upcomingInterviews);
        model.addAttribute("username", userDetails.getUsername());

        return "student/interviews-list";
    }
}
