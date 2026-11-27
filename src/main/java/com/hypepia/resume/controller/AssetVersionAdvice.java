package com.hypepia.resume.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 정적 리소스(css, 이미지)는 1년 캐시하므로, 배포 후 새 파일을 받도록 URL에 버전을 붙인다.
 * 앱이 재시작될 때마다 값이 바뀐다. 템플릿에서는 @{/css/main.css(v=${assetVersion})} 형태로 쓴다.
 */
@ControllerAdvice
public class AssetVersionAdvice {

    private static final String ASSET_VERSION = Long.toString(System.currentTimeMillis() / 1000, 36);

    @ModelAttribute("assetVersion")
    public String assetVersion() {
        return ASSET_VERSION;
    }
}
