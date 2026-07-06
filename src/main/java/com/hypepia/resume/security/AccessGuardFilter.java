package com.hypepia.resume.security;

import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

@Component
public class AccessGuardFilter implements WebFilter {

    private static final Set<String> PROTECTED_PATHS = Set.of("/", "/introduction");

    @Override
    @NullMarked
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (!PROTECTED_PATHS.contains(path)) {
            return chain.filter(exchange);
        }

        return exchange.getSession().flatMap(session -> {
            if (Boolean.TRUE.equals(session.getAttribute("unlocked"))) {
                return chain.filter(exchange);
            }
            exchange.getResponse().setStatusCode(HttpStatus.FOUND);
            exchange.getResponse().getHeaders().setLocation(URI.create("/locked?redirect=" + path));
            return exchange.getResponse().setComplete();
        });
    }
}
