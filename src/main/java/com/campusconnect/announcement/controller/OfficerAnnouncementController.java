package com.campusconnect.announcement.controller;

import com.campusconnect.announcement.dto.AnnouncementRequest;
import com.campusconnect.announcement.entity.Announcement;
import com.campusconnect.announcement.entity.AnnouncementPriority;
import com.campusconnect.announcement.entity.TargetAudience;
import com.campusconnect.announcement.service.AnnouncementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/officer/announcements")
public class OfficerAnnouncementController {

    private final AnnouncementService announcementService;

    public OfficerAnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public String listAnnouncements(@RequestParam(name = "active", required = false) Boolean active,
                                    @RequestParam(name = "audience", required = false) String audienceParam,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {
        TargetAudience audience = null;
        if (audienceParam != null && !audienceParam.isBlank() && !"ALL_TYPES".equalsIgnoreCase(audienceParam)) {
            try {
                audience = TargetAudience.valueOf(audienceParam.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Page<Announcement> announcements = announcementService.getOfficerAnnouncements(
                active, audience, PageRequest.of(page, 15)
        );

        model.addAttribute("announcements", announcements);
        model.addAttribute("audiences", TargetAudience.values());
        model.addAttribute("priorities", AnnouncementPriority.values());
        model.addAttribute("currentActive", active);
        model.addAttribute("currentAudience", audience != null ? audience.name() : "ALL_TYPES");
        model.addAttribute("username", userDetails.getUsername());

        return "officer/announcements-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("announcementRequest", new AnnouncementRequest());
        model.addAttribute("isEdit", false);
        model.addAttribute("audiences", TargetAudience.values());
        model.addAttribute("priorities", AnnouncementPriority.values());
        model.addAttribute("username", userDetails.getUsername());
        return "officer/announcement-form";
    }

    @PostMapping
    public String createAnnouncement(@Valid @ModelAttribute("announcementRequest") AnnouncementRequest request,
                                     BindingResult result,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("audiences", TargetAudience.values());
            model.addAttribute("priorities", AnnouncementPriority.values());
            model.addAttribute("username", userDetails.getUsername());
            return "officer/announcement-form";
        }

        announcementService.createAnnouncement(userDetails.getUsername(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Announcement published successfully!");
        return "redirect:/officer/announcements";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        try {
            Announcement announcement = announcementService.getAnnouncementById(id);
            AnnouncementRequest form = new AnnouncementRequest(
                    announcement.getTitle(),
                    announcement.getContent(),
                    announcement.getTargetAudience(),
                    announcement.getPriority(),
                    announcement.isPinned(),
                    announcement.isActive(),
                    false,
                    announcement.getExpiresAt()
            );

            model.addAttribute("announcementRequest", form);
            model.addAttribute("announcementId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("audiences", TargetAudience.values());
            model.addAttribute("priorities", AnnouncementPriority.values());
            model.addAttribute("username", userDetails.getUsername());
            return "officer/announcement-form";
        } catch (IllegalArgumentException e) {
            return "redirect:/officer/announcements";
        }
    }

    @PostMapping("/{id}")
    public String updateAnnouncement(@PathVariable("id") Long id,
                                     @Valid @ModelAttribute("announcementRequest") AnnouncementRequest request,
                                     BindingResult result,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("announcementId", id);
            model.addAttribute("audiences", TargetAudience.values());
            model.addAttribute("priorities", AnnouncementPriority.values());
            model.addAttribute("username", userDetails.getUsername());
            return "officer/announcement-form";
        }

        announcementService.updateAnnouncement(id, request);
        redirectAttributes.addFlashAttribute("successMessage", "Announcement updated successfully!");
        return "redirect:/officer/announcements";
    }

    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Announcement toggled = announcementService.toggleStatus(id);
            String status = toggled.isActive() ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("successMessage", "Announcement " + status + ".");
        } catch (Exception ignored) {
        }
        return "redirect:/officer/announcements";
    }

    @PostMapping("/{id}/delete")
    public String deleteAnnouncement(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            announcementService.deleteAnnouncement(id);
            redirectAttributes.addFlashAttribute("successMessage", "Announcement deleted successfully.");
        } catch (Exception ignored) {
        }
        return "redirect:/officer/announcements";
    }
}
