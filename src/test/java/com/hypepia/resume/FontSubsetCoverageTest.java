package com.hypepia.resume;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 템플릿에 쓰인 글자가 모두 서브셋 폰트에 들어 있는지 확인한다.
 * 실패하면 `python scripts/subset_font.py` 로 폰트를 다시 만들 것.
 */
class FontSubsetCoverageTest {

    @Test
    void subsetFontCoversAllTemplateCharacters() throws IOException {
        Set<Integer> covered = Files.readString(Path.of("scripts/pretendard-subset-chars.txt"), StandardCharsets.UTF_8)
                .codePoints().boxed().collect(Collectors.toSet());

        Set<String> missing = new TreeSet<>();
        try (Stream<Path> files = Files.walk(Path.of("src/main/resources/templates"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".html")).toList()) {
                Files.readString(file, StandardCharsets.UTF_8).codePoints()
                        .filter(cp -> !Character.isWhitespace(cp) && !Character.isISOControl(cp))
                        .filter(cp -> !covered.contains(cp))
                        .forEach(cp -> missing.add(new String(Character.toChars(cp))));
            }
        }

        assertTrue(missing.isEmpty(),
                "서브셋 폰트에 없는 글자: " + String.join("", missing) + " — python scripts/subset_font.py 실행 필요");
    }
}
