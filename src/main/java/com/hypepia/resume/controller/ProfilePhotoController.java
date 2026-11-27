package com.hypepia.resume.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * 이력서 증명사진. /static 이 아닌 곳에 두고 잠금(AccessGuardFilter) 대상 경로로 내려줘서
 * 비밀번호를 입력했거나 공유 링크로 들어온 사람만 볼 수 있게 한다.
 */
@RestController
public class ProfilePhotoController {

    private static final Resource PHOTO = new ClassPathResource("private/profile-photo.webp");

    @GetMapping("/profile-photo")
    public ResponseEntity<Resource> profilePhoto() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/webp"))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate())
                .body(PHOTO);
    }
}
