"""Extract MinimalSoundFont bytes from example1.c into assets + Java fallback."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
C_FILE = ROOT / "tools" / "example1.c"
ASSET_DIR = ROOT / "app" / "src" / "main" / "assets" / "keyboard"
JAVA_FILE = ROOT / "app" / "src" / "main" / "java" / "com" / "game" / "dream" / "audio" / "BuiltinSoundFont.java"

text = C_FILE.read_text(encoding="utf-8")
text = re.sub(r"#define\s+TEN0[^\n]*", "", text)
body = re.search(r"MinimalSoundFont\[\]\s*=\s*\{(.*?)\};", text, re.S).group(1)
body = re.sub(r"\bTEN0\b", ",".join(["0"] * 10), body)
body = body.replace("\n", " ")

data = []
for part in body.split(","):
    part = part.strip()
    if not part:
        continue
    if part.startswith("'") and part.endswith("'"):
        data.append(ord(part[1]))
    else:
        data.append(int(part))

assert len(data) == 484, len(data)

ASSET_DIR.mkdir(parents=True, exist_ok=True)
for name in ("gm.sf2", "builtin_min.sf2"):
    (ASSET_DIR / name).write_bytes(bytes(data))

def fmt_byte(b: int) -> str:
    if b > 127:
        return f"(byte) {b}"
    return str(b)


lines = []
for i in range(0, len(data), 16):
    chunk = data[i : i + 16]
    lines.append("            " + ", ".join(fmt_byte(b) for b in chunk) + ",")

java = """package com.game.dream.audio;

/** TinySoundFont 官方示例 484 字节，与 assets/keyboard/gm.sf2 一致。 */
final class BuiltinSoundFont {

    static final byte[] DATA = {
%s
    };

    private BuiltinSoundFont() {
    }
}
""" % ("\n".join(lines))

JAVA_FILE.write_text(java, encoding="utf-8")
print("ok", len(data), "bytes")