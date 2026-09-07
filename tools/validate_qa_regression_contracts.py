#!/usr/bin/env python3
"""0.4.1 QA regression contract.

This is intentionally a static/fixture validator: Forge game tests are not available in CI yet.
It protects invariants that can be proven from source/data and reports known P0/P1 debt without
pretending the base 0.4.0 runtime already satisfies the new field-playtest acceptance criteria.
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
TARGETING = ROOT / "data" / "targeting_policy.json"

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
        "STRUCT-SURFACE-OUTSIDE",
        "STRUCT-UNDERGROUND-ABOVE",
        "STRUCT-HUGE-START-BOX",
        "STRUCT-WRONG-INSTANCE",
        "STRUCT-LOCATOR-OUTSIDE-BOX",
        "STRUCT-PIECE-VS-START",
        "STRUCT-UNLOADED-CHUNK",
        "REC-AUTO-GRANT",
        "REC-DUPLICATE",
        "REC-SIMILAR-NAME",
        "REC-INVENTORY-FULL",
        "REC-DEATH-RELOG",
        "REC-WRONG-QUEST",
        "REC-WRONG-VILLAGE",
        "REC-NOT-CONSUMED",
        "REC-SIMULTANEOUS",
        "NAV-RUMOR-NO-PIN",
        "NAV-KNOWN-CAN-MARK",
        "NAV-SAFE-OBJECTIVE-MARKER",
        "NAV-UNDERGROUND-APPROACH",
        "NAV-GORE-SEMANTICS",
        "SOC-NO-PROFESSIONS",
        "SOC-TINY-VILLAGE",
        "SOC-GIVERS-DIE",
        "SOC-CLOSE-VILLAGES",
        "SOC-CROSS-VILLAGE-REFERRAL",
        "SOC-WRONG-VILLAGER",
        "SOC-COMPLETE-HINTS-OFF",
        "SOC-HINT-VARIATION",
        "VAR-SAME-INSTANCE",
        "VAR-SAME-REWARD",
        "VAR-SAME-VERB",
        "VAR-RECYCLE-COMPLETED",
        "UI-ACTIONBAR-LENGTH",
        "UI-NPC-PROSE-SURFACE",
        "UI-CONVERSATION-LENGTH",
        "UI-CONVERSATION-STATES",
        "PERF-LOCATE-ON-INTERACT",
        "PERF-CACHE-BOUNDARY",
        "PERF-WATCHDOG-STACK",
    }
    ids = {case.get("id") for case in cases if isinstance(case, dict)}
    missing = required - ids
    if missing:
        errors.append("qa regression matrix missing cases: " + ", ".join(sorted(missing)))

    allowed_status = {"GUARDED", "KNOWN_DEBT", "MANUAL"}
    for case in cases:
        if not isinstance(case, dict):
            errors.append("qa regression matrix contains non-object case")
            continue
        if case.get("status") not in allowed_status:
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
    turnin = load_text(JAVA / "RecoveryQuestRuntime.java")
    state = load_text(JAVA / "VillageQuestState.java")

    require(manager, 'target_start_chunk_x', "accepted structure contracts persist exact start-chunk identity when known")
    require(manager, 'target_start_chunk_z', "accepted structure contracts persist exact start-chunk identity when known")
    require(places, 'currentStart.x == expectedStartChunkX && currentStart.z == expectedStartChunkZ',
            "exact structure completion checks assigned start chunk")
    require(manager, '("UNDERGROUND".equals(approach) || "SUBMERGED".equals(approach))',
            "generic horizontal fallback is skipped for underground/submerged targets")

    for key in ('"ccc_recovery_quest"', '"ccc_recovery_village"', '"ccc_recovery_target"'):
        require(recovery, key, f"recovery evidence carries {key}")
    require(recovery, 'stack.is(ModItems.RECOVERED_EVIDENCE.get())', "recovery accepts exact evidence item type")
    require(recovery, 'questId.equals(tag.getString(QUEST_ID))', "recovery matches quest id")
    require(recovery, 'villageKey.equals(tag.getString(VILLAGE_KEY))', "recovery matches village id")
    require(recovery, 'targetKey.equals(tag.getString(TARGET_KEY))', "recovery matches target id")
    if "getHoverName" in recovery or "getDisplayName" in recovery:
        errors.append("recovery evidence matching must not use display names")
    require(recovery, 'shrink(1)', "recovery turn-in consumes one evidence item")
    require(turnin, 'RecoveredEvidence.consume(player, active)', "turn-in is evidence guarded")
    require(state, 'active_by_village', "simultaneous contracts are village-keyed")
    require(state, 'static List<CompoundTag> allActives', "runtime can process multiple simultaneous contracts")
    require(manager, 'onPlayerClone', "authored quest state survives player clone/death")

    warn_if(
        'RecoveredEvidence.create(definition, active)' in survey,
        "P0-REC-AUTO-GRANT",
        "recovery evidence is still minted by structure occupancy instead of a real container/interactable",
    )
    warn_if(
        'current.getBoundingBox().isInside(playerPos)' in places
        and 'getPieces()' not in method_body(places, 'static boolean insideExactStructure'),
        "P0-STRUCT-WHOLE-BOX",
        "insideExactStructure still trusts the whole StructureStart bounding box rather than occupied pieces/dwell",
    )
    tick = method_body(manager, 'public static void onPlayerTick')
    warn_if(
        'dx * dx + dz * dz' in tick and 'objective_complete' in tick,
        "P0-STRUCT-SURFACE-RADIUS",
        "surface STRUCTURE_SURVEY completion still has a horizontal locator-radius path",
    )


def validate_navigation_and_social() -> None:
    hints = load_text(JAVA / "QuestHintNetwork.java")
    gore = load_text(JAVA / "GoreTunnelLead.java")
    village = load_text(JAVA / "VillageContext.java")

    rumor_case = re.search(r'case "quest_hint_rumor"\s*->\s*([^;]+);', hints)
    if not rumor_case or "Knowledge.RUMOR" not in rumor_case.group(1):
        errors.append("RUMOR action is not explicitly routed through RUMOR knowledge")
    give_lead = method_body(hints, 'private static boolean giveLead')
    if "revealStructureToAtlas" in give_lead:
        errors.append("RUMOR/LEAD giveLead path must not directly reveal an exact Atlas marker")
    require(hints, 'objectiveComplete(active)', "completed quests suppress social hint network")
    require(hints, 'village.key().equals(theirs.key())', "specialist/referral search rejects cross-village villagers")
    require(village, 'VillageNameCacheBridge.nearestAssigned', "routine village context avoids structure locate for names")
    require(village, 'findClosest(', "village context is anchored by nearby civic POI when available")

    warn_if(
        'NamedPlaceBridge.revealStructureToAtlas' in method_body(gore, 'private static boolean revealSpecialistLead'),
        "P0-NAV-GORE-CENTER",
        "Gore KNOWN lead still marks the structure locator/center rather than a verified safe approach",
    )
    warn_if(
        'DISCOVERY_RADIUS' in method_body(gore, 'static void onPlayerTick')
        and 'VERTICAL_TOLERANCE' in method_body(gore, 'static void onPlayerTick'),
        "P0-NAV-GORE-FALLBACK",
        "Gore discovery retains a broad radius/vertical fallback when exact occupancy is unavailable",
    )
    warn_if(
        'fallback|' in village and 'Math.floorDiv(anchor.getX(), 128)' in village,
        "P1-SOC-CLOSE-VILLAGES",
        "unnamed-village fallback identity is 128-block quantized and can collide for unusually close settlements",
    )


def validate_performance() -> None:
    resolver = load_text(JAVA / "NearbyStructureResolver.java")
    manager = load_text(JAVA / "VillageConversationQuestManager.java")
    gore = load_text(JAVA / "GoreTunnelLead.java")
    policy = {}
    try:
        policy = json.loads(TARGETING.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"targeting policy unreadable: {exc}")

    for path in JAVA.glob("*.java"):
        src = load_text(path)
        body = method_body(src, "onPlayerTick(")
        if body and "findNearestMapStructure" in body:
            errors.append(f"{path.name}: findNearestMapStructure must never run directly from onPlayerTick")
        if body and "NearbyStructureResolver.findNearest" in body:
            errors.append(f"{path.name}: NearbyStructureResolver.findNearest must never run directly from onPlayerTick")

    require(resolver, 'findNearestMapStructure(', "structure resolver uses a single identifiable expensive locate boundary")
    require(manager, 'TARGET_CACHE', "authored quest target lookup has a village/quest cache")
    require(manager, 'village.key() + ":" + definition.id()', "authored target cache key includes village and quest target")
    require(gore, 'CACHE', "Gore target lookup has a per-village cache")
    require(gore, 'village.key()', "Gore target cache is village scoped")

    candidate_search = policy.get("candidate_search", {}) if isinstance(policy, dict) else {}
    positive = candidate_search.get("positive_cache_ticks")
    runtime_match = re.search(r'TARGET_CACHE_LIFETIME\s*=\s*(\d+)L', manager)
    if isinstance(positive, int) and runtime_match:
        runtime_ttl = int(runtime_match.group(1))
        warn_if(
            runtime_ttl < positive,
            "P1-PERF-CACHE-TTL",
            f"runtime positive target cache is {runtime_ttl} ticks but targeting policy specifies {positive}",
        )

    interaction = method_body(manager, 'public static void onEntityInteract')
    warn_if(
        'selectOffer(' in interaction,
        "P0-PERF-INTERACT-LOCATE",
        "ordinary villager interaction can synchronously enter target selection; first-cache-miss structure locate can block server thread",
    )
    resolve_body = method_body(manager, 'private static NearbyStructureResolver.ResolvedStructure resolveStructureTarget')
    warn_if(
        'for (ResourceLocation candidate' in resolve_body and resolve_body.count('NearbyStructureResolver.findNearest') >= 2,
        "P1-PERF-RETRY-FANOUT",
        "mixed-family illegal-nearest fallback can fan one cache miss into 1+N synchronous structure locates",
    )


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
    warn_if(
        'player.displayClientMessage' in hints and '" It\'s underground;' in hints,
        "P0-UI-HINT-ACTIONBAR",
        "QuestHintNetwork still emits explanatory NPC prose through the action bar",
    )
    warn_if(
        'player.displayClientMessage' in gore and '"The child points "' in gore,
        "P0-UI-GORE-ACTIONBAR",
        "Gore social prose still uses the action bar",
    )
    warn_if(
        'returnInstruction(definition' in survey and 'displayClientMessage' in survey,
        "P0-UI-SURVEY-ACTIONBAR",
        "survey/recovery action-bar confirmation still appends return-direction prose",
    )


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
