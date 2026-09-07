#!/usr/bin/env python3
"""Guard the executable contextual ambient voice layer added for 0.4.1."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "runtime" / "src" / "main" / "java" / "com" / "natureul" / "cozycrazyquests"
CONV = ROOT / "runtime" / "src" / "main" / "resources" / "data" / "cozycrazyquests" / "conversations"

manager = (JAVA / "RegionalAmbientConversationManager.java").read_text(encoding="utf-8")
mod = (JAVA / "CozyCrazyQuests.java").read_text(encoding="utf-8")

for token in (
    "VillageProgressState.Trust",
    "getDayTime() / 24000L",
    '"SHARED_CORE".equals(cell.band())',
    'case "NORTH" -> "north"',
    'case "EAST" -> "east"',
    'case "SOUTH" -> "south"',
    'case "WEST" -> "west"',
    'case "HEARTHLANDS" -> "hearthlands"',
    'case "FRONTIER" -> "frontier"',
    'case "WILDLANDS" -> "wildlands"',
    '"DREAD_REACHES", "DREAD"',
    "GoreTunnelLead.adultDialogue",
    "VillageQuestState.recentCompletion",
    "COMPLETION_MEMORY_TICKS = 72000L",
    "definition.accomplishmentCategory()",
):
    if token not in manager:
        raise SystemExit(f"ERROR: contextual ambient manager missing invariant {token!r}")

regional_registration = mod.find("RegionalAmbientConversationManager::onEntityInteract")
social_registration = mod.find("VillageSocialConversationManager::onEntityInteract")
if regional_registration < 0:
    raise SystemExit("ERROR: contextual ambient manager is not registered")
if social_registration < 0 or regional_registration > social_registration:
    raise SystemExit("ERROR: contextual ambient manager must run before the ordinary social fallback")
if "EventPriority.NORMAL, RegionalAmbientConversationManager::onEntityInteract" not in mod:
    raise SystemExit("ERROR: contextual ambient manager should run at NORMAL priority")
if "EventPriority.LOW, VillageSocialConversationManager::onEntityInteract" not in mod:
    raise SystemExit("ERROR: ordinary social fallback should run after contextual voice at LOW priority")

regional = {"ambient_region_shared_core_hearthlands"}
for macro in ("north", "east", "south", "west"):
    for tier in ("hearthlands", "frontier", "wildlands", "dread_reaches"):
        regional.add(f"ambient_region_{macro}_{tier}")
completion = {f"ambient_completion_{category}" for category in ("community", "exploration", "profession", "danger")}

for stem in sorted(regional | completion):
    path = CONV / f"{stem}.json"
    if not path.is_file():
        raise SystemExit(f"ERROR: missing contextual ambient resource {path.name}")
    data = json.loads(path.read_text(encoding="utf-8"))
    beats = [
        option.get("dialogue", "")
        for page in data.get("dialogues", [])
        for option in page.get("dialogue_options", [])
    ]
    if len(beats) < 2:
        raise SystemExit(f"ERROR: {path.name} needs at least two contextual observations")
    for beat in beats:
        words = len(re.findall(r"\b[\w’'-]+\b", beat))
        if words > 25:
            raise SystemExit(f"ERROR: {path.name} ambient beat is {words} words; ambient should stay short")
    raw = path.read_text(encoding="utf-8")
    if 'ccc_action:\\"route_help\\"' not in raw:
        raise SystemExit(f"ERROR: {path.name} has no useful route-help exit")
    if "mark_active_target" in raw or "quest_hint_identify" in raw:
        raise SystemExit(f"ERROR: {path.name} must not fabricate Atlas/knowledge authority")

print(f"OK: {len(regional)} region/tier + {len(completion)} recent-completion ambient resources validated")
