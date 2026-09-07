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

README = """CozyCrazyCraft Quest System — 0.4.0 SOCIAL PROGRESSION / AUTHORED QUEST PLAYTEST

Target: Minecraft Forge 1.20.1
Built for the current CozyCrazyCraft instance.

Requires your normal installed pack, including:
- Bountiful 6.0.4
- Conversations 1.0.5 and Lazr's Lib
- your current CozyCrazyZones build
- the structure, creature, reward, Atlas, and utility mods already present in CozyCrazyCraft

IMPORTANT WHEN UPGRADING
1. Remove every older mods/CozyCrazyQuests-*.jar from the instance.
2. Copy this ZIP's config/ and mods/ folders into the Minecraft instance root and merge/replace when asked.
3. Keep your normal third-party mod jars. They are deliberately NOT bundled here.
4. This overlay contains CozyCrazyCraft-owned Bountiful configuration plus CozyCrazyQuests-0.4.0.jar.
5. Existing worlds/player state are expected to survive the upgrade, but this is a playtest branch: back up a world you care about before testing progression changes.

WHAT 0.4.0 IS NOW
The old Cartographer-only proof has grown into an authored village social-progression runtime.
Bountiful remains the civic/public notice layer, while meaningful village work can now be offered and discussed directly by professions through Conversations.

The runtime now includes:
- Hearthlands authored village progression;
- Frontier authored expedition and side-work catalogs;
- the first executable Wildlands authored contracts and Great Hunts;
- multiple simultaneous village-local contracts without making one NPC UUID critical;
- profession-aware referrals and active-quest clue networks;
- persistent village/place identity shared with CozyCrazyZones and the Atlas;
- explicit player knowledge states: UNKNOWN -> RUMOR -> LEAD -> KNOWN -> CONFIRMED;
- exact generated-structure targeting and occupancy checks for surveys/recoveries;
- quest-bound recovered evidence for structure recovery jobs;
- exact-creature boss hunt semantics where a verified hunt is enabled;
- stable per-villager ambient dialogue variants and rare child rumor/help routes;
- physical Village Contracts that preserve issuing village, objective, direction, approach, and return information.

CORE PROGRESSION RULE
Progression is semantic, not a raw quest counter. Hearthlands trust is built through different kinds of contribution such as COMMUNITY, EXPLORATION, PROFESSION, and DANGER work. Repeating one easy verb is not intended to substitute for broad village involvement.

SOCIAL ROUTING TEST
1. Enter an inhabited village and speak to several adult professions rather than only the Cartographer.
2. A villager with relevant authored work may offer it through Conversations. Ordinary villagers should still have profession/ambient dialogue instead of every person becoming a quest dispenser.
3. Accept a structure contract. Inspect the Village Contract tooltip: it should preserve the actual issuing village, objective, initial bearing, approach information, and valid return professions.
4. Ask other residents about the active job. Cartographers, masons, librarians/clerics, smiths, fishermen, guards, and other residents should vary in what they plausibly know.
5. Referral dialogue should route you to a named useful resident when one is loaded. If no specialist is available, the fallback lead must still prevent the quest from becoming a dead end.
6. IMPORTANT 0.4.0 FIX: referrals and fallback guides are scoped to the issuing VillageContext. A nearby second village must not donate its residents to the first village's clue network.
7. Rumor-level knowledge must not silently create a precise Atlas pin. More authoritative local/map evidence may identify or mark a place.

STRUCTURE / RECOVERY TEST
- Structure-dependent work is withheld if no legal real target can be resolved within its bounded search policy.
- Accepted structure instances are frozen into quest state; the target must not jump after relogging or discovering another structure.
- Underground/submerged locator Y values are navigation hints, not fake room objectives.
- Normal structure surveys complete from exact generated-structure occupancy, with a compatibility path for older non-recovery discovery state.
- Recovery contracts require physically entering the exact assigned structure instance. Only then is the quest-bound recovered object created.
- Return the object to an accepted profession in the named issuing village; the system must not require the original NPC UUID to survive.

WILDLANDS PLAYTEST SLICE
Verified executable Wildlands content currently includes:
- Frostmarch: The Sleeping Mountain — exact Frostmaw Great Hunt at a real Frostmaw spawn structure; signature reward White Reach.
- Frostmarch: The Last Warm Camp — recover a marked stove plate from an Ice Pit; Cold Sweat expedition utility rewards.
- Greenveil: Temple of Eight Roots — recover a seed reliquary from a real jungle-temple/monument target; signature reward Greenwake.
- Sunscar: The Sunbird — exact Umvuthi/Umvuthana Great Hunt at the real grove; signature reward Sunspike.

INTENTIONAL ENCOUNTER RESTRAINT
Not every dangerous creature is a radiant kill target.
- The Tunnel Gore path treats the ore-rich underground lair as the discovery/reward. Killing the Gore is explicitly NOT the objective.
- West Pumpkinhead content remains out of ordinary random rotation until its authoritative encounter/spawn path is suitable for authored progression.
- The eastern Jungle Abomination final remains feature-gated rather than inventing a fake boss path.
- Ancient Remnant remains feature-gated as well.
Asymmetry here is intentional: verified encounters are preferable to fabricated regional symmetry.

TUNNEL GORE / DEEP ROAD TEST
- A rare child rumor can point toward a real nearby Tunnel Gore lair, but children are not required progression routers.
- Once locally Recognized, appropriate stone/tool specialists can independently reveal the deep-road lead if a real lair exists.
- A specialist can mark the surface reference on the Atlas and give a small torch courtesy; the ore-rich location is the real reward.
- Entering the actual lair confirms the discovery. No Gore kill counter should appear.

NAMED-PLACE / ATLAS RULE
CozyCrazyZones owns persistent world-global village/place identity. CozyCrazyQuests reuses that identity instead of inventing quest-only duplicate names. A structure should retain the same name in dialogue, contract text, Atlas discovery, and later references.

BOUNTIFUL ROLE
Bountiful remains useful for civic notices, field work, trust-adjacent public tasks, and compatibility. It is no longer expected to carry the entire authored progression experience by itself.
The deployment baseline intentionally excludes default Bountiful pool/decree content so CozyCrazyCraft's custom civic layer controls what appears.

CURRENT CONTENT / VALIDATION SNAPSHOT
CI for this package validates:
- 108 authored/story-capable quest definitions;
- 16 regional repeatable field jobs;
- 32 live ordinary objective cards under the one-objective/payment policy;
- four structure-bound recovery contracts;
- the executable Wildlands structure jobs and exact Great Hunts;
- target-distance/tier/macro legality;
- semantic trust and knowledge-state policy;
- Conversations dialogue schema/copy limits;
- Bountiful configuration and story metadata;
- Java compilation and integrated overlay packaging.

KNOWN PLAYTEST NOTES
- Conversations integration uses runtime/reflection bridges because the third-party Conversations jar is not bundled into this repository. The CI build proves our side compiles and packages; the real UI/action handshake still deserves in-client smoke testing whenever Conversations changes.
- Conversations still permits only one dialogue id on an entity at a time. Simultaneous multiplayer interaction with the exact same NPC remains a later hardening target.
- Four prototype Bountiful command-map rewards (jungle temple, Frostmaw, Umvuthi, stables) remain yellow-path features that should be explicitly smoke-tested in the real pack before being treated as final shipping behavior.
- The checked-in static world-binding audit snapshot originated from CozyCrazyZones 0.3.6. The runtime ZoneBridge calls CozyZonesApi.regionalCellAt reflectively; that API remains the compatibility boundary for newer CozyCrazyZones builds.
- Dread Reaches is not being filled with invented legendary content merely to complete a grid. Continue validating actual structures/encounters first.

FAST SMOKE TEST ORDER
1. Remove the old CozyCrazyQuests jar and install this overlay.
2. Launch an existing test world and confirm no duplicate CozyCrazyQuests jars are loaded.
3. Talk to multiple villagers in one Hearthlands village; verify ambient/profession variation and authored offers.
4. Accept one structure quest, ask several residents for help, and inspect the physical contract.
5. If two villages are near each other, deliberately test that referrals stay inside the issuing village.
6. Complete a structure survey/recovery and turn it in to an accepted profession in the issuing village.
7. Test a Frontier village for expedition-style authored work.
8. If convenient, test one verified Wildlands contract/Great Hunt rather than forcing every region in one session.
9. Test the Tunnel Gore rumor/lead only as a discovery path; do not expect a kill objective.
10. Report any dialogue that feels repetitive, any target that is absurdly distant/wrong-tier, any contract that loses its village/place identity, or any case where a player can become stranded without a usable lead.

Build identity: CozyCrazyQuests 0.4.0 — social-progression / authored-quest playtest.
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
