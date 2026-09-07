#!/usr/bin/env python3
"""Static guardrails for the executable authored recovery-quest runtime.

The data-level recovery contract validator predates the Conversations runtime. This validator protects
what the player actually experiences now: a local structure must exist, a recovery job must require
physical entry into the assigned generated instance, the recovered object must be quest-bound, and the
old proximity fallback must never complete a recovery job merely because the player reached a locator
coordinate.
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
require(bridge, "RecoveredEvidence.create(definition, active)", "exact structure entry creates quest-bound evidence")
require(evidence, '"ccc_recovery_quest"', "evidence binds to quest id")
require(evidence, '"ccc_recovery_village"', "evidence binds to issuing village")
require(evidence, '"ccc_recovery_target"', "evidence binds to target instance key")
require(turnin, "RecoveredEvidence.consume(player, active)", "turn-in consumes the matching recovered object")
require(token, "RecoveryQuestRuntime.beforeTurnIn(player)", "Conversations turn-in is recovery-guarded")
require(named_place, "if (start == null || !start.isValid()) return null;", "exact identity is never fabricated from a locator")

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

print(f"OK: executable recovery semantics validated ({len(quests)} structure-bound jobs, 12 dialogue files)")
