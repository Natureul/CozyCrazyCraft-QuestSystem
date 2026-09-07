#!/usr/bin/env python3
"""Protect 0.4.1 playtest fixes that are easy to accidentally reintroduce.

This is intentionally source-level. The failure modes are architectural regressions: putting authored
structure surveys back on a locator-radius tick, mirroring NPC offer prose into the action bar, or
marking underground structure centers as if they were entrances.
"""

from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "runtime" / "src" / "main" / "java" / "com" / "natureul" / "cozycrazyquests"

errors: list[str] = []


def read(name: str) -> str:
    path = JAVA / name
    if not path.exists():
        errors.append(f"missing runtime source: {path.relative_to(ROOT)}")
        return ""
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, label: str) -> None:
    if token not in text:
        errors.append(f"{label}: missing required invariant token {token!r}")


def forbid(text: str, token: str, label: str) -> None:
    if token in text:
        errors.append(f"{label}: forbidden regression token present: {token!r}")


def main() -> int:
    manager = read("VillageConversationQuestManager.java")
    mod = read("CozyCrazyQuests.java")
    survey = read("StructureSurveyCompletionBridge.java")
    places = read("NamedPlaceBridge.java")

    # Structure visits have one authority: physical exact-structure proof. The old manager tick used
    # horizontal distance to the locate coordinate and caused the Amber Rest early-completion failure.
    forbid(manager, "public static void onPlayerTick(", "VillageConversationQuestManager")
    forbid(mod, "VillageConversationQuestManager::onPlayerTick", "CozyCrazyQuests event registration")
    require(mod, "StructureSurveyCompletionBridge::onPlayerTick", "CozyCrazyQuests event registration")
    require(survey, "QUALIFYING_DWELL_TICKS", "StructureSurveyCompletionBridge")
    require(survey, "NamedPlaceBridge.insideExactStructure", "StructureSurveyCompletionBridge")
    require(places, "insideAnyPiece(current, playerPos)", "NamedPlaceBridge exact occupancy")

    # NPC offers belong in Conversations. The interaction handler must not immediately mirror the
    # destination/distance into a second action-bar surface.
    start = manager.find("public static void onEntityInteract(")
    end = manager.find("public static void onLivingDeath(")
    if start < 0 or end <= start:
        errors.append("VillageConversationQuestManager: could not isolate onEntityInteract")
    else:
        interaction = manager[start:end]
        forbid(interaction, "displayClientMessage(", "authored quest offer interaction")
        forbid(interaction, " • ", "authored quest offer interaction")

    # Objective anchor and navigation anchor are deliberately different for underground/submerged work.
    require(manager, "navigationAnchorFor(", "VillageConversationQuestManager Atlas routing")
    require(manager, "NamedPlaceBridge.surfaceApproach(", "VillageConversationQuestManager Atlas routing")
    require(places, "BlockPos navigationAnchor", "NamedPlaceBridge Atlas overload")
    require(places, "navigationAnchor == null ? identity.markerPos()", "NamedPlaceBridge Atlas overload")

    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} runtime UI/structure invariant regression(s)")
        return 1

    print("OK: structure-proof, Conversations-first offer UI, and navigation-anchor invariants validated")
    return 0


if __name__ == "__main__":
    sys.exit(main())
