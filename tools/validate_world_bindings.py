#!/usr/bin/env python3
"""Validate the machine-readable CozyCrazyZones contract and quest structure bindings.

This catches contradictions in our checked-in integration data. It does not launch
Minecraft, locate structures, or prove that a worldgen instance exists.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONTRACT = ROOT / "data" / "world_bindings" / "cozy_zones_0_3_6.json"
BINDINGS = ROOT / "data" / "world_bindings" / "quest_structure_bindings.json"
CATALOG = ROOT / "data" / "quest_catalog"

TIERS = ["HEARTHLANDS", "FRONTIER", "WILDLANDS", "DREAD_REACHES"]
TIER_INDEX = {name: idx for idx, name in enumerate(TIERS)}
REGIONS = {"CORE", "NORTH", "EAST", "SOUTH", "WEST"}
INFLUENCE = {"SHARED_CORE", "CARDINAL_TRANSITION", "ESTABLISHED"}

errors: list[str] = []
warnings: list[str] = []


def load(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"{path.relative_to(ROOT)}: invalid JSON: {exc}")
        return None


def catalog_ids() -> set[str]:
    result: set[str] = set()
    for path in CATALOG.glob("*.json"):
        data = load(path)
        if not isinstance(data, dict):
            continue
        for quest in data.get("quests", []):
            if isinstance(quest, dict) and isinstance(quest.get("id"), str):
                result.add(quest["id"])
    return result


def main() -> int:
    contract = load(CONTRACT)
    bindings = load(BINDINGS)
    quests = catalog_ids()

    if not isinstance(contract, dict) or not isinstance(bindings, dict):
        return finish()

    if contract.get("source", {}).get("version") != "0.3.6":
        errors.append("World binding contract must explicitly identify CozyCrazyZones 0.3.6")
    if bindings.get("zoning_contract") != "CozyCrazyZones-0.3.6":
        errors.append("Quest bindings must point to CozyCrazyZones-0.3.6")

    # The 0.3.6 starter information layer is now part of the handoff we intend
    # to build around, not merely documentation.
    starter = contract.get("starter_information") or {}
    if starter.get("first_village_preferred_min") != 1050 or starter.get("first_village_preferred_max") != 1250:
        errors.append("Unexpected 0.3.6 preferred first-village reservation range")
    for key in ("starter_desk_map_service", "starter_atlas_service", "starter_village_route_service"):
        if starter.get(key) is not True:
            errors.append(f"0.3.6 starter information contract missing enabled {key}")

    macro_names = contract.get("macro_regions") or {}
    if macro_names.get("WEST") != "Harvestwood":
        errors.append("CozyCrazyZones 0.3.6 WEST display name must be Harvestwood")

    explicit_rules: dict[str, dict] = {}
    for rule in contract.get("structure_rules", []):
        if not isinstance(rule, dict):
            errors.append("cozy_zones_0_3_6.json: structure rule must be an object")
            continue
        sid = rule.get("id")
        if not isinstance(sid, str) or not sid:
            errors.append("cozy_zones_0_3_6.json: structure rule missing id")
            continue
        if sid in explicit_rules:
            errors.append(f"Duplicate structure rule: {sid}")
        explicit_rules[sid] = rule
        if rule.get("minimum") not in TIER_INDEX:
            errors.append(f"Structure {sid}: invalid minimum tier {rule.get('minimum')!r}")
        if rule.get("minimum_influence") not in INFLUENCE:
            errors.append(f"Structure {sid}: invalid minimum influence {rule.get('minimum_influence')!r}")
        macros = rule.get("macro_regions")
        if not isinstance(macros, list) or any(m not in REGIONS - {"CORE"} for m in macros):
            errors.append(f"Structure {sid}: invalid macro_regions {macros!r}")

    prefix_rules: dict[str, dict] = {}
    for rule in contract.get("structure_prefix_rules", []):
        if not isinstance(rule, dict) or not isinstance(rule.get("prefix"), str):
            errors.append("cozy_zones_0_3_6.json: malformed structure prefix rule")
            continue
        prefix_rules[rule["prefix"]] = rule

    natural_rules: dict[str, dict] = {}
    for rule in contract.get("natural_entity_rules", []):
        if not isinstance(rule, dict) or not isinstance(rule.get("id"), str):
            errors.append("cozy_zones_0_3_6.json: malformed natural entity rule")
            continue
        eid = rule["id"]
        if eid in natural_rules:
            errors.append(f"Duplicate natural entity rule: {eid}")
        natural_rules[eid] = rule
        if rule.get("minimum") not in TIER_INDEX:
            errors.append(f"Entity {eid}: invalid minimum tier")
        if rule.get("minimum_influence") not in INFLUENCE:
            errors.append(f"Entity {eid}: invalid minimum influence")
        macros = rule.get("macro_regions")
        if not isinstance(macros, list) or any(m not in REGIONS - {"CORE"} for m in macros):
            errors.append(f"Entity {eid}: invalid macro_regions {macros!r}")
        if not isinstance(rule.get("daytime_candidate"), bool):
            errors.append(f"Entity {eid}: daytime_candidate must be boolean")
        if not isinstance(rule.get("enabled"), bool):
            errors.append(f"Entity {eid}: enabled must be boolean")

    seen_quests: set[str] = set()
    for binding in bindings.get("bindings", []):
        if not isinstance(binding, dict):
            errors.append("quest_structure_bindings.json: binding must be an object")
            continue

        qid = binding.get("quest_id")
        label = f"binding::{qid or '<missing>'}"
        if not isinstance(qid, str) or qid not in quests:
            errors.append(f"{label}: quest_id is missing from quest catalog")
            continue
        if qid in seen_quests:
            errors.append(f"{label}: duplicate binding")
        seen_quests.add(qid)

        qregion = binding.get("quest_region")
        qtier = binding.get("quest_tier")
        if qregion not in REGIONS:
            errors.append(f"{label}: invalid quest_region {qregion!r}")
        if qtier not in TIER_INDEX:
            errors.append(f"{label}: invalid quest_tier {qtier!r}")
            continue

        status = str(binding.get("binding_status", ""))
        targets = binding.get("targets")
        if not isinstance(targets, list):
            errors.append(f"{label}: targets must be a list")
            continue

        if status == "EXPLICIT_ENTITY_RULE":
            if targets:
                warnings.append(f"{label}: entity-ecology binding unexpectedly also has structure targets")
            continue

        if not targets:
            errors.append(f"{label}: structure binding has no targets")
            continue

        for target in targets:
            if not isinstance(target, str) or not target:
                errors.append(f"{label}: invalid target {target!r}")
                continue

            if target.endswith("*"):
                prefix = target[:-1]
                rule = prefix_rules.get(prefix)
                if rule is None:
                    errors.append(f"{label}: no checked-in prefix rule for {target}")
                    continue
            else:
                rule = explicit_rules.get(target)
                if rule is None:
                    errors.append(f"{label}: target {target} is not an explicit/prefix CozyCrazyZones structure rule")
                    continue

            minimum = rule.get("minimum")
            if minimum in TIER_INDEX and TIER_INDEX[minimum] > TIER_INDEX[qtier]:
                errors.append(
                    f"{label}: quest tier {qtier} is earlier than structure minimum {minimum} for {target}"
                )

            macros = set(rule.get("macro_regions") or [])
            if qregion in {"NORTH", "EAST", "SOUTH", "WEST"} and macros and qregion not in macros:
                errors.append(
                    f"{label}: target {target} is restricted to {sorted(macros)}, not quest region {qregion}"
                )

            if status.startswith("EXPLICIT") and qregion in {"NORTH", "EAST", "SOUTH", "WEST"} and not macros:
                if status not in {"EXPLICIT_MINIMUM_RULE", "EXPLICIT_ENTITY_RULE"}:
                    warnings.append(
                        f"{label}: status {status} claims explicit regional binding, but {target} has no macro restriction"
                    )

    blocked = bindings.get("blocked_or_pending", [])
    for item in blocked:
        if not isinstance(item, dict):
            errors.append("blocked_or_pending entry must be an object")
            continue
        qid = item.get("quest_id")
        if qid not in quests:
            errors.append(f"blocked_or_pending references unknown quest {qid!r}")
        if not isinstance(item.get("reason"), str) or not item.get("reason", "").strip():
            errors.append(f"blocked_or_pending {qid!r} is missing a reason")

    critical = {
        "mowziesmobs:frostmaw_spawn": ("WILDLANDS", {"NORTH"}),
        "mowziesmobs:umvuthana_grove": ("WILDLANDS", {"SOUTH"}),
        "cataclysm:cursed_pyramid": ("DREAD_REACHES", {"SOUTH"}),
        "betterjungletemples:jungle_temple": ("FRONTIER", {"EAST"}),
    }
    for sid, (tier, macros) in critical.items():
        rule = explicit_rules.get(sid)
        if rule is None:
            errors.append(f"Critical zoning anchor disappeared: {sid}")
            continue
        if rule.get("minimum") != tier or set(rule.get("macro_regions") or []) != macros:
            errors.append(f"Critical zoning anchor changed unexpectedly: {sid}")

    # Regional field-job mob claims are now machine-checkable against the 0.3.6
    # natural-entity contract.
    field_claims = {
        "alexsmobs:snow_leopard": ("FRONTIER", {"NORTH"}),
        "mowziesmobs:foliaath": ("FRONTIER", {"EAST"}),
        "skarrier_mobs:carniflore": ("WILDLANDS", {"EAST"}),
        "skarrier_mobs:slither_matriarch": ("WILDLANDS", {"EAST"}),
        "alexsmobs:rattlesnake": ("FRONTIER", {"SOUTH"}),
        "skarrier_mobs:quake": ("FRONTIER", {"SOUTH"}),
        "born_in_chaos_v1:spirit_guide": ("FRONTIER", {"SOUTH"}),
        "alexsmobs:grizzly_bear": ("FRONTIER", {"NORTH", "WEST"}),
        "born_in_chaos_v1:supreme_bonescaller": ("DREAD_REACHES", set()),
    }
    for eid, (tier, macros) in field_claims.items():
        rule = natural_rules.get(eid)
        if rule is None:
            errors.append(f"Regional field-job ecology anchor disappeared: {eid}")
            continue
        if rule.get("minimum") != tier or set(rule.get("macro_regions") or []) != macros:
            errors.append(f"Regional field-job ecology anchor changed unexpectedly: {eid}")

    suppression = contract.get("suppression", {})
    if "cataclysm" not in suppression.get("structure_namespaces", []):
        errors.append("Expected Cataclysm structure namespace suppression is missing")
    if "cataclysm:cursed_pyramid" not in suppression.get("structure_exceptions", []):
        errors.append("Expected Cursed Pyramid exception is missing")

    return finish()


def finish() -> int:
    for warning in warnings:
        print(f"WARNING: {warning}")
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} error(s), {len(warnings)} warning(s)")
        return 1
    print(f"OK: CozyCrazyZones 0.3.6 world-binding validation passed ({len(warnings)} warning(s))")
    return 0


if __name__ == "__main__":
    sys.exit(main())
