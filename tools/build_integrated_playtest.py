#!/usr/bin/env python3
"""Build the integrated CozyCrazyCraft quest-system playtest root overlay.

Expected inputs:
- deployment/config/bountiful/**
- compiled runtime/build/libs/CozyCrazyQuests-*.jar

The output contains only CozyCrazyCraft-owned files. It never bundles Bountiful,
Conversations, CozyCrazyZones, or any other third-party mod.
"""

from __future__ import annotations

from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

ROOT = Path(__file__).resolve().parents[1]
BOUNTIFUL = ROOT / "deployment" / "config" / "bountiful"
RUNTIME_LIBS = ROOT / "runtime" / "build" / "libs"
BUILD = ROOT / "build"
OUTPUT = BUILD / "CozyCrazyCraft-QuestSystem-INTEGRATED_PLAYTEST_ROOT_OVERLAY.zip"

README = """CozyCrazyCraft Quest System — 0.3.0 AUTHORED DIALOGUE PLAYTEST

Target: Minecraft Forge 1.20.1
Requires your normal installed pack, including:
- Bountiful 6.0.4
- Conversations 1.0.5 (user-installed; NOT bundled here)
- Lazr's Lib required by your Conversations installation
- CozyCrazyZones 0.3.6+
- Dungeons Enhanced 5.4.3
- Valhelsia Structures if present in your normal pack

IMPORTANT WHEN UPGRADING
- Remove any older mods/CozyCrazyQuests-*.jar before copying this overlay.
- Keep your normal Bountiful, Conversations, Lazr's Lib, CozyCrazyZones, Dungeons Enhanced, and other pack jars.
- This ZIP contains only CozyCrazyCraft-owned config/resources and one CozyCrazyQuests runtime jar.

0.3.0 architecture goal
Bountiful remains the public civic notice board. Important profession requests and story contracts move into an authored villager layer. The first end-to-end proof is a Cartographer quest whose availability depends on a real nearby structure.

The First Real Map test
1. Use an inhabited HEARTHLANDS village with a working bounty board and a vanilla Cartographer.
2. Right-click the Cartographer. If a legal nearby landmark can be resolved, Conversations should open a dialogue titled "The First Real Map" instead of immediately opening trade.
3. The dialogue must still offer "Show me your trades instead" so adding quests does not destroy normal villager trading.
4. Accept the survey. A named Village Contract should appear with the actual resolved landmark type plus approximate distance and compass direction.
5. Walk to that exact target. The 0.3.0 prototype searches up to 1024 blocks and accepts same-tier or one-tier-outward targets; outside the shared core it requires the target to remain in the same macro-region.
6. When you come within 56 horizontal blocks of the cached target, you should receive "Survey complete". The target must NOT change if you relog, wander away, or discover another structure.
7. Return to any Cartographer in the ISSUING village. The dialogue should switch to the turn-in state.
8. Choose "The survey is finished." Expected reward: 5 emeralds + one spyglass + 5 XP points, plus one completion added to the same Bountiful board ledger used for Village Trust.
9. Speak to the Cartographer again. This first-clear survey should not be re-offered to the same player in that village; normal trading should remain available.

Nearby-structure eligibility
- The initial legal target families are Dungeons Enhanced Watch Tower, Valhelsia Tower Ruin, and Dungeons Enhanced Stables.
- A structure-dependent quest is withheld if no legal nearby instance resolves.
- The locate happens lazily when the Cartographer needs an offer and is cached. There is no background structure-search tick.
- The structure instance is frozen into the accepted quest state. This is the foundation for the larger Master Bible rule: villages should talk about what is actually near them.

Existing 0.2.1 board behavior remains in this build
- authored titles on board hover;
- ordinary radiant pay uses emeralds rather than generic food roulette;
- roughly 120-second gradual board rotation;
- same-board bounty redemption;
- outdoor/path-biased automatic board placement and throttled in-village board locator;
- current prototype Bountiful recovery contracts and proof loot remain for comparison, but they are now considered transitional content rather than the final story-delivery architecture.

Known limitations of 0.3.0
- Only the Cartographer survey lifecycle has been moved to Conversations so far. This is deliberate: validate one complete lifecycle before importing hundreds of Master Bible hooks.
- Conversations 1.0.5 integration is reflection-based and CI can compile it without the third-party jar, but the actual UI/action handshake must be smoke-tested in the real modpack.
- A Conversations villager carries one dialogue id at a time. 0.3.0 selects that id immediately before interaction; multiplayer simultaneous use of the exact same villager is a later hardening target.
- The survey detects arrival near the resolved structure position; it does not yet require a photograph or room-level interaction.
- The old weighted Bountiful Recovery Contracts remain present during this transition and will later be replaced by deterministic Trust/featured contracts.

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
