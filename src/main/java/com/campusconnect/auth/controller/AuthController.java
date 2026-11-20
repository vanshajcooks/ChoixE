package com.campusconnect.auth.controller;

import com.campusconnect.auth.dto.RecruiterRegisterRequest;
import com.campusconnect.auth.dto.StudentRegisterRequest;
import com.campusconnect.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "registered", required = false) String registered,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Account created successfully! Please log in to continue.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerSelect() {
        return "auth/register-select";
    }

    @GetMapping("/register/student")
    public String showStudentRegisterForm(Model model) {
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new StudentRegisterRequest());
        }
        return "auth/register-student";
    }

    @PostMapping("/register/student")
    public String handleStudentRegistration(@Valid @ModelAttribute("request") StudentRegisterRequest request,
                                            BindingResult result,
                                            Model model) {
        if (!request.isPasswordMatching()) {
            result.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }

        if (result.hasErrors()) {
            return "auth/register-student";
        }

        try {
            authService.registerStudent(request);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("registrationError", e.getMessage());
            return "auth/register-student";
        }
    }

    @GetMapping("/register/recruiter")
    public String showRecruiterRegisterForm(Model model) {
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new RecruiterRegisterRequest());
        }
        return "auth/register-recruiter";
    }

    @PostMapping("/register/recruiter")
    public String handleRecruiterRegistration(@Valid @ModelAttribute("request") RecruiterRegisterRequest request,
                                              BindingResult result,
                                              Model model) {
        if (!request.isPasswordMatching()) {
            result.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }

        if (result.hasErrors()) {
            return "auth/register-recruiter";
        }

        try {
            authService.registerRecruiter(request);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("registrationError", e.getMessage());
            return "auth/register-recruiter";
        }
    }
}
