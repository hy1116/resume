"""
Pretendard Variable 폰트를 사이트 템플릿에 실제로 쓰인 글자만 남겨 서브셋으로 만든다.

템플릿(src/main/resources/templates)의 문구를 바꿔 새 글자가 생기면 다시 실행해야 한다.
(FontSubsetCoverageTest 가 빠진 글자를 잡아낸다)

사용법 (프로젝트 루트에서):
    pip install fonttools brotli
    python scripts/subset_font.py

결과:
    - static/fonts/pretendard-subset.woff2 덮어쓰기 (파일명 고정 — 템플릿·CSS는 고칠 필요 없음,
      캐시는 templates/fragments/font.html 의 ?v=assetVersion 으로 배포마다 갱신)
    - scripts/pretendard-subset-chars.txt 갱신 (테스트에서 사용)
"""
import glob
import io
import os
import urllib.request

from fontTools import subset

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
TEMPLATES = os.path.join(RES, "templates")
FONT_DIR = os.path.join(RES, "static", "fonts")
CHARS_FILE = os.path.join(ROOT, "scripts", "pretendard-subset-chars.txt")
SOURCE_URL = ("https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/"
              "packages/pretendard/dist/web/variable/woff2/PretendardVariable.woff2")


def collect_chars():
    chars = set(chr(c) for c in range(0x20, 0x7F))  # ASCII 전체
    for path in glob.glob(os.path.join(TEMPLATES, "**", "*.html"), recursive=True):
        with open(path, encoding="utf-8") as f:
            chars |= set(f.read())
    return "".join(sorted(c for c in chars if c.isprintable()))


def build_subset(text):
    source = urllib.request.urlopen(SOURCE_URL).read()
    options = subset.Options()
    options.flavor = "woff2"
    options.layout_features = ["*"]
    font = subset.load_font(io.BytesIO(source), options)
    subsetter = subset.Subsetter(options)
    subsetter.populate(text=text)
    subsetter.subset(font)
    out = io.BytesIO()
    subset.save_font(font, out, options)
    return out.getvalue()


def main():
    text = collect_chars()
    data = build_subset(text)
    name = "pretendard-subset.woff2"

    os.makedirs(FONT_DIR, exist_ok=True)
    for old in glob.glob(os.path.join(FONT_DIR, "pretendard-subset-*.woff2")):
        os.remove(old)
    with open(os.path.join(FONT_DIR, name), "wb") as f:
        f.write(data)

    with open(CHARS_FILE, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)

    print(f"{name}: {len(text)} chars, {len(data) / 1024:.0f} KB")


if __name__ == "__main__":
    main()
