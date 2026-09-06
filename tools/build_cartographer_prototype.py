#!/usr/bin/env python3
"""Build the isolated regional-cartographer datapack prototype.

This intentionally remains separate from the normal root-overlay playtest so Bountiful
and cartographer behavior can be tested independently.
"""

from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "prototype" / "cartographer_datapack"
BUILD = ROOT / "build"
OUTPUT = BUILD / "CozyCrazyCraft-Regional-Cartographer-PROTOTYPE.zip"


def main() -> None:
    if not (SOURCE / "pack.mcmeta").is_file():
        raise SystemExit("Missing cartographer prototype pack.mcmeta")

    BUILD.mkdir(parents=True, exist_ok=True)
    with ZipFile(OUTPUT, "w", compression=ZIP_DEFLATED) as zf:
        for path in sorted(SOURCE.rglob("*")):
            if path.is_file() and path.name != "README.md":
                zf.write(path, path.relative_to(SOURCE).as_posix())

    print(OUTPUT.relative_to(ROOT))


if __name__ == "__main__":
    main()
