#!/usr/bin/env python3
"""Prevent two different quest-looking objects from occupying one authored recovery site."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "runtime" / "src" / "main" / "java" / "com" / "natureul" / "cozycrazyquests"
POOLS = ROOT / "deployment" / "config" / "bountiful" / "bounty_pools"
LANG = ROOT / "runtime" / "src" / "main" / "resources" / "assets" / "cozycrazyquests" / "lang" / "en_us.json"

injector = (JAVA / "ProofLootInjector.java").read_text(encoding="utf-8")

# These two prototypes directly collide with current authored recovery sites and must stay retired.
for forbidden in (
    'chests/jungle_monument/treasure',
    'chests/ice_pit/armory',
    'ModItems.GREENVEIL_SURVEY_NOTES',
    'ModItems.FROSTMARCH_DISPATCH',
):
    if forbidden in injector:
        raise SystemExit(f"ERROR: retired overlapping public proof returned to ProofLootInjector: {forbidden}")

north = json.loads((POOLS / "ccc_hearth_north_objs.json").read_text(encoding="utf-8"))["content"]
east = json.loads((POOLS / "ccc_hearth_east_objs.json").read_text(encoding="utf-8"))["content"]
for qid in ("ccc_north_h1_recover_dispatch", "ccc_east_h1_recover_survey_notes"):
    if qid in north or qid in east:
        raise SystemExit(f"ERROR: retired cross-tier public recovery notice is live: {qid}")

# Remaining static public proofs are allowed only where no executable villager recovery targets the same
# structure family. This scans every Java recovery(...) definition, including regional catalogs.
remaining_proof_structures = {
    "dungeons_enhanced:stables": "Stablemaster's Seal",
    "dungeons_enhanced:desert_tomb": "Sunscar Tomb Tablet",
}
for path in JAVA.glob("*QuestCatalog.java"):
    source = path.read_text(encoding="utf-8")
    for block in re.findall(r"\brecovery\((.*?)(?=\n\s*\);)", source, flags=re.S):
        for structure, proof in remaining_proof_structures.items():
            if f'"{structure}"' in block:
                raise SystemExit(
                    f"ERROR: {path.name} authored recovery overlaps static public proof {proof} at {structure}"
                )

lang = json.loads(LANG.read_text(encoding="utf-8"))
hint = lang.get("item.cozycrazyquests.quest_proof_hint", "")
if "Notice-board" not in hint or "unrelated to villager recovery" not in hint:
    raise SystemExit("ERROR: public proof tooltip must explicitly distinguish notice-board proof from villager recovery")

print("OK: public notice proofs and authored physical recovery sites have one-player-visible-truth separation")
