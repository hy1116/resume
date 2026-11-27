"""
Pretendard Variable 폰트를 '고정된 글자 세트'로 서브셋한다. 한 번 만들면 문구를 바꿔도 다시 만들 필요가 없다.

글자 세트: ASCII + KS X 1001 한글 2,350자(일상에서 쓰는 한글 거의 전부) + 한글 자모 + 자주 쓰는 기호.
템플릿에 이 세트 밖의 글자를 쓰면 FontSubsetCoverageTest 가 알려준다. 그때만 다시 실행하면 되고,
그 글자는 자동으로 세트에 추가된다.

사용법 (프로젝트 루트에서):
    pip install fonttools brotli
    python scripts/subset_font.py

결과:
    - static/fonts/pretendard-ks2350.woff2 (파일명 고정, 1년 캐시)
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


SYMBOLS = "·…–—‘’“”•※→←↑↓↗↘○●◎◇◆□■△▲▽▼☆★♡♥、。「」『』《》〈〉【】①②③④⑤⑥⑦⑧⑨⑩×÷±°℃%‰©®™€₩"


def ks_x_1001_hangul():
    chars = []
    for hi in range(0xB0, 0xC9):
        for lo in range(0xA1, 0xFF):
            try:
                chars.append(bytes([hi, lo]).decode("euc-kr"))
            except UnicodeDecodeError:
                pass
    return {c for c in chars if "가" <= c <= "힣"}


def collect_chars():
    chars = set(chr(c) for c in range(0x20, 0x7F))  # ASCII 전체
    chars |= ks_x_1001_hangul()
    chars |= set(chr(c) for c in range(0x3131, 0x3164))  # 한글 호환 자모
    chars |= set(SYMBOLS)
    # 세트 밖 글자가 템플릿에 있으면 함께 넣는다 (드문 경우)
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
    name = "pretendard-ks2350.woff2"

    os.makedirs(FONT_DIR, exist_ok=True)
    for old in glob.glob(os.path.join(FONT_DIR, "pretendard-subset*.woff2")):
        os.remove(old)
    with open(os.path.join(FONT_DIR, name), "wb") as f:
        f.write(data)

    with open(CHARS_FILE, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)

    print(f"{name}: {len(text)} chars, {len(data) / 1024:.0f} KB")


if __name__ == "__main__":
    main()
