"""Pixel-art FortuneRift icon, scaled up for the mod jar and CurseForge."""
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SIZE = 64
SCALE = 8


def chunk(tag, data):
    return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)


def write_png(path, width, height, rows):
    raw = b"".join(b"\x00" + row for row in rows)
    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    path.write_bytes(png)


def scale(rows, factor):
    out = []
    for row in rows:
        wide = bytearray()
        for i in range(0, len(row), 4):
            wide.extend(row[i:i + 4] * factor)
        out.extend([bytes(wide)] * factor)
    return out


def paint():
    pixels = [[(12, 10, 18, 255) for _ in range(SIZE)] for _ in range(SIZE)]
    # outer gold frame
    for y in range(SIZE):
        for x in range(SIZE):
            if x < 3 or y < 3 or x >= SIZE - 3 or y >= SIZE - 3:
                pixels[y][x] = (196, 138, 28, 255)
            elif x < 5 or y < 5 or x >= SIZE - 5 or y >= SIZE - 5:
                pixels[y][x] = (22, 16, 34, 255)
    # cube body
    for y in range(10, 54):
        for x in range(10, 54):
            shade = 18 + ((x * 3 + y) % 5)
            pixels[y][x] = (shade, shade - 4, shade + 12, 255)
    # cracks
    cracks = []
    for i in range(8, 56):
        cracks.append((i, 18 + i // 3))
        cracks.append((i, 46 - i // 4))
        if i % 2 == 0:
            cracks.append((18 + (i % 20), i))
    for x, y in cracks:
        if 10 <= x < 54 and 10 <= y < 54:
            pixels[y][x] = (255, 206, 72, 255)
            if x + 1 < 54:
                pixels[y][x + 1] = (224, 160, 32, 255)
    # spark
    for dx, dy in ((48, 12), (50, 10), (46, 14)):
        if 0 <= dx < SIZE and 0 <= dy < SIZE:
            pixels[dy][dx] = (255, 244, 210, 255)
    rows = []
    for y in range(SIZE):
        row = bytearray()
        for x in range(SIZE):
            row.extend(pixels[y][x])
        rows.append(bytes(row))
    return scale(rows, SCALE)


def main():
    rows = paint()
    targets = [
        ROOT / "26.3" / "common" / "src" / "main" / "resources" / "fortunerift.png",
        ROOT / "1.21.1" / "common" / "src" / "main" / "resources" / "fortunerift.png",
        ROOT / "1.20.1" / "common" / "src" / "main" / "resources" / "fortunerift.png",
        ROOT / "marketplace" / "icon.png",
    ]
    for path in targets:
        path.parent.mkdir(parents=True, exist_ok=True)
        write_png(path, SIZE * SCALE, SIZE * SCALE, rows)
        print(path)


if __name__ == "__main__":
    main()
