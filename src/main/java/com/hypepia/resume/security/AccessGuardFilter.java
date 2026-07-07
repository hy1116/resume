package com.hypepia.resume.security;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String TOKEN_PARAM = "key";

    @Value("${resume.access-token:}")
    private String accessToken;

    @Override
    @NullMarked
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (!PROTECTED_PATHS.contains(path)) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getQueryParams().getFirst(TOKEN_PARAM);
        if (!accessToken.isBlank() && accessToken.equals(token)) {
            return exchange.getSession().flatMap(session -> {
                session.getAttributes().put("unlocked", true);
                exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                exchange.getResponse().getHeaders().setLocation(URI.create(path));
                return exchange.getResponse().setComplete();
            });
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
