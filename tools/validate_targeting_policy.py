#!/usr/bin/env python3
"""Validate quest/cartographer target-selection distance policy.

This checks design invariants only. It does not locate structures or launch Minecraft.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
POLICY = ROOT / "data" / "targeting_policy.json"
BOARD_POLICY = ROOT / "data" / "board_policy.json"

TIERS = ["HEARTHLANDS", "FRONTIER", "WILDLANDS", "DREAD_REACHES"]
SCOPES = {"LOCAL_SITE", "REGIONAL_EXPEDITION", "OUTWARD_LEAD", "LEGENDARY_DESTINATION"}

errors: list[str] = []
warnings: list[str] = []


def load(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"{path.relative_to(ROOT)}: invalid JSON: {exc}")
        return None


def validate_envelope(label: str, envelope: dict) -> None:
    keys = ("min", "ideal", "soft_max", "hard_max")
    values = []
    for key in keys:
        value = envelope.get(key)
        if not isinstance(value, int) or value < 0:
            errors.append(f"{label}.{key}: must be a non-negative integer")
            return
        values.append(value)
    if values != sorted(values) or len(set(values)) != len(values):
        errors.append(f"{label}: expected min < ideal < soft_max < hard_max, found {values}")


def main() -> int:
    data = load(POLICY)
    board = load(BOARD_POLICY)
    if not isinstance(data, dict) or not isinstance(board, dict):
        return finish()

    if data.get("schema_version") != 1:
        errors.append("targeting_policy.json: schema_version must be 1")
    if data.get("geography_contract") != "CozyCrazyZones-0.3.6":
        errors.append("targeting_policy.json must identify CozyCrazyZones-0.3.6")
    if "0.3.6" not in str(board.get("source_of_truth", "")):
        errors.append("board_policy.json must use the same 0.3.6 geography contract")
    if board.get("targeting_policy") != "data/targeting_policy.json":
        errors.append("board_policy.json must point at data/targeting_policy.json")

    hard = data.get("hard_rules")
    if not isinstance(hard, dict):
        errors.append("targeting_policy.json: hard_rules must be an object")
    else:
        required_true = {
            "same_dimension",
            "require_cozy_zones_structure_allowed",
            "respect_target_radial_tier",
            "respect_target_macro_region_when_regional",
            "no_candidate_means_no_posting",
            "do_not_use_wrong_region_as_fallback",
        }
        for key in sorted(required_true):
            if hard.get(key) is not True:
                errors.append(f"targeting_policy.json: hard_rules.{key} must remain true")
        jump = hard.get("outward_leads_max_tier_jump")
        if jump != 1:
            errors.append("targeting_policy.json: outward leads must be limited to a one-tier jump")
        gain = hard.get("outward_leads_min_radial_gain_blocks")
        if not isinstance(gain, int) or gain < 1:
            errors.append("targeting_policy.json: outward radial gain must be a positive integer")

    cache = data.get("candidate_search")
    if not isinstance(cache, dict):
        errors.append("targeting_policy.json: candidate_search must be an object")
    else:
        max_candidates = cache.get("max_candidate_instances")
        if not isinstance(max_candidates, int) or not 1 <= max_candidates <= 32:
            errors.append("candidate_search.max_candidate_instances must be between 1 and 32")
        positive = cache.get("positive_cache_ticks")
        negative = cache.get("negative_cache_ticks")
        if not isinstance(positive, int) or positive < 1200:
            errors.append("candidate_search.positive_cache_ticks is implausibly low")
        if not isinstance(negative, int) or negative < 1200:
            errors.append("candidate_search.negative_cache_ticks is implausibly low")
        if isinstance(positive, int) and isinstance(negative, int) and negative > positive:
            warnings.append("Negative target cache is longer than positive cache; review world-change responsiveness")

    scopes = data.get("scopes")
    if not isinstance(scopes, dict):
        errors.append("targeting_policy.json: scopes must be an object")
    else:
        unknown = set(scopes) - SCOPES
        missing = SCOPES - set(scopes)
        if unknown:
            errors.append("targeting_policy.json: unknown scopes: " + ", ".join(sorted(unknown)))
        if missing:
            errors.append("targeting_policy.json: missing scopes: " + ", ".join(sorted(missing)))

        for scope, spec in scopes.items():
            if not isinstance(spec, dict):
                errors.append(f"scope {scope}: must be an object")
                continue
            tier_envelopes = [(tier, spec.get(tier)) for tier in TIERS if tier in spec]
            for tier, envelope in tier_envelopes:
                if not isinstance(envelope, dict):
                    errors.append(f"scope {scope}/{tier}: envelope must be an object")
                else:
                    validate_envelope(f"scope {scope}/{tier}", envelope)

        # Intended tier coverage is part of the semantics.
        for tier in TIERS:
            if tier not in scopes.get("LOCAL_SITE", {}):
                errors.append(f"LOCAL_SITE missing {tier}")
            if tier not in scopes.get("REGIONAL_EXPEDITION", {}):
                errors.append(f"REGIONAL_EXPEDITION missing {tier}")
        for tier in TIERS[:-1]:
            if tier not in scopes.get("OUTWARD_LEAD", {}):
                errors.append(f"OUTWARD_LEAD missing {tier}")
        if "DREAD_REACHES" not in scopes.get("LEGENDARY_DESTINATION", {}):
            errors.append("LEGENDARY_DESTINATION must define DREAD_REACHES")

        # As the player progresses outward, ordinary same-purpose journeys may widen,
        # but should not unexpectedly get shorter.
        for scope in ("LOCAL_SITE", "REGIONAL_EXPEDITION"):
            ideals = [scopes[scope][tier]["ideal"] for tier in TIERS if isinstance(scopes[scope].get(tier), dict)]
            if ideals != sorted(ideals):
                errors.append(f"{scope}: ideal distance should be non-decreasing by tier")

    order = data.get("selection_order")
    if not isinstance(order, list) or len(order) < 6 or not all(isinstance(x, str) and x.strip() for x in order):
        errors.append("targeting_policy.json: selection_order must be a useful ordered list")

    proto = data.get("prototype_limits")
    if not isinstance(proto, dict):
        errors.append("targeting_policy.json: prototype_limits must be an object")
    else:
        cmd_chunks = proto.get("supplementaries_command_map_search_radius_chunks")
        cmd_blocks = proto.get("supplementaries_command_map_nominal_radius_blocks")
        trade_chunks = proto.get("supplementaries_trade_default_search_radius_chunks")
        trade_blocks = proto.get("supplementaries_trade_default_nominal_radius_blocks")
        if isinstance(cmd_chunks, int) and isinstance(cmd_blocks, int) and cmd_chunks * 16 != cmd_blocks:
            errors.append("Supplementaries command nominal block radius must equal chunk radius * 16")
        if isinstance(trade_chunks, int) and isinstance(trade_blocks, int) and trade_chunks * 16 != trade_blocks:
            errors.append("Supplementaries trade nominal block radius must equal chunk radius * 16")

    return finish()


def finish() -> int:
    for warning in warnings:
        print(f"WARNING: {warning}")
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} error(s), {len(warnings)} warning(s)")
        return 1
    print(f"OK: target-distance policy validation passed ({len(warnings)} warning(s))")
    return 0


if __name__ == "__main__":
    sys.exit(main())
