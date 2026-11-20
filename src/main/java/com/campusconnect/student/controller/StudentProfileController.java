package com.campusconnect.student.controller;

import com.campusconnect.student.dto.StudentProfileUpdateRequest;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.service.StudentService;
import com.campusconnect.student.service.StudentService.ResumeDownload;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student")
public class StudentProfileController {

    private final StudentService studentService;

    public StudentProfileController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        StudentProfile profile = studentService.getProfileByEmail(userDetails.getUsername());
        model.addAttribute("profile", profile);
        model.addAttribute("username", userDetails.getUsername());
        return "student/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        StudentProfile profile = studentService.getProfileByEmail(userDetails.getUsername());
        if (!model.containsAttribute("request")) {
            StudentProfileUpdateRequest request = new StudentProfileUpdateRequest();
            request.setFullName(profile.getUser().getFullName());
            request.setPhoneNumber(profile.getPhoneNumber());
            request.setDepartment(profile.getDepartment());
            request.setGraduationYear(profile.getGraduationYear());
            request.setCgpa(profile.getCgpa());
            request.setActiveBacklogs(profile.getActiveBacklogs());
            model.addAttribute("request", request);
        }
        model.addAttribute("profile", profile);
        return "student/profile-edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute("request") StudentProfileUpdateRequest request,
                                BindingResult result,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            StudentProfile profile = studentService.getProfileByEmail(userDetails.getUsername());
            model.addAttribute("profile", profile);
            return "student/profile-edit";
        }

        try {
            boolean reVerificationTriggered = studentService.updateProfile(userDetails.getUsername(), request);
            if (reVerificationTriggered) {
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Profile updated! Because academic fields were modified, your profile requires re-verification by the Placement Officer.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
            }
            return "redirect:/student/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            StudentProfile profile = studentService.getProfileByEmail(userDetails.getUsername());
            model.addAttribute("profile", profile);
            return "student/profile-edit";
        }
    }

    @PostMapping("/resume/upload")
    public String uploadResume(@RequestParam("resume") MultipartFile file,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            studentService.uploadResume(userDetails.getUsername(), file);
            redirectAttributes.addFlashAttribute("successMessage", "Resume uploaded successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload resume. Please try again.");
        }
        return "redirect:/student/profile";
    }

    @GetMapping("/resume/download")
    public ResponseEntity<Resource> downloadResume(@AuthenticationPrincipal UserDetails userDetails) {
        ResumeDownload download = studentService.getResumeResource(userDetails.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.originalFilename() + "\"")
                .body(download.resource());
    }
}
