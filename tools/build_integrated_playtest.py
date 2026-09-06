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

README = """CozyCrazyCraft Quest System — 0.2.1 INTEGRATED PLAYTEST

Target: Minecraft Forge 1.20.1
Requires your normal installed pack, including:
- Bountiful 6.0.4
- CozyCrazyZones (use your newest test build)
- Dungeons Enhanced 5.4.3
- Domestication Innovation if you normally use it

IMPORTANT WHEN UPGRADING
- Remove any older mods/CozyCrazyQuests-*.jar before copying this overlay.
- Keep your normal Bountiful, Dungeons Enhanced, CozyCrazyZones, and other pack jars.
- This ZIP replaces the quest-system Bountiful config and installs one CozyCrazyQuests runtime jar.

What 0.2.1 is intended to test:
1. Boardless inhabited villages gain one civic Bountiful board near their meeting point.
2. Existing Bountiful boards prevent duplicate repair boards and processed settlements persist across reloads.
3. Auto-placed boards prefer outdoor ground beside village paths and near the meeting-point elevation instead of roof-like heightmap placements.
4. While inside a village, a known board more than 12 blocks away gets a throttled actionbar direction/distance hint.
5. Hearthlands boards receive the decree matching their CozyCrazyZones region.
6. Boards settle around 5–7 underlying notices instead of filling the 21-slot capacity.
7. Taken notices return only to the exact board that issued them.
8. Board-hover story text begins with the same authored quest title used by the accepted paper instead of a raw target name such as Skeleton.
9. Ordinary radiant rewards use clean emerald village payment; generic bread/cooked-beef reward rolls are removed.
10. The board gradually rotates one slot about every 120 seconds rather than every 300 seconds.
11. Bounties do not expire after acceptance and Bountiful still grants its small built-in completion XP.
12. The Bountiful reputation label reads Village Trust and objective repRequired is honored.
13. Trusted regional boards can issue the current prototype structure Recovery Contracts.
14. Fresh target structures contain custom glowing proof items which Bountiful consumes on turn-in.

Recovery Contract targets in this build:
- Harvestwood / Trust 2: Dungeons Enhanced Stables -> Stablemaster's Seal
- Sunscar / Trust 2: Dungeons Enhanced Desert Tomb -> Sunscar Tomb Tablet
- Greenveil / Trust 3: Dungeons Enhanced Jungle Monument treasure -> Greenveil Survey Notes
- Frostmarch / Trust 3: Dungeons Enhanced Ice Pit armory -> Frostmarch Dispatch

Important current limitations:
- Recovery Contracts are still weighted Bountiful entries. Reaching the required Trust only makes them eligible for newly generated notices; old notices do not transform, and the contract is not yet guaranteed to occupy a featured slot. That progression layer is the next redesign.
- Recovery jobs currently name a structure type rather than binding to one exact generated structure instance.
- Cartographer/useful-target map binding is still a separate layer.
- Already-generated pet shops are not removed from existing chunks.
- Frontier/Wildlands/Dread dedicated Bountiful decrees are still being materialized; those cells currently fail safely to Local Notices.
- This archive is CI/build validated, but the new placement/locator behavior still needs an in-game smoke test.

Suggested 0.2.1 board test:
- Use a village whose board you previously had trouble finding. If its saved board still exists, walk around the village and confirm the actionbar gives a direction/distance hint while you are more than 12 blocks away.
- For placement testing, use NEW village chunks or a fresh world so a boardless village receives a new 0.2.1 placement.
- Open a posting and confirm the left-side board hover starts with the authored title, then take it and confirm the paper uses the same title.
- Confirm routine notice rewards no longer show bread or cooked beef.
- Take all visible notices; after roughly two minutes, confirm a changed slot can surface fresh work rather than waiting five minutes.
- Complete a bounty from Village A, try Village B (should reject), then return to Village A (should redeem).

Suggested Recovery Contract test:
- West/South become eligible at Village Trust 2; East/North at Village Trust 3.
- Eligibility applies only to newly generated notices after the threshold is reached.
- Use a fresh target structure whose loot chest has never been opened.
- Helpful commands:
  /locate structure dungeons_enhanced:stables
  /locate structure dungeons_enhanced:desert_tomb
  /locate structure dungeons_enhanced:jungle_monument
  /locate structure dungeons_enhanced:ice_pit
- Open the relevant chest and confirm the named glowing proof item exists.
- Return with the accepted bounty and proof item; the proof should be consumed on successful redemption.

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
