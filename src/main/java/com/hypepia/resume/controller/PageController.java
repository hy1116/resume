package com.hypepia.resume.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String resume(Model model) {
        model.addAttribute("activePage", "resume");
        return "resume";
    }

    @GetMapping("/portfolio")
    public String portfolio(Model model) {
        model.addAttribute("activePage", "portfolio");
        return "portfolio";
    }
}
