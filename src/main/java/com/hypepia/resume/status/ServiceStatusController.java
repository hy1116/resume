package com.hypepia.resume.status;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 포트폴리오 프로젝트들의 실행 여부를 서버에서 확인해 내려준다.
 * 브라우저에서 다른 도메인을 직접 호출하면 CORS에 막히므로 서버가 대신 확인하고,
 * 외부 사이트에 부담을 주지 않도록 결과를 일정 시간 캐시한다.
 */
@RestController
public class ServiceStatusController {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Duration CACHE_TTL = Duration.ofSeconds(60);

    private final WebClient webClient;
    private final Map<String, List<String>> targets;
    private final Mono<Map<String, String>> cachedStatus;

    public ServiceStatusController(
            @Value("${resume.status.stocksense-url}") String stocksenseUrl,
            @Value("${resume.status.apiverse-url}") String apiverseUrl,
            @Value("${resume.status.puzzle-urls}") List<String> puzzleUrls) {
        this.webClient = WebClient.create();
        this.targets = new LinkedHashMap<>();
        this.targets.put("stocksense", List.of(stocksenseUrl));
        this.targets.put("apiverse", List.of(apiverseUrl));
        this.targets.put("puzzle", puzzleUrls);
        this.cachedStatus = checkAll().cache(CACHE_TTL);
    }

    @GetMapping("/api/status")
    public Mono<Map<String, String>> status() {
        return cachedStatus;
    }

    private Mono<Map<String, String>> checkAll() {
        return Flux.fromIterable(targets.entrySet())
                .flatMap(entry -> checkAllUp(entry.getValue()).map(status -> Map.entry(entry.getKey(), status)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    /** 여러 URL을 묶어 확인한다. 모두 2xx일 때만 up. */
    private Mono<String> checkAllUp(List<String> urls) {
        return Flux.fromIterable(urls)
                .flatMap(this::check)
                .all("up"::equals)
                .map(allUp -> allUp ? "up" : "down");
    }

    private Mono<String> check(String url) {
        return webClient.get()
                .uri(url)
                .exchangeToMono(response -> response.releaseBody()
                        .thenReturn(response.statusCode().is2xxSuccessful() ? "up" : "down"))
                .timeout(TIMEOUT)
                .onErrorReturn("down");
    }
}
