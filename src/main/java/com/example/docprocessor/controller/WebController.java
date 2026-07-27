package com.example.docprocessor.controller;

import com.example.docprocessor.config.AppConfigProperties;
import com.example.docprocessor.model.Document;
import com.example.docprocessor.model.User;
import com.example.docprocessor.service.DocumentService;
import com.example.docprocessor.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class WebController {

    private final UserService userService;
    private final DocumentService documentService;
    private final AppConfigProperties appConfig;

    public WebController(UserService userService, DocumentService documentService, AppConfigProperties appConfig) {
        this.userService = userService;
        this.documentService = documentService;
        this.appConfig = appConfig;
    }

    @ModelAttribute
    public void addAppAttributes(Model model) {
        model.addAttribute("appTitle", appConfig.getTitle());
        model.addAttribute("appDescription", appConfig.getDescription());
        model.addAttribute("appVersion", appConfig.getVersion());
        model.addAttribute("appDocumentCode", appConfig.getDocumentCode());
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        String errorMsg = (String) request.getSession().getAttribute("loginError");
        if (errorMsg != null) {
            model.addAttribute("error", errorMsg);
            request.getSession().removeAttribute("loginError");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            userService.registerUser(username, password);
            model.addAttribute("success", "Registration successful! Please login.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/")
    public String dashboard(@AuthenticationPrincipal UserDetails currentUser,
                            @RequestParam(required = false) String query,
                            @RequestParam(required = false) String fileType,
                            @RequestParam(required = false) Long selectDocId,
                            Model model) {
        model.addAttribute("currentUser", currentUser);

        List<Document> documents = documentService.searchAndFilterDocuments(query, fileType);
        model.addAttribute("documents", documents);
        model.addAttribute("query", query);
        model.addAttribute("fileType", fileType);

        if (selectDocId != null) {
            try {
                Document selectedDoc = documentService.getDocumentById(selectDocId);
                selectedDoc.getSections().size(); // Trigger load
                model.addAttribute("selectedDoc", selectedDoc);
            } catch (Exception e) {
                model.addAttribute("error", "Error loading selected document: " + e.getMessage());
            }
        }

        return "dashboard";
    }

    @PostMapping("/upload")
    public String uploadDocument(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a valid file to upload.");
            return "redirect:/";
        }
        try {
            Document doc = documentService.processDocument(file);
            if (doc.isProcessed()) {
                redirectAttributes.addFlashAttribute("success", "File successfully processed! (Type: " + doc.getFileType() + ")");
                redirectAttributes.addAttribute("selectDocId", doc.getId());
            } else {
                redirectAttributes.addFlashAttribute("error", "Processing failed: " + doc.getStatusMessage());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Upload error: " + e.getMessage());
        }
        return "redirect:/";
    }

    // User Maintenance Panel
    @GetMapping("/users")
    public String userMaintenance(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    @PostMapping("/users/lock/{id}")
    public String lockUser(@PathVariable Long id) {
        userService.lockUser(id);
        return "redirect:/users";
    }

    @PostMapping("/users/unlock/{id}")
    public String unlockUser(@PathVariable Long id) {
        userService.unlockUser(id);
        return "redirect:/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }

    @PostMapping("/users/reset-password")
    public String resetPassword(@RequestParam Long userId, @RequestParam String newPassword, RedirectAttributes redirectAttributes) {
        userService.resetPassword(userId, newPassword);
        redirectAttributes.addFlashAttribute("success", "Password successfully updated.");
        return "redirect:/users";
    }

    @PostMapping("/users/expire-password/{id}")
    public String expirePassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.setPasswordExpired(id);
        redirectAttributes.addFlashAttribute("success", "Password marked as expired successfully.");
        return "redirect:/users";
    }
}
