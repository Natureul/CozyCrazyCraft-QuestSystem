#!/usr/bin/env python3
"""Static validation for CozyCrazyCraft's Bountiful 6.0.4 config-pack content.

This deliberately validates only properties we can prove without launching Minecraft.
It is not a substitute for the in-game smoke test in docs/TEST_PLAN.md.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BOUNTIFUL = ROOT / "deployment" / "config" / "bountiful"
POOLS = BOUNTIFUL / "bounty_pools"
DECREES = BOUNTIFUL / "bounty_decrees"
CONFIG = BOUNTIFUL / "bountiful.json"

SUPPORTED_TYPES = {"item", "item_tag", "entity", "criteria", "command"}
GREEN_TYPES = {"item", "item_tag", "entity"}

# Exact 1.20.1 ResourceLoadStrategy treats exclusion strings as Regex after
# replacing literal '*' with a restricted [A-Za-z_/]+ group. These patterns
# intentionally avoid '*' and use regex '.+' so digits/hyphens are also caught.
CUSTOM_ONLY_EXCLUSIONS = {"bounty_pools/.+", "bounty_decrees/.+"}

errors: list[str] = []
warnings: list[str] = []


def load_json(path: Path):
    try:
        with path.open("r", encoding="utf-8") as f:
            return json.load(f)
    except Exception as exc:  # pragma: no cover - command-line validator
        errors.append(f"{path.relative_to(ROOT)}: invalid JSON: {exc}")
        return None


def validate_main_config() -> None:
    data = load_json(CONFIG)
    if not isinstance(data, dict):
        return

    exclusions = set(data.get("dataPackExclusions", []))
    missing = CUSTOM_ONLY_EXCLUSIONS - exclusions
    if missing:
        errors.append(
            "bountiful.json does not fully enforce the hardened custom-only content rules; missing: "
            + ", ".join(sorted(missing))
        )

    # Catch accidental regression to the tempting but incomplete wildcard.
    weak = {"bounty_pools/*", "bounty_decrees/*"} & exclusions
    if weak:
        warnings.append(
            "Found Bountiful '*' exclusions. Exact 1.20.1 turns '*' into [A-Za-z_/]+, "
            "which may miss valid paths containing digits/hyphens; prefer the .+ rules."
        )

    if data.get("maxNumRewards") != 2:
        warnings.append(
            "maxNumRewards is not 2; reward-balance assumptions in the current design use at most two rewards."
        )


def validate_pools() -> set[str]:
    pool_ids: set[str] = set()

    for path in sorted(POOLS.glob("*.json")):
        pool_id = path.stem
        pool_ids.add(pool_id)
        data = load_json(path)
        if not isinstance(data, dict):
            continue

        content = data.get("content")
        if not isinstance(content, dict) or not content:
            errors.append(f"{path.relative_to(ROOT)}: pool must contain a non-empty 'content' object")
            continue

        for entry_id, entry in content.items():
            label = f"{path.relative_to(ROOT)}::{entry_id}"
            if not isinstance(entry, dict):
                errors.append(f"{label}: entry must be an object")
                continue

            entry_type = entry.get("type")
            if entry_type not in SUPPORTED_TYPES:
                errors.append(f"{label}: unsupported/unknown type {entry_type!r}")
            elif entry_type not in GREEN_TYPES:
                warnings.append(
                    f"{label}: uses YELLOW type {entry_type!r}; must have an explicit in-game test before shipping"
                )

            content_id = entry.get("content")
            if not isinstance(content_id, str) or not content_id:
                errors.append(f"{label}: missing non-empty 'content' registry/criterion/command string")

            amount = entry.get("amount")
            if not isinstance(amount, dict):
                errors.append(f"{label}: missing amount object")
            else:
                lo = amount.get("min")
                hi = amount.get("max")
                if not isinstance(lo, int) or not isinstance(hi, int):
                    errors.append(f"{label}: amount min/max must be integers")
                elif lo < 1 or hi < lo:
                    errors.append(f"{label}: invalid amount range {lo}..{hi}")

            worth = entry.get("unitWorth")
            if not isinstance(worth, (int, float)) or worth <= 0:
                errors.append(f"{label}: unitWorth must be a positive number")

            weight = entry.get("weightMult", 1.0)
            if not isinstance(weight, (int, float)) or weight <= 0:
                errors.append(f"{label}: weightMult must be positive")

    if not pool_ids:
        errors.append("No custom bounty pools found")
    return pool_ids


def validate_decrees(pool_ids: set[str]) -> None:
    decree_count = 0
    for path in sorted(DECREES.glob("*.json")):
        decree_count += 1
        data = load_json(path)
        if not isinstance(data, dict):
            continue

        for key in ("objectives", "rewards"):
            refs = data.get(key)
            if not isinstance(refs, list) or not refs:
                errors.append(f"{path.relative_to(ROOT)}: '{key}' must be a non-empty list")
                continue
            for ref in refs:
                if ref not in pool_ids:
                    errors.append(
                        f"{path.relative_to(ROOT)}: {key} references missing pool {ref!r}"
                    )

        if not isinstance(data.get("name"), str) or not data.get("name", "").strip():
            warnings.append(
                f"{path.relative_to(ROOT)}: give the decree a literal 'name' so 1.20.1 does not depend on a missing translation key"
            )

    if decree_count == 0:
        errors.append("No custom decrees found")


if __name__ == "__main__":
    validate_main_config()
    pools = validate_pools()
    validate_decrees(pools)

    for warning in warnings:
        print(f"WARNING: {warning}")

    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} error(s), {len(warnings)} warning(s)")
        sys.exit(1)

    print(f"OK: static Bountiful validation passed ({len(warnings)} warning(s))")
