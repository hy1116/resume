"""
Pretendard Variable 폰트를 사이트 템플릿에 실제로 쓰인 글자만 남겨 서브셋으로 만든다.

템플릿(src/main/resources/templates)의 문구를 바꿔 새 글자가 생기면 다시 실행해야 한다.
(FontSubsetCoverageTest 가 빠진 글자를 잡아낸다)

사용법 (프로젝트 루트에서):
    pip install fonttools brotli
    python scripts/subset_font.py

결과:
    - static/fonts/pretendard-subset-<hash>.woff2 생성 (이전 파일 삭제)
    - main.css 의 @font-face 와 템플릿의 preload 경로를 새 파일명으로 교체
    - scripts/pretendard-subset-chars.txt 갱신 (테스트에서 사용)
"""
import glob
import hashlib
import io
import os
import re
import urllib.request

from fontTools import subset

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
TEMPLATES = os.path.join(RES, "templates")
FONT_DIR = os.path.join(RES, "static", "fonts")
CSS = os.path.join(RES, "static", "css", "main.css")
CHARS_FILE = os.path.join(ROOT, "scripts", "pretendard-subset-chars.txt")
SOURCE_URL = ("https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/"
              "packages/pretendard/dist/web/variable/woff2/PretendardVariable.woff2")
FONT_PATTERN = re.compile(r"pretendard-subset-[0-9a-f]+\.woff2")


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


def replace_in_file(path, new_name):
    with open(path, encoding="utf-8", newline="") as f:
        content = f.read()
    updated = FONT_PATTERN.sub(new_name, content)
    if updated != content:
        with open(path, "w", encoding="utf-8", newline="") as f:
            f.write(updated)


def main():
    text = collect_chars()
    data = build_subset(text)
    name = f"pretendard-subset-{hashlib.sha256(data).hexdigest()[:8]}.woff2"

    os.makedirs(FONT_DIR, exist_ok=True)
    for old in glob.glob(os.path.join(FONT_DIR, "pretendard-subset-*.woff2")):
        os.remove(old)
    with open(os.path.join(FONT_DIR, name), "wb") as f:
        f.write(data)

    replace_in_file(CSS, name)
    for path in glob.glob(os.path.join(TEMPLATES, "**", "*.html"), recursive=True):
        replace_in_file(path, name)

    with open(CHARS_FILE, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)

    print(f"{name}: {len(text)} chars, {len(data) / 1024:.0f} KB")


if __name__ == "__main__":
    main()
