#!/usr/bin/env python3
"""Build a drop-in root overlay containing the current CozyCrazyCraft Bountiful playtest.

The resulting ZIP intentionally contains configuration/data only. It does not bundle
third-party mods or the CozyCrazyZones jar. Apply it on top of a test instance that
already has the pack's mods installed.
"""

from __future__ import annotations

from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "deployment" / "config" / "bountiful"
BUILD = ROOT / "build"
OUTPUT = BUILD / "CozyCrazyCraft-QuestSystem-PLAYTEST_ROOT_OVERLAY.zip"

README = """CozyCrazyCraft Quest System — PLAYTEST ROOT OVERLAY

Target: Forge 1.20.1 / Bountiful 6.0.4
Geography handoff: CozyCrazyZones 0.3.6

This overlay contains only config/bountiful files from the quest repository.
It intentionally disables Bountiful's default pools/decrees and loads only the
CozyCrazyCraft playtest content.

Useful manual test decrees:
  /bo decree ccc_local_notices

Regional Hearthlands boards:
  /bo decree ccc_hearth_north
  /bo decree ccc_hearth_east
  /bo decree ccc_hearth_south
  /bo decree ccc_hearth_west

Broad regional field-job stress tests:
  /bo decree ccc_field_north
  /bo decree ccc_field_east
  /bo decree ccc_field_south
  /bo decree ccc_field_west

Deterministic structure-map tests:
  /bo decree ccc_map_test_north
  /bo decree ccc_map_test_east
  /bo decree ccc_map_test_south
  /bo decree ccc_map_test_west

Signature regional reward test:
  /bo decree ccc_signature_south_h1

After installing/replacing config, restart or reload as appropriate and run /bo test.
The map tests use a Bountiful command reward that asks Supplementaries to create a
real structure map. This handoff still requires an in-game smoke test before it is
considered production-proven.
"""


def main() -> None:
    if not SOURCE.is_dir():
        raise SystemExit(f"Missing deployment source: {SOURCE}")

    BUILD.mkdir(parents=True, exist_ok=True)
    with ZipFile(OUTPUT, "w", compression=ZIP_DEFLATED) as zf:
        zf.writestr("PLAYTEST_README.txt", README)
        for path in sorted(SOURCE.rglob("*")):
            if not path.is_file():
                continue
            relative = path.relative_to(ROOT / "deployment")
            zf.write(path, relative.as_posix())

    print(OUTPUT.relative_to(ROOT))


if __name__ == "__main__":
    main()
