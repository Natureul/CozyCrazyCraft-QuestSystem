#!/usr/bin/env python3
"""Static validation for the neutral CozyCrazyCraft quest catalog.

This validator deliberately checks design invariants only.  It does not claim that
world hooks, map generation, custom reward construction, or exact mod interactions
work in Minecraft.  Those remain gated by their explicit status fields and the
in-game test plan.
"""

from __future__ import annotations

import json
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CATALOG = ROOT / "data" / "quest_catalog"

EXPECTED = {
    "core.json": ("CORE", "Shared Core"),
    "north.json": ("NORTH", "Frostmarch"),
    "east.json": ("EAST", "Greenveil"),
    "south.json": ("SOUTH", "Sunscar"),
    "west.json": ("WEST", "Harvestlands"),
}

TIERS = {"HEARTHLANDS", "FRONTIER", "WILDLANDS", "DREAD_REACHES"}
SAFE_PRIMITIVES = {"item", "item_tag", "entity", "proof_item"}

REGIONAL_BOSS_TIERS = {
    "mowziesmobs:frostmaw": ("NORTH", "WILDLANDS"),
    "mowziesmobs:umvuthi": ("SOUTH", "WILDLANDS"),
    "born_in_chaos_v1:sir_pumpkinhead": ("WEST", "WILDLANDS"),
    "aquamirae:captain_cornelia": ("NORTH", "DREAD_REACHES"),
    "cataclysm:ancient_remnant": ("SOUTH", "DREAD_REACHES"),
    "born_in_chaos_v1:lord_pumpkinhead": ("WEST", "DREAD_REACHES"),
    "cozycrazyquests:jungle_abomination_placeholder": ("EAST", "DREAD_REACHES"),
}

errors: list[str] = []
warnings: list[str] = []
seen_ids: dict[str, str] = {}


def load(path: Path):
    try:
        with path.open("r", encoding="utf-8") as f:
            return json.load(f)
    except Exception as exc:
        errors.append(f"{path.relative_to(ROOT)}: invalid JSON: {exc}")
        return None


def require_text(value, label: str) -> None:
    if not isinstance(value, str) or not value.strip():
        errors.append(f"{label}: expected non-empty text")


def validate_amount(amount, label: str) -> None:
    if not isinstance(amount, dict):
        errors.append(f"{label}: amount must be an object")
        return
    lo, hi = amount.get("min"), amount.get("max")
    if not isinstance(lo, int) or not isinstance(hi, int):
        errors.append(f"{label}: amount min/max must be integers")
    elif lo < 1 or hi < lo:
        errors.append(f"{label}: invalid amount range {lo}..{hi}")


def validate_reward_plan(rewards, label: str) -> None:
    if not isinstance(rewards, list) or not rewards:
        errors.append(f"{label}: reward_plan must contain at least one reward")
        return
    if len(rewards) > 2:
        errors.append(
            f"{label}: reward_plan has {len(rewards)} entries; current Bountiful design is capped at two rewards"
        )
    for index, reward in enumerate(rewards):
        rlabel = f"{label}.reward_plan[{index}]"
        if not isinstance(reward, dict):
            errors.append(f"{rlabel}: reward must be an object")
            continue
        require_text(reward.get("kind"), f"{rlabel}.kind")
        require_text(reward.get("key"), f"{rlabel}.key")
        require_text(reward.get("status"), f"{rlabel}.status")
        require_text(reward.get("role"), f"{rlabel}.role")


