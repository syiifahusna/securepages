package com.securepages.controller;

import com.securepages.model.payload.LoginRequest;
import com.securepages.model.payload.RegisterRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class PublicController {

    @GetMapping({"/","/index"})
    public String index() {
        return "public/index";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "public/register";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "public/login";
    }

    @GetMapping("/forgot_password")
    public String forgotPassword(Model model) {
        return "public/forgot_password";
    }

    @GetMapping("/reset_password")
    public String resetPassword(@RequestParam("rec") String token, Model model) {
        model.addAttribute("rec", token);
        return "public/reset_password";
    }

    @GetMapping("/gallery")
    public String aboutUs(Model model) {
        return "public/gallery";
    }
}
