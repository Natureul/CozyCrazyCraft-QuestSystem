#!/usr/bin/env python3
"""Validate position->decree assignment data for future Bountiful board runtime hook."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
POLICY = ROOT / "data" / "board_decree_assignment.json"
DECREES = ROOT / "deployment" / "config" / "bountiful" / "bounty_decrees"

TIERS = {"HEARTHLANDS", "FRONTIER", "WILDLANDS", "DREAD_REACHES"}
BANDS = {"SHARED_CORE", "CARDINAL_TRANSITION", "ESTABLISHED"}
MACROS = {"ANY", "NORTH", "EAST", "SOUTH", "WEST"}
STATUSES = {"LIVE_DATA", "PLANNED_DATA"}

errors: list[str] = []
warnings: list[str] = []


def main() -> int:
    try:
        data = json.loads(POLICY.read_text(encoding="utf-8"))
    except Exception as exc:
        print(f"ERROR: {POLICY.relative_to(ROOT)}: invalid JSON: {exc}")
        return 1

    if data.get("schema_version") != 1:
        errors.append("board_decree_assignment.json: schema_version must be 1")
    if data.get("geography_contract") != "CozyCrazyZones-0.3.6":
        errors.append("board_decree_assignment.json must target CozyCrazyZones-0.3.6")

    assignments = data.get("assignments")
    if not isinstance(assignments, list) or not assignments:
        errors.append("board_decree_assignment.json: assignments must be a non-empty list")
        return finish()

    seen: set[tuple[str, str, str]] = set()
    live = 0
    planned = 0
    for index, row in enumerate(assignments):
        label = f"assignment[{index}]"
        if not isinstance(row, dict):
            errors.append(f"{label}: must be an object")
            continue
        band = row.get("influence_band")
        tier = row.get("tier")
        macro = row.get("macro_region")
        decree = row.get("decree")
        status = row.get("status")

        if band not in BANDS:
            errors.append(f"{label}: invalid influence_band {band!r}")
        if tier not in TIERS:
            errors.append(f"{label}: invalid tier {tier!r}")
        if macro not in MACROS:
            errors.append(f"{label}: invalid macro_region {macro!r}")
        if status not in STATUSES:
            errors.append(f"{label}: invalid status {status!r}")
        if not isinstance(decree, str) or not decree:
            errors.append(f"{label}: decree must be non-empty")
            continue

        key = (str(band), str(tier), str(macro))
        if key in seen:
            errors.append(f"{label}: duplicate board cell {key}")
        seen.add(key)

        exists = (DECREES / f"{decree}.json").is_file()
        if status == "LIVE_DATA":
            live += 1
            if not exists:
                errors.append(f"{label}: LIVE_DATA decree file is missing: {decree}.json")
        elif status == "PLANNED_DATA":
            planned += 1
            if exists:
                warnings.append(f"{label}: planned decree now exists and can likely be promoted to LIVE_DATA: {decree}")

    core = ("SHARED_CORE", "HEARTHLANDS", "ANY")
    if core not in seen:
        errors.append("Missing Shared Core Hearthlands fallback assignment")

    for band in ("CARDINAL_TRANSITION", "ESTABLISHED"):
        for macro in ("NORTH", "EAST", "SOUTH", "WEST"):
            if (band, "HEARTHLANDS", macro) not in seen:
                errors.append(f"Missing {band} Hearthlands assignment for {macro}")

    runtime = data.get("runtime_rules")
    if not isinstance(runtime, dict):
        errors.append("board_decree_assignment.json: runtime_rules must be an object")
    else:
        for key in ("only_stamp_pristine_boards", "never_reclassify_populated_board_automatically", "planned_data_must_not_be_selected_until_decree_exists"):
            if runtime.get(key) is not True:
                errors.append(f"runtime_rules.{key} must remain true")

    if live < 9:
        warnings.append(f"Only {live} live assignment rows; expected at least Shared Core + 8 H1 region/band rows")
    if planned:
        print(f"INFO: {planned} board cells are intentionally waiting on tier-specific live decree data")

    return finish()


def finish() -> int:
    for warning in warnings:
        print(f"WARNING: {warning}")
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} error(s), {len(warnings)} warning(s)")
        return 1
    print(f"OK: board decree assignment validation passed ({len(warnings)} warning(s))")
    return 0


if __name__ == "__main__":
    sys.exit(main())
