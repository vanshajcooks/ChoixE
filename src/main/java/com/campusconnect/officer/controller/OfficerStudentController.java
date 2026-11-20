package com.campusconnect.officer.controller;

import com.campusconnect.officer.dto.VerificationDecisionRequest;
import com.campusconnect.officer.service.OfficerStudentService;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import com.campusconnect.student.service.StudentService.ResumeDownload;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/officer/students")
public class OfficerStudentController {

    private final OfficerStudentService officerStudentService;

    public OfficerStudentController(OfficerStudentService officerStudentService) {
        this.officerStudentService = officerStudentService;
    }

    @GetMapping
    public String listStudents(@RequestParam(value = "status", required = false) String statusParam,
                               @RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               Model model) {
        VerificationStatus status = null;
        if (statusParam != null && !statusParam.isBlank() && !"ALL".equalsIgnoreCase(statusParam)) {
            try {
                status = VerificationStatus.valueOf(statusParam.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Page<StudentProfile> students = officerStudentService.getStudents(
                status,
                search,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        model.addAttribute("students", students);
        model.addAttribute("currentStatus", status != null ? status.name() : "ALL");
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("pendingCount", officerStudentService.getPendingCount());

        return "officer/students-list";
    }

    @GetMapping("/{id}")
    public String viewStudentDetail(@PathVariable("id") Long id, Model model) {
        StudentProfile student = officerStudentService.getStudentById(id);
        model.addAttribute("student", student);
        model.addAttribute("decisionRequest", new VerificationDecisionRequest());
        return "officer/student-detail";
    }

    @GetMapping("/{id}/resume")
    public ResponseEntity<Resource> downloadStudentResume(@PathVariable("id") Long id) {
        ResumeDownload download = officerStudentService.getStudentResumeResource(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.originalFilename() + "\"")
                .body(download.resource());
    }

    @PostMapping("/{id}/verify")
    public String verifyStudent(@PathVariable("id") Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            officerStudentService.verifyStudent(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Student profile has been verified successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/officer/students/" + id;
    }

    @PostMapping("/{id}/reject")
    public String rejectStudent(@PathVariable("id") Long id,
                                @RequestParam("reason") String reason,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            officerStudentService.rejectStudent(id, reason, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("warningMessage", "Student profile has been marked as Rejected.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/officer/students/" + id;
    }
}
