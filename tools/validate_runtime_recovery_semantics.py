#!/usr/bin/env python3
"""Static guardrails for executable authored recovery-quest binding and turn-in semantics.

A recovery job must have one player-visible truth: a quest-bound object found through a physical container
interaction in the exact assigned generated structure, then taken into the player's inventory and consumed
at turn-in. Mere structure occupancy must never mint or complete recovery evidence.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "runtime" / "src" / "main" / "java" / "com" / "natureul" / "cozycrazyquests"
DIALOGUES = ROOT / "runtime" / "src" / "main" / "resources" / "data" / "cozycrazyquests" / "conversations"


def text(path: Path) -> str:
    if not path.is_file():
        raise SystemExit(f"ERROR: missing required runtime file: {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8")


def require(haystack: str, needle: str, label: str) -> None:
    if needle not in haystack:
        raise SystemExit(f"ERROR: recovery semantic guard missing: {label}")


catalog = text(JAVA / "HearthlandsRecoveryQuestCatalog.java")
quest_catalog = text(JAVA / "VillageQuestCatalog.java")
manager = text(JAVA / "VillageConversationQuestManager.java")
bridge = text(JAVA / "StructureSurveyCompletionBridge.java")
evidence = text(JAVA / "RecoveredEvidence.java")
turnin = text(JAVA / "RecoveryQuestRuntime.java")
token = text(JAVA / "ConversationTokenItem.java")
named_place = text(JAVA / "NamedPlaceBridge.java")
mod_main = text(JAVA / "CozyCrazyQuests.java")

quests = {
    "hearthlands_recovery_frozen_dispatch": "Sealed Watch Dispatch",
    "hearthlands_recovery_greenveil_folio": "Water-Stained Survey Folio",
    "hearthlands_recovery_sunscar_rubbing": "Carved Funerary Rubbing",
    "hearthlands_recovery_stable_ledger": "Weathered Stable Ledger",
}

for quest_id, object_name in quests.items():
    require(catalog, f'"{quest_id}"', f"catalog includes {quest_id}")
    require(catalog, f'"{object_name}"', f"catalog names recovery object for {quest_id}")

require(quest_catalog, "boolean isRecovery()", "quest definitions expose recovery specialization")
require(quest_catalog, "HearthlandsRecoveryQuestCatalog.ALL", "recovery bank is executable")
require(manager, "|| definition.isRecovery()) continue;", "legacy survey proximity fallback skips recovery jobs")
require(manager, 'objective = "recover " + definition.recoveryObjectName()', "acceptance text says recover, not survey")

require(turnin, "PlayerInteractEvent.RightClickBlock", "recovery starts from a physical block interaction")
require(turnin, "instanceof Container container", "recovery source is a real container")
require(turnin, "NamedPlaceBridge.insideExactStructure", "recovery container must be in the exact assigned structure")
require(turnin, "RecoveredEvidence.create(definition, active)", "physical recovery source creates quest-bound evidence")
require(turnin, "container.setItem(emptySlot, evidence)", "evidence is inserted into the opened physical container")
require(turnin, '"recovery_evidence_seeded"', "one active contract does not repeatedly seed duplicate evidence")
require(turnin, "RecoveredEvidence.has(player, active)", "objective waits until the player actually possesses evidence")
require(turnin, 'active.putBoolean("objective_complete", true)', "physical evidence possession completes recovery")
require(turnin, "RecoveredEvidence.consume(player, active)", "turn-in consumes the matching recovered object")
require(mod_main, "RecoveryQuestRuntime::onRightClickBlock", "physical recovery interaction is registered")
require(mod_main, "RecoveryQuestRuntime::onPlayerTick", "physical recovery possession check is registered")

if "RecoveredEvidence.create(definition, active)" in bridge:
    raise SystemExit("ERROR: structure occupancy bridge must not mint recovery evidence")
require(bridge, "|| definition.isRecovery()) continue;", "ordinary structure survey bridge excludes recovery jobs")

require(evidence, '"ccc_recovery_quest"', "evidence binds to quest id")
require(evidence, '"ccc_recovery_village"', "evidence binds to issuing village")
require(evidence, '"ccc_recovery_target"', "evidence binds to target instance key")
require(evidence, "stack.is(ModItems.RECOVERED_EVIDENCE.get())", "evidence match uses exact item type")
require(evidence, "questId.equals(tag.getString(QUEST_ID))", "evidence match checks quest id")
require(evidence, "villageKey.equals(tag.getString(VILLAGE_KEY))", "evidence match checks village id")
require(token, "RecoveryQuestRuntime.beforeTurnIn(player)", "Conversations turn-in is recovery-guarded")
require(named_place, "if (start == null || !start.isValid()) return null;", "exact identity is never fabricated from a locator")
require(named_place, "insideAnyPiece(current, playerPos)", "exact structure proof requires a real structure piece")

if "getHoverName" in evidence or "getDisplayName" in evidence:
    raise SystemExit("ERROR: recovery evidence matching must never use a display name")

stems = (
    "recovery_frozen_dispatch",
    "recovery_greenveil_folio",
    "recovery_sunscar_rubbing",
    "recovery_stable_ledger",
)
for stem in stems:
    for phase in ("offer", "active", "turnin"):
        path = DIALOGUES / f"{stem}_{phase}.json"
        if not path.is_file():
            raise SystemExit(f"ERROR: missing recovery dialogue: {path.relative_to(ROOT)}")

print(f"OK: physical recovery identity/turn-in semantics validated ({len(quests)} structure-bound jobs, 12 dialogue files)")
