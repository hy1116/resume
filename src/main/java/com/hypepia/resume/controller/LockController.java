package com.hypepia.resume.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

@Controller
public class LockController {

    private static final Set<String> ALLOWED_REDIRECTS = Set.of("/", "/introduction");

    @Value("${resume.access-password}")
    private String accessPassword;

    @GetMapping("/locked")
    public String locked(@RequestParam(defaultValue = "/") String redirect,
                          @RequestParam(required = false) String error,
                          Model model) {
        model.addAttribute("redirect", safeRedirect(redirect));
        model.addAttribute("error", error != null);
        return "lock";
    }

    @PostMapping("/unlock")
    public Mono<Void> unlock(ServerWebExchange exchange) {
        return exchange.getFormData().flatMap(form -> {
            String password = form.getFirst("password");
            String safe = safeRedirect(form.getFirst("redirect"));
            return exchange.getSession().flatMap(session -> {
                String target;
                if (accessPassword.equals(password)) {
                    session.getAttributes().put("unlocked", true);
                    target = safe;
                } else {
                    target = "/locked?redirect=" + safe + "&error=1";
                }
                exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                exchange.getResponse().getHeaders().setLocation(URI.create(target));
                return exchange.getResponse().setComplete();
            });
        });
    }

    private String safeRedirect(String redirect) {
        return ALLOWED_REDIRECTS.contains(redirect) ? redirect : "/";
    }
}
