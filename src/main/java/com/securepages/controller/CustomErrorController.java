package com.securepages.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;


@Controller
@RequestMapping("/")
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object error = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        model.addAttribute("status", status);
        model.addAttribute("message", message);
        model.addAttribute("error", error != null ? error.toString() : "N/A");

        return "public/errors/error";
    }

}
