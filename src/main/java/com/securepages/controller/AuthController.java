package com.securepages.controller;

import com.securepages.model.payload.RegisterRequest;
import com.securepages.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/attemptregister")
    public String attemptRegister(@ModelAttribute RegisterRequest registerRequest, Model model) {
        // Log the registration attempt
        //logger.info("Attempting to register user with username: {}", registerRequest.getUsername());

        // Check if username already exists
        if (authService.isUsernameExists(registerRequest.getUsername())) {
            model.addAttribute("err", "Username has been used. Please use a different username");
            return "public/register";
        }

        // Check if email already exists
        if (authService.isEmailExists(registerRequest.getEmail())) {
            model.addAttribute("err", "Email has been registered. Please use a different email");
            return "public/register";
        }

        // Attempt to register the user
        boolean registrationSuccessful = authService.register(registerRequest);
        if (registrationSuccessful) {
            model.addAttribute("msg", "Guest has been registered. Please check email for account confirmation before login");
        } else {
            model.addAttribute("err", "Registration failed. Please try again later.");
        }

        return "public/register";
    }

    @GetMapping("/account_confirmation")
    public String accountConfirmed(@RequestParam("rec") String token,
                                   Model model) {
        if(authService.accConfirmation(token)){
            model.addAttribute("msg", "Account Confirmed");
        }else{
            model.addAttribute("err", "Account Failed");
        }

        return "public/account_confirmation";
    }

    @PostMapping("/forgot_password")
    public String forgotPassword(@RequestParam("email") String email,
                                 Model model) {

        if(!authService.isEmailExists(email)){
            model.addAttribute("err","Email does not exist");
        }else{
            if(authService.forgotPassword(email)){
                model.addAttribute("msg","Email has been sent");
            }else{
                model.addAttribute("err","Fail to sent email. Please try again later");
            }
        }

        return "public/forgot_password";
    }

    @PostMapping("/reset_password")
    public String reset_password(@RequestParam("rec") String token,
                                 @RequestParam("password") String password,
                                 Model model) {


        if(authService.resetPassword(token,password)){
            model.addAttribute("msg","You have reset your password. You can now login using your new password");
        }else{
            model.addAttribute("err","Fail to reset password. Unfortunately, this link is unavailable or expired");
        }

        return "public/reset_password";
    }

}
