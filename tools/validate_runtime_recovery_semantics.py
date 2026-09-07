#!/usr/bin/env python3
"""Static guardrails for executable authored recovery-quest binding and replacement semantics.

A recovery job has one player-visible truth: a quest-bound object found through a physical container
interaction in the exact assigned generated structure, then taken into inventory and consumed at turn-in.
Replacement is explicit and generation-bound: voiding a lost copy makes any old copy invalid before a new
physical cache can be seeded.
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
runtime = text(JAVA / "RecoveryQuestRuntime.java")
token = text(JAVA / "ConversationTokenItem.java")
support = text(JAVA / "RecoverySupportConversationManager.java")
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
require(manager, 'objective = "recover " + definition.recoveryObjectName()', "acceptance text says recover, not survey")

if "public static void onPlayerTick(" in manager:
    raise SystemExit("ERROR: VillageConversationQuestManager must not own locator/proximity survey ticking")
if "VillageConversationQuestManager::onPlayerTick" in mod_main:
    raise SystemExit("ERROR: obsolete VillageConversationQuestManager survey tick must not be registered")
require(mod_main, "StructureSurveyCompletionBridge::onPlayerTick", "physical structure survey bridge is registered")

require(runtime, "PlayerInteractEvent.RightClickBlock", "recovery starts from a physical block interaction")
require(runtime, "instanceof RandomizableContainerBlockEntity container", "recovery source is a loot/storage cache")
require(runtime, "NamedPlaceBridge.insideExactStructure", "recovery container must be in the exact assigned structure")
require(runtime, "RecoveredEvidence.create(definition, active)", "physical recovery source creates quest-bound evidence")
require(runtime, "container.setItem(emptySlot, evidence)", "evidence is inserted into the opened physical container")
require(runtime, '"recovery_evidence_seeded"', "one active generation does not repeatedly seed duplicate evidence")
require(runtime, "RecoveredEvidence.has(player, active)", "objective waits until the player actually possesses evidence")
require(runtime, 'active.putBoolean("objective_complete", true)', "physical evidence possession completes recovery")
require(runtime, "RecoveredEvidence.consume(player, active)", "turn-in consumes the matching recovered object")
require(runtime, 'static final String GENERATION = "recovery_evidence_generation"', "active recovery state has a replacement generation")
require(runtime, "invalidateCurrentGeneration(active)", "lost evidence invalidates old generation before reseeding")
require(runtime, "active.remove(SEEDED)", "reset permits a fresh physical cache binding")
require(runtime, "static boolean resetLostEvidence", "player-authorized replacement action exists")
if "player.addItem(evidence)" in runtime or "player.drop(evidence" in runtime:
    raise SystemExit("ERROR: recovery evidence must not bypass the physical cache")
require(mod_main, "RecoveryQuestRuntime::onRightClickBlock", "physical recovery interaction is registered")
require(mod_main, "RecoveryQuestRuntime::onPlayerTick", "physical recovery possession check is registered")
require(mod_main, "RecoverySupportConversationManager::onEntityInteract", "missing-evidence Conversations support is registered")
require(mod_main, "EventPriority.LOWEST", "missing-evidence support runs as a narrow final override")

if "RecoveredEvidence.create(definition, active)" in bridge:
    raise SystemExit("ERROR: structure occupancy bridge must not mint recovery evidence")
require(bridge, "|| definition.isRecovery()) continue;", "ordinary structure survey bridge excludes recovery jobs")

require(evidence, '"ccc_recovery_quest"', "evidence binds to quest id")
require(evidence, '"ccc_recovery_village"', "evidence binds to issuing village")
require(evidence, '"ccc_recovery_target"', "evidence binds to target instance key")
require(evidence, '"ccc_recovery_generation"', "evidence binds to current replacement generation")
require(evidence, "generation != tag.getInt(GENERATION)", "old generations cannot satisfy current contract")
require(evidence, "stack.is(ModItems.RECOVERED_EVIDENCE.get())", "evidence match uses exact item type")
require(evidence, "questId.equals(tag.getString(QUEST_ID))", "evidence match checks quest id")
require(evidence, "villageKey.equals(tag.getString(VILLAGE_KEY))", "evidence match checks village id")
require(token, '"recovery_reset".equals(action)', "Conversations token dispatches explicit replacement reset")
require(token, "RecoveryQuestRuntime.beforeTurnIn(player)", "Conversations turn-in is recovery-guarded")
require(support, "RecoveryQuestRuntime.needsReplacementSupport", "support appears only for seeded missing evidence")
require(support, "definition.accepts", "support remains on plausible return contacts")
require(named_place, "if (start == null || !start.isValid()) return null;", "exact identity is never fabricated from a locator")
require(named_place, "insideAnyPiece(current, playerPos)", "exact structure proof requires a real structure piece")

if "getHoverName" in evidence or "getDisplayName" in evidence:
    raise SystemExit("ERROR: recovery evidence matching must never use a display name")

support_dialogue = text(DIALOGUES / "recovery_evidence_missing.json")
require(support_dialogue, 'ccc_action:\\"recovery_reset\\"', "support dialogue exposes explicit void/re-search choice")
require(support_dialogue, "first copy", "support dialogue lets player decline reset and retrieve original")

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

print("OK: physical recovery identity, generation-safe replacement, and turn-in semantics validated")
