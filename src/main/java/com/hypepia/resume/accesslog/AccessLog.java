package com.hypepia.resume.accesslog;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "access_logs")
public record AccessLog(
        @Id String id,
        String path,
        String method,
        int statusCode,
        String remoteAddress,
        String userAgent,
        String referer,
        boolean unlocked,
        Instant timestamp
) {
}
