package com.hypepia.resume.accesslog;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Instant;

@Component
public class AccessLogFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);
    private static final String[] SKIP_PREFIXES = {"/css/", "/images/", "/favicon.ico"};

    private final AccessLogRepository accessLogRepository;

    public AccessLogFilter(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    @Override
    @NullMarked
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (shouldSkip(path)) {
            return chain.filter(exchange);
        }

        return chain.filter(exchange)
                .doFinally(signal -> recordAccess(exchange)
                        .subscribe(null, error -> log.warn("Failed to persist access log", error)));
    }

    private boolean shouldSkip(String path) {
        for (String prefix : SKIP_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> recordAccess(ServerWebExchange exchange) {
        return exchange.getSession().flatMap(session -> {
            AccessLog entry = new AccessLog(
                    null,
                    exchange.getRequest().getPath().value(),
                    exchange.getRequest().getMethod().name(),
                    exchange.getResponse().getStatusCode() != null ? exchange.getResponse().getStatusCode().value() : 0,
                    resolveClientIp(exchange),
                    exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT),
                    exchange.getRequest().getHeaders().getFirst(HttpHeaders.REFERER),
                    Boolean.TRUE.equals(session.getAttribute("unlocked")),
                    Instant.now()
            );
            return accessLogRepository.save(entry).then();
        });
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null && remoteAddress.getAddress() != null
                ? remoteAddress.getAddress().getHostAddress()
                : "unknown";
    }
}
