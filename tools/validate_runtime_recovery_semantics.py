#!/usr/bin/env python3
"""Static guardrails for executable authored recovery-quest binding and turn-in semantics.

The 0.4.0 field test proved that "enter structure -> mint evidence into inventory" is not an acceptable
player-facing recovery interaction. This validator therefore protects identity, persistence and consumption
without blessing that temporary P0 implementation shortcut. While the shortcut still exists it is reported
as KNOWN DEBT; the integration branch can remove it without having to weaken CI first.
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
require(bridge, "boolean recovery = definition.isRecovery();", "structure visit bridge recognizes recovery jobs")
require(bridge, "!recovery && !physicallyInside", "old discovery records cannot substitute for recovery entry")
require(evidence, '"ccc_recovery_quest"', "evidence binds to quest id")
require(evidence, '"ccc_recovery_village"', "evidence binds to issuing village")
require(evidence, '"ccc_recovery_target"', "evidence binds to target instance key")
require(evidence, "stack.is(ModItems.RECOVERED_EVIDENCE.get())", "evidence match uses exact item type")
require(evidence, "questId.equals(tag.getString(QUEST_ID))", "evidence match checks quest id")
require(evidence, "villageKey.equals(tag.getString(VILLAGE_KEY))", "evidence match checks village id")
require(turnin, "RecoveredEvidence.consume(player, active)", "turn-in consumes the matching recovered object")
require(token, "RecoveryQuestRuntime.beforeTurnIn(player)", "Conversations turn-in is recovery-guarded")
require(named_place, "if (start == null || !start.isValid()) return null;", "exact identity is never fabricated from a locator")

if "getHoverName" in evidence or "getDisplayName" in evidence:
    raise SystemExit("ERROR: recovery evidence matching must never use a display name")

if "RecoveredEvidence.create(definition, active)" in bridge:
    print(
        "KNOWN P0: recovery evidence is still created by StructureSurveyCompletionBridge on occupancy. "
        "This is intentionally NOT a required invariant; replace it with real container/interactable evidence."
    )

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

print(f"OK: recovery identity/turn-in semantics validated ({len(quests)} structure-bound jobs, 12 dialogue files)")