def validate_quest(quest, path: Path, region: str) -> tuple[str | None, str | None]:
    label = f"{path.relative_to(ROOT)}"
    if not isinstance(quest, dict):
        errors.append(f"{label}: quest entry must be an object")
        return None, None

    qid = quest.get("id")
    require_text(qid, f"{label}.id")
    if isinstance(qid, str) and qid:
        if qid in seen_ids:
            errors.append(f"{label}: duplicate quest id {qid!r}; first seen in {seen_ids[qid]}")
        else:
            seen_ids[qid] = str(path.relative_to(ROOT))
        label = f"{path.relative_to(ROOT)}::{qid}"

    tier = quest.get("tier")
    if tier not in TIERS:
        errors.append(f"{label}: invalid tier {tier!r}")

    for key in ("notice_class", "delivery", "title", "issuer", "body", "implementation_status"):
        require_text(quest.get(key), f"{label}.{key}")

    objective = quest.get("objective")
    target = None
    if not isinstance(objective, dict):
        errors.append(f"{label}: objective must be an object")
    else:
        primitive = objective.get("primitive")
        if primitive not in SAFE_PRIMITIVES:
            errors.append(
                f"{label}: objective primitive {primitive!r} is not allowed; catalog must stay on {sorted(SAFE_PRIMITIVES)}"
            )
        target = objective.get("target")
        require_text(target, f"{label}.objective.target")
        require_text(objective.get("registry_status"), f"{label}.objective.registry_status")
        validate_amount(objective.get("amount"), f"{label}.objective")

        world_hook = objective.get("requires_world_hook")
        if not isinstance(world_hook, bool):
            errors.append(f"{label}.objective.requires_world_hook must be boolean")

        if primitive == "proof_item":
            if world_hook is not True:
                errors.append(f"{label}: proof_item objective must explicitly require a world hook")
            if isinstance(target, str) and not target.startswith("cozycrazyquests:"):
                warnings.append(
                    f"{label}: proof_item target {target!r} is outside cozycrazyquests namespace; verify that it is truly a unique proof item"
                )
        elif world_hook is True:
            warnings.append(
                f"{label}: non-proof primitive is marked requires_world_hook=true; confirm that extra hook is actually necessary"
            )

        if quest.get("delivery") == "repeatable_pool" and primitive == "proof_item":
            errors.append(f"{label}: repeatable_pool cannot use a structure proof item without authored targeting")

        registry_status = objective.get("registry_status", "")
        implementation_status = quest.get("implementation_status", "")
        if isinstance(registry_status, str) and registry_status.startswith("DEFERRED_"):
            if implementation_status != "DEFERRED_EXTERNAL":
                errors.append(
                    f"{label}: deferred external registry target must also use implementation_status=DEFERRED_EXTERNAL"
                )

    validate_reward_plan(quest.get("reward_plan"), label)

    notes = quest.get("notes")
    if notes is not None and not isinstance(notes, list):
        errors.append(f"{label}.notes must be a list when present")

    if isinstance(target, str) and target in REGIONAL_BOSS_TIERS:
        expected_region, expected_tier = REGIONAL_BOSS_TIERS[target]
        if (region, tier) != (expected_region, expected_tier):
            errors.append(
                f"{label}: boss target {target} belongs to {expected_region}/{expected_tier}, not {region}/{tier}"
            )

    return qid if isinstance(qid, str) else None, tier if isinstance(tier, str) else None


def main() -> int:
    total = 0
    regional_counts: dict[str, Counter] = {}

    for filename, (expected_region, expected_display) in EXPECTED.items():
        path = CATALOG / filename
        if not path.exists():
            errors.append(f"Missing catalog file: {path.relative_to(ROOT)}")
            continue
        data = load(path)
        if not isinstance(data, dict):
            continue

        if data.get("schema_version") != 1:
            errors.append(f"{path.relative_to(ROOT)}: schema_version must be 1")
        if data.get("region") != expected_region:
            errors.append(f"{path.relative_to(ROOT)}: region must be {expected_region}")
        if data.get("display_region") != expected_display:
            errors.append(f"{path.relative_to(ROOT)}: display_region must be {expected_display!r}")

        quests = data.get("quests")
        if not isinstance(quests, list) or not quests:
            errors.append(f"{path.relative_to(ROOT)}: quests must be a non-empty list")
            continue

        counts = Counter()
        for quest in quests:
            _, tier = validate_quest(quest, path, expected_region)
            if tier in TIERS:
                counts[tier] += 1
            total += 1
        regional_counts[expected_region] = counts

    # Current authored design intentionally has 12 generic Hearthlands concepts
    # and six concepts per cardinal region per radial tier.
    core = regional_counts.get("CORE", Counter())
    if core != Counter({"HEARTHLANDS": 12}):
        errors.append(f"CORE catalog should contain exactly 12 Hearthlands entries; found {dict(core)}")

    for region in ("NORTH", "EAST", "SOUTH", "WEST"):
        counts = regional_counts.get(region, Counter())
        expected = Counter({tier: 6 for tier in TIERS})
        if counts != expected:
            errors.append(f"{region} catalog should contain exactly 6 quests per tier; found {dict(counts)}")

    if total != 108:
        errors.append(f"Expected 108 quest concepts in current catalog; found {total}")

    for warning in warnings:
        print(f"WARNING: {warning}")
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} error(s), {len(warnings)} warning(s)")
        return 1

    print(f"OK: neutral quest catalog validation passed ({total} quests, {len(warnings)} warning(s))")
    return 0


if __name__ == "__main__":
    sys.exit(main())
