#!/usr/bin/env python3
"""Build the integrated CozyCrazyCraft quest-system playtest root overlay.

Expected inputs:
- deployment/config/bountiful/**
- compiled runtime/build/libs/CozyCrazyQuests-*.jar

The output contains only CozyCrazyCraft-owned files. It never bundles Bountiful,
CozyCrazyZones, Domestication Innovation, or any other third-party mod.
"""

from __future__ import annotations

from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

ROOT = Path(__file__).resolve().parents[1]
BOUNTIFUL = ROOT / "deployment" / "config" / "bountiful"
RUNTIME_LIBS = ROOT / "runtime" / "build" / "libs"
BUILD = ROOT / "build"
OUTPUT = BUILD / "CozyCrazyCraft-QuestSystem-INTEGRATED_PLAYTEST_ROOT_OVERLAY.zip"

README = """CozyCrazyCraft Quest System — INTEGRATED PLAYTEST

Target: Minecraft Forge 1.20.1
Requires your normal installed pack, including:
- Bountiful 6.0.4
- CozyCrazyZones (0.3.6+ API; use your newest test build)
- Dungeons Enhanced 5.4.3
- Domestication Innovation if you normally use it

What this build is intended to test:
1. Boardless inhabited villages gain a civic Bountiful board near their meeting point.
2. Existing Bountiful boards prevent duplicate repair boards.
3. New/pristine Hearthlands boards receive the decree matching their CozyCrazyZones region.
4. Boards settle around 5–7 visible notices instead of filling the 21-slot capacity.
5. Bounties no longer expire after acceptance.
6. The Bountiful reputation label reads Village Trust.
7. Objective repRequired is honored by the runtime, allowing trusted-only work.
8. Domestication Innovation pet-shop village pieces are suppressed for newly generated villages.
9. Trusted regional boards can issue structure Recovery Contracts.
10. Fresh target structures contain custom glowing proof items which Bountiful consumes on turn-in.

Recovery Contract targets in this build:
- Harvestwood / Trust 2: Dungeons Enhanced Stables -> Stablemaster's Seal
- Sunscar / Trust 2: Dungeons Enhanced Desert Tomb -> Sunscar Tomb Tablet
- Greenveil / Trust 3: Dungeons Enhanced Jungle Monument treasure -> Greenveil Survey Notes
- Frostmarch / Trust 3: Dungeons Enhanced Ice Pit armory -> Frostmarch Dispatch

Important limitations of this V1:
- Already-generated pet shops are not removed from existing chunks.
- Frontier/Wildlands/Dread dedicated Bountiful decrees are still being materialized; those cells currently fail safely to Local Notices.
- Rich story-card UI is not yet implemented.
- Recovery jobs currently name a structure type rather than binding to one exact structure instance.
- Cartographer/useful-target map binding is still a separate layer.
- Stock Bountiful still allows a completed contract to be redeemed at a different board; same-issuing-board return remains planned.
- This archive is CI/build validated, but Minecraft behavior still needs your in-game smoke test.

Suggested board test:
- Use NEW village chunks for pet-shop suppression testing.
- Visit a village with no stock Bountiful gazebo and wait nearby for up to ~15 seconds.
- Open the civic board and confirm 5–7 postings.
- In Hearthlands, inspect the decree and compare it with /cozyzone debug or the zone overlay.
- Take a bounty and confirm its paper no longer shows/counts down an expiration timer.
- Complete several bounties at one board and watch Village Trust rise.

Suggested Recovery Contract test:
- Use a fresh target structure whose loot chest has never been opened.
- Helpful commands:
  /locate structure dungeons_enhanced:stables
  /locate structure dungeons_enhanced:desert_tomb
  /locate structure dungeons_enhanced:jungle_monument
  /locate structure dungeons_enhanced:ice_pit
- Open the relevant chest and confirm the named glowing proof item exists.
- Return with the accepted bounty and proof item; the proof should be consumed on successful redemption.
- For a no-travel turn-in smoke test you can /give the proof item by its cozycrazyquests ID.

Do not delete your normal Bountiful, Dungeons Enhanced, or CozyCrazyZones jars. This ZIP adds CozyCrazyQuests and replaces the quest-system Bountiful config only.
"""


def main() -> None:
    if not BOUNTIFUL.is_dir():
        raise SystemExit(f"Missing Bountiful deployment folder: {BOUNTIFUL}")
    jars = sorted(RUNTIME_LIBS.glob("CozyCrazyQuests-*.jar"))
    if not jars:
        raise SystemExit("No compiled CozyCrazyQuests runtime jar found under runtime/build/libs")
    jar = jars[-1]

    BUILD.mkdir(parents=True, exist_ok=True)
    with ZipFile(OUTPUT, "w", compression=ZIP_DEFLATED) as zf:
        zf.writestr("QUEST_SYSTEM_PLAYTEST_README.txt", README)
        for path in sorted(BOUNTIFUL.rglob("*")):
            if path.is_file():
                zf.write(path, path.relative_to(ROOT / "deployment").as_posix())
        zf.write(jar, f"mods/{jar.name}")

    print(OUTPUT.relative_to(ROOT))


if __name__ == "__main__":
    main()
