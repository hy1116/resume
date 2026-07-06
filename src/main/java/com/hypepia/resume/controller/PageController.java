package com.hypepia.resume.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String resume() {
        return "resume";
    }

    @GetMapping("/introduction")
    public String introduction() {
        return "introduction";
    }

    @GetMapping("/portfolio")
    public String portfolio() {
        return "portfolio";
    }

    @GetMapping("/portfolio/stocksense")
    public String stocksense() {
        return "portfolio-stocksense";
    }

    @GetMapping("/portfolio/apiverse")
    public String apiverse() {
        return "portfolio-apiverse";
    }
}
