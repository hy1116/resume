package com.hypepia.resume.accesslog;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * MongoDB URI가 설정되지 않은 환경(로컬 등)에서는 Mongo 자동 설정과 접속 로그 기능을 끈다.
 * 설정 파일(application-secret.properties 포함)이 모두 로드된 뒤에 실행되도록 가장 마지막 순서로 동작한다.
 */
public class AccessLogEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String ENABLED_PROPERTY = "resume.access-log.enabled";

    private static final String MONGO_AUTO_CONFIGURATIONS = String.join(",",
            "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration",
            "org.springframework.boot.mongodb.autoconfigure.MongoReactiveAutoConfiguration",
            "org.springframework.boot.mongodb.autoconfigure.health.MongoHealthContributorAutoConfiguration",
            "org.springframework.boot.mongodb.autoconfigure.health.MongoReactiveHealthContributorAutoConfiguration",
            "org.springframework.boot.mongodb.autoconfigure.metrics.MongoMetricsAutoConfiguration",
            "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration",
            "org.springframework.boot.data.mongodb.autoconfigure.DataMongoReactiveAutoConfiguration",
            "org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration",
            "org.springframework.boot.data.mongodb.autoconfigure.DataMongoReactiveRepositoriesAutoConfiguration"
    );

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (StringUtils.hasText(environment.getProperty("spring.mongodb.uri"))) {
            return;
        }

        String existingExcludes = environment.getProperty("spring.autoconfigure.exclude", "");
        String excludes = StringUtils.hasText(existingExcludes)
                ? existingExcludes + "," + MONGO_AUTO_CONFIGURATIONS
                : MONGO_AUTO_CONFIGURATIONS;

        environment.getPropertySources().addFirst(new MapPropertySource("accessLogDisabled", Map.of(
                "spring.autoconfigure.exclude", excludes,
                ENABLED_PROPERTY, "false"
        )));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
