#!/usr/bin/env python3
"""Validate the neutral board-population policy consumed by future CozyCrazyZones integration."""

from __future__ import annotations

import json
import math
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
POLICY = ROOT / "data" / "board_policy.json"

TIERS = {"HEARTHLANDS", "FRONTIER", "WILDLANDS", "DREAD_REACHES"}
BANDS = {"SHARED_CORE", "CARDINAL_TRANSITION", "ESTABLISHED"}
CATALOG_SELECTORS = {"CORE", "CURRENT_REGION"}

errors: list[str] = []
warnings: list[str] = []


def main() -> int:
    try:
        with POLICY.open("r", encoding="utf-8") as fh:
            data = json.load(fh)
    except Exception as exc:
        print(f"ERROR: {POLICY.relative_to(ROOT)}: invalid JSON: {exc}")
        return 1

    if data.get("schema_version") != 1:
        errors.append("board_policy.json: schema_version must be 1")

    source = data.get("source_of_truth")
    if not isinstance(source, str) or "CozyCrazyZones" not in source:
        errors.append("board_policy.json: source_of_truth must explicitly identify CozyCrazyZones")

    cells = data.get("cells")
    if not isinstance(cells, dict):
        errors.append("board_policy.json: cells must be an object")
        return finish()

    unknown_bands = sorted(set(cells) - BANDS)
    if unknown_bands:
        errors.append("board_policy.json: unknown influence bands: " + ", ".join(unknown_bands))

    for band, tier_map in cells.items():
        if not isinstance(tier_map, dict) or not tier_map:
            errors.append(f"board_policy.json::{band}: must contain at least one tier rule")
            continue
        for tier, rule in tier_map.items():
            label = f"board_policy.json::{band}/{tier}"
            if tier not in TIERS:
                errors.append(f"{label}: unknown tier")
                continue
            if not isinstance(rule, dict):
                errors.append(f"{label}: rule must be an object")
                continue
            sources = rule.get("quest_sources")
            if not isinstance(sources, list) or not sources:
                errors.append(f"{label}: quest_sources must be a non-empty list")
                continue

            weight_total = 0.0
            for index, entry in enumerate(sources):
                elabel = f"{label}.quest_sources[{index}]"
                if not isinstance(entry, dict):
                    errors.append(f"{elabel}: entry must be an object")
                    continue

                catalog = entry.get("catalog")
                if catalog not in CATALOG_SELECTORS:
                    errors.append(f"{elabel}: invalid catalog selector {catalog!r}")

                source_tier = entry.get("tier")
                if source_tier not in TIERS:
                    errors.append(f"{elabel}: invalid source tier {source_tier!r}")

                weight = entry.get("weight")
                if not isinstance(weight, (int, float)) or weight <= 0:
                    errors.append(f"{elabel}: weight must be a positive number")
                else:
                    weight_total += float(weight)

                if catalog == "CORE" and source_tier != "HEARTHLANDS":
                    errors.append(f"{elabel}: Shared Core catalog currently only contains Hearthlands quests")

            if not math.isclose(weight_total, 1.0, abs_tol=1e-9):
                errors.append(f"{label}: quest-source weights must sum to 1.0, found {weight_total:.6f}")

    if "SHARED_CORE" not in cells or "HEARTHLANDS" not in cells.get("SHARED_CORE", {}):
        errors.append("board_policy.json: Shared Core must define a Hearthlands rule")
    if "CARDINAL_TRANSITION" not in cells or "HEARTHLANDS" not in cells.get("CARDINAL_TRANSITION", {}):
        errors.append("board_policy.json: Cardinal Transition must define a Hearthlands rule")
    established = cells.get("ESTABLISHED", {})
    for tier in TIERS:
        if tier not in established:
            errors.append(f"board_policy.json: Established band is missing {tier}")

    population = data.get("board_population")
    if not isinstance(population, dict):
        errors.append("board_policy.json: board_population must be an object")
    else:
        visible = population.get("target_visible_postings")
        if not isinstance(visible, dict):
            errors.append("board_policy.json: target_visible_postings must be an object")
        else:
            lo, hi = visible.get("min"), visible.get("max")
            if not isinstance(lo, int) or not isinstance(hi, int) or lo < 1 or hi < lo:
                errors.append("board_policy.json: invalid target_visible_postings min/max")
            elif hi > 10:
                warnings.append("Board target exceeds 10 visible postings; review UI clutter")

    return finish()


def finish() -> int:
    for warning in warnings:
        print(f"WARNING: {warning}")
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} error(s), {len(warnings)} warning(s)")
        return 1
    print(f"OK: board-policy validation passed ({len(warnings)} warning(s))")
    return 0


if __name__ == "__main__":
    sys.exit(main())
