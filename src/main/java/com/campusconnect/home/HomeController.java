package com.campusconnect.home;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("appName", "ChoixE");
        model.addAttribute("appDescription", "Campus Placement Management Portal");
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        }
        return "index";
    }
}
