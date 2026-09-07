#!/usr/bin/env python3
"""0.4.1 QA regression contract.

Static/fixture CI guardrails for the September field-test failures. The runtime has now moved authored
structure targeting away from synchronous worldgen locate calls and toward a persistent index of real
generated StructureStart instances, so this validator protects that architecture directly rather than
blessing the old expensive boundary.
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "runtime" / "src" / "main" / "java" / "com" / "natureul" / "cozycrazyquests"
CONVERSATIONS = ROOT / "runtime" / "src" / "main" / "resources" / "data" / "cozycrazyquests" / "conversations"
MATRIX = ROOT / "data" / "qa_regression_matrix.json"

errors: list[str] = []
warnings: list[str] = []


def load_text(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except Exception as exc:
        errors.append(f"{path.relative_to(ROOT)}: cannot read: {exc}")
        return ""


def require(text: str, needle: str, label: str) -> None:
    if needle not in text:
        errors.append(f"missing guard: {label}")


def forbid(text: str, needle: str, label: str) -> None:
    if needle in text:
        errors.append(f"forbidden regression: {label}")


def warn_if(condition: bool, debt_id: str, message: str) -> None:
    if condition:
        warnings.append(f"{debt_id}: {message}")


def method_body(source: str, signature_fragment: str) -> str:
    start = source.find(signature_fragment)
    if start < 0:
        return ""
    brace = source.find("{", start)
    if brace < 0:
        return ""
    depth = 0
    for i in range(brace, len(source)):
        c = source[i]
        if c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                return source[brace + 1:i]
    return ""


def validate_matrix() -> None:
    try:
        matrix = json.loads(MATRIX.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"data/qa_regression_matrix.json: invalid JSON: {exc}")
        return
    cases = matrix.get("cases")
    if not isinstance(cases, list):
        errors.append("qa_regression_matrix.json: cases must be a list")
        return

    required = {
        "STRUCT-SURFACE-OUTSIDE", "STRUCT-UNDERGROUND-ABOVE", "STRUCT-HUGE-START-BOX",
        "STRUCT-WRONG-INSTANCE", "STRUCT-LOCATOR-OUTSIDE-BOX", "STRUCT-PIECE-VS-START",
        "STRUCT-UNLOADED-CHUNK", "REC-AUTO-GRANT", "REC-DUPLICATE", "REC-SIMILAR-NAME",
        "REC-INVENTORY-FULL", "REC-DEATH-RELOG", "REC-WRONG-QUEST", "REC-WRONG-VILLAGE",
        "REC-NOT-CONSUMED", "REC-SIMULTANEOUS", "NAV-RUMOR-NO-PIN", "NAV-KNOWN-CAN-MARK",
        "NAV-SAFE-OBJECTIVE-MARKER", "NAV-UNDERGROUND-APPROACH", "NAV-GORE-SEMANTICS",
        "SOC-NO-PROFESSIONS", "SOC-TINY-VILLAGE", "SOC-GIVERS-DIE", "SOC-CLOSE-VILLAGES",
        "SOC-CROSS-VILLAGE-REFERRAL", "SOC-WRONG-VILLAGER", "SOC-COMPLETE-HINTS-OFF",
        "SOC-HINT-VARIATION", "VAR-SAME-INSTANCE", "VAR-SAME-REWARD", "VAR-SAME-VERB",
        "VAR-RECYCLE-COMPLETED", "UI-ACTIONBAR-LENGTH", "UI-NPC-PROSE-SURFACE",
        "UI-CONVERSATION-LENGTH", "UI-CONVERSATION-STATES", "PERF-LOCATE-ON-INTERACT",
        "PERF-CACHE-BOUNDARY", "PERF-WATCHDOG-STACK",
    }
    ids = {case.get("id") for case in cases if isinstance(case, dict)}
    missing = required - ids
    if missing:
        errors.append("qa regression matrix missing cases: " + ", ".join(sorted(missing)))

    for case in cases:
        if not isinstance(case, dict):
            errors.append("qa regression matrix contains non-object case")
            continue
        if case.get("status") not in {"GUARDED", "KNOWN_DEBT", "MANUAL"}:
            errors.append(f"{case.get('id')}: invalid status {case.get('status')!r}")
        if case.get("severity") not in {"P0", "P1", "P2"}:
            errors.append(f"{case.get('id')}: invalid severity")
        if not isinstance(case.get("expected"), str) or not case["expected"].strip():
            errors.append(f"{case.get('id')}: missing expected result")
        if not isinstance(case.get("repro"), list) or len(case["repro"]) < 2:
            errors.append(f"{case.get('id')}: needs at least two reproducible steps")


def validate_structure_and_recovery() -> None:
    manager = load_text(JAVA / "VillageConversationQuestManager.java")
    survey = load_text(JAVA / "StructureSurveyCompletionBridge.java")
    places = load_text(JAVA / "NamedPlaceBridge.java")
    recovery = load_text(JAVA / "RecoveredEvidence.java")
    runtime = load_text(JAVA / "RecoveryQuestRuntime.java")
    state = load_text(JAVA / "VillageQuestState.java")
    mod_main = load_text(JAVA / "CozyCrazyQuests.java")

    require(manager, 'target_start_chunk_x', "accepted contracts persist exact start chunk X")
    require(manager, 'target_start_chunk_z', "accepted contracts persist exact start chunk Z")
    require(places, 'insideAnyPiece(current, playerPos)', "exact occupancy requires a real structure piece")
    require(places, 'currentStart.x == expectedStartChunkX && currentStart.z == expectedStartChunkZ',
            "exact occupancy checks the assigned generated instance")
    require(survey, 'QUALIFYING_DWELL_TICKS', "structure surveys require sustained meaningful presence")
    require(survey, '|| definition.isRecovery()) continue;', "survey runtime does not impersonate recovery")
    require(mod_main, 'StructureSurveyCompletionBridge::onPlayerTick', "piece/dwell survey runtime is registered")
    forbid(mod_main, 'VillageConversationQuestManager::onPlayerTick',
           "legacy horizontal locator-radius survey tick must not be registered")

    for key in ('"ccc_recovery_quest"', '"ccc_recovery_village"', '"ccc_recovery_target"'):
        require(recovery, key, f"recovery evidence carries {key}")
    require(recovery, 'stack.is(ModItems.RECOVERED_EVIDENCE.get())', "recovery matches exact evidence item")
    require(recovery, 'questId.equals(tag.getString(QUEST_ID))', "recovery matches quest id")
    require(recovery, 'villageKey.equals(tag.getString(VILLAGE_KEY))', "recovery matches village id")
    require(recovery, 'targetKey.equals(tag.getString(TARGET_KEY))', "recovery matches target instance key")
    require(recovery, 'shrink(1)', "turn-in consumes one matching evidence item")
    require(runtime, 'PlayerInteractEvent.RightClickBlock', "evidence comes from a physical interaction")
    require(runtime, 'instanceof RandomizableContainerBlockEntity container', "recovery source is a real loot/storage cache")
    require(runtime, 'NamedPlaceBridge.insideExactStructure', "recovery container belongs to exact assigned structure")
    require(runtime, 'container.setItem(emptySlot, evidence)', "quest-bound evidence is placed in the physical cache")
    require(runtime, 'RecoveredEvidence.has(player, active)', "objective waits for evidence possession")
    require(runtime, 'RecoveredEvidence.consume(player, active)', "turn-in consumes exact quest-bound evidence")
    forbid(runtime, 'player.addItem(evidence)', "full recovery cache must not silently award evidence")
    require(mod_main, 'RecoveryQuestRuntime::onRightClickBlock', "physical recovery interaction is registered")
    require(state, 'active_by_village', "simultaneous contracts remain village-keyed")
    require(manager, 'onPlayerClone', "authored quest state survives death/clone")

    if "getHoverName" in recovery or "getDisplayName" in recovery:
        errors.append("recovery evidence matching must never use display names")
    if 'RecoveredEvidence.create(definition, active)' in survey:
        errors.append("structure occupancy bridge must never mint recovery evidence")


def validate_navigation_and_social() -> None:
    hints = load_text(JAVA / "QuestHintNetwork.java")
    gore = load_text(JAVA / "GoreTunnelLead.java")
    places = load_text(JAVA / "NamedPlaceBridge.java")
    village = load_text(JAVA / "VillageContext.java")

    rumor_case = re.search(r'case "quest_hint_rumor"\s*->\s*([^;]+);', hints)
    if not rumor_case or "Knowledge.RUMOR" not in rumor_case.group(1):
        errors.append("RUMOR action is not explicitly routed through RUMOR knowledge")
    give_lead = method_body(hints, 'private static boolean giveLead')
    if "revealStructureToAtlas" in give_lead:
        errors.append("RUMOR/LEAD giveLead path must not directly reveal an Atlas marker")
    require(hints, 'private static boolean markKnownTarget', "KNOWN reveal is isolated from RUMOR/LEAD")
    require(hints, 'NamedPlaceBridge.surfaceApproach', "underground KNOWN hint uses a surface approach")
    require(hints, 'objectiveComplete(active)', "completed quests suppress hint network")
    require(hints, 'village.key().equals(theirs.key())', "referrals reject cross-village villagers")

    require(places, 'BlockPos navigationAnchor', "Atlas API separates objective identity from navigation anchor")
    require(places, 'static BlockPos surfaceApproach', "runtime exposes safe surface approach geometry")
    require(gore, 'NamedPlaceBridge.surfaceApproach', "Gore lead computes a separate approach anchor")
    require(gore, 'readApproach(state)', "Gore Atlas reveal uses stored approach position")
    forbid(method_body(gore, 'static void onPlayerTick'), 'DISCOVERY_RADIUS',
           "Gore discovery must not use broad horizontal radius fallback")
    require(gore, 'QUALIFYING_DWELL_TICKS', "Gore confirmation requires sustained exact-piece presence")

    require(village, 'VillageNameCacheBridge.nearestAssigned', "routine village naming avoids structure locate")
    require(village, 'findClosest(', "village context uses nearby civic POI when available")
    warn_if(
        'fallback|' in village and 'Math.floorDiv(anchor.getX(), 128)' in village,
        "P1-SOC-CLOSE-VILLAGES",
        "unnamed-village fallback identity is still 128-block quantized",
    )


def validate_performance() -> None:
    resolver = load_text(JAVA / "NearbyStructureResolver.java")
    index = load_text(JAVA / "GeneratedStructureIndexSavedData.java")
    mod_main = load_text(JAVA / "CozyCrazyQuests.java")
    watchdog = load_text(ROOT / "tools" / "analyze_watchdog_log.py")

    # The field log showed synchronous locate paths during quest resolution, while the captured watchdog
    # itself was a teleport/chunk-load stall. Preventing broad locate calls here is a proactive latency
    # boundary, not an attribution of that watchdog to the quest runtime.
    for path in JAVA.glob("*.java"):
        src = load_text(path)
        if "findNearestMapStructure(" in src:
            errors.append(f"{path.name}: authored quest runtime must not call findNearestMapStructure")

    require(resolver, 'GeneratedStructureIndexSavedData.get(level).findNearest',
            "authored structure target resolution is an indexed in-memory lookup")
    require(index, 'ChunkEvent.Load', "generated structure index observes authoritative chunk loads")
    require(index, 'chunk.getAllStarts()', "generated structure index reads real generated starts")
    require(index, 'extends SavedData', "generated structure index persists across restarts")
    require(index, 'maxDistanceBlocks', "indexed nearest search remains distance bounded")
    require(mod_main, 'GeneratedStructureIndexSavedData::onChunkLoad', "structure index is registered")
    require(watchdog, 'QUEST_STRUCTURE_LOCATE', "watchdog classifier preserves structure-locate regression detection")


def validate_ui_and_conversations() -> None:
    if not CONVERSATIONS.is_dir():
        errors.append("missing Conversations directory")
        return

    phases: dict[str, set[str]] = {}
    dialogue_count = 0
    for path in sorted(CONVERSATIONS.glob("*.json")):
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except Exception as exc:
            errors.append(f"{path.name}: invalid JSON: {exc}")
            continue
        for container in data.get("dialogues", []):
            for option in container.get("dialogue_options", []):
                dialogue = option.get("dialogue")
                if not isinstance(dialogue, str):
                    continue
                dialogue_count += 1
                words = len(re.findall(r"\b[\w’'-]+\b", dialogue))
                if words > 45:
                    errors.append(f"{path.name}: {words}-word NPC beat exceeds hard QA ceiling of 45 words")
                elif words > 30:
                    warnings.append(f"P2-UI-LONG-BEAT: {path.name} has a {words}-word beat; Bible target is usually <=30")
                condition = option.get("condition")
                if not isinstance(condition, str) or not condition.strip():
                    errors.append(f"{path.name}: dialogue option lacks explicit state condition")

        match = re.match(r"(.+)_(offer|active|turnin)\.json$", path.name)
        if match:
            phases.setdefault(match.group(1), set()).add(match.group(2))

    for stem, seen in sorted(phases.items()):
        if "offer" in seen and seen != {"offer", "active", "turnin"}:
            warnings.append(f"P1-UI-STATE-COVERAGE: {stem} has {sorted(seen)}, expected offer/active/turnin")

    if dialogue_count == 0:
        errors.append("no Conversations dialogue beats found")

    hints = load_text(JAVA / "QuestHintNetwork.java")
    gore = load_text(JAVA / "GoreTunnelLead.java")
    survey = load_text(JAVA / "StructureSurveyCompletionBridge.java")
    if '" It\'s underground;' in hints:
        errors.append("QuestHintNetwork must not emit explanatory route prose through action bar")
    if '"The child points "' in gore:
        errors.append("Gore social prose must not use action bar")
    if 'returnInstruction(definition' in survey:
        errors.append("survey confirmation must not append return-direction prose")


def main() -> int:
    validate_matrix()
    validate_structure_and_recovery()
    validate_navigation_and_social()
    validate_performance()
    validate_ui_and_conversations()

    for warning in warnings:
        print("KNOWN DEBT:", warning)
    if errors:
        for error in errors:
            print("ERROR:", error)
        print(f"FAILED: {len(errors)} error(s), {len(warnings)} known-debt/warning(s)")
        return 1
    print(f"OK: 0.4.1 QA regression contract passed ({len(warnings)} known-debt/warning(s) surfaced)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
