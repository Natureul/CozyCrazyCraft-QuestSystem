#!/usr/bin/env python3
"""Validate the data-only import of Quest & Reward Master Bible v0.2.

This intentionally validates semantic/design data, not runtime registry existence. Exact item,
quality and enchant IDs are audited separately before those rewards are hardcoded into game state.
"""

from __future__ import annotations

import json
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

POLICY = ROOT / "data" / "ecology" / "alexsmobs_policy_v0_2.json"
HOOK_FILES = {
    "NORTH": ROOT / "data" / "ecology" / "alexsmobs_hooks_north_v0_2.json",
    "EAST": ROOT / "data" / "ecology" / "alexsmobs_hooks_east_v0_2.json",
    "SOUTH": ROOT / "data" / "ecology" / "alexsmobs_hooks_south_v0_2.json",
    "WEST": ROOT / "data" / "ecology" / "alexsmobs_hooks_west_v0_2.json",
}
SIGNATURES = ROOT / "data" / "rewards" / "unique_signature_rewards_v0_2.json"
NATIVE_REWARDS = ROOT / "data" / "rewards" / "alexsmobs_native_rewards_v0_2.json"

TIERS = {"HEARTHLANDS", "FRONTIER", "WILDLANDS", "DREAD_REACHES"}


def load(path: Path):
    with path.open("r", encoding="utf-8") as fh:
        return json.load(fh)


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    policy = load(POLICY)
    species = policy["species"]
    require(len(species) == 68, f"Expected 68 Alex's Mobs ecology species, found {len(species)}")
    registry_ids = [entry["registry_id"] for entry in species]
    require(len(set(registry_ids)) == 68, "Alex's Mobs ecology registry IDs must be unique")
    require(all(x.startswith("alexsmobs:") for x in registry_ids), "Every ecology registry ID must be alexsmobs:*")
    require(len(policy["role_vocabulary"]) == 7, "Expected seven v0.2 ecology-role families")
    require(len(policy["visibility_by_band"]) == 4, "Expected four ecology visibility bands")

    all_hooks = []
    for region, path in HOOK_FILES.items():
        data = load(path)
        hooks = data["quests"]
        require(data["macro_region"] == region, f"{path.name}: macro_region mismatch")
        require(data["count"] == 40, f"{region}: declared ecology hook count must be 40")
        require(len(hooks) == 40, f"{region}: expected 40 ecology hooks, found {len(hooks)}")
        tier_counts = Counter(q["radial_tier"] for q in hooks)
        require(set(tier_counts) == TIERS, f"{region}: unexpected tier set {set(tier_counts)}")
        require(all(tier_counts[tier] == 10 for tier in TIERS), f"{region}: expected exactly ten hooks per tier, found {tier_counts}")
        expected_prefix = f"AM-{region[0]}-"
        require(all(q["id"].startswith(expected_prefix) for q in hooks), f"{region}: hook ID prefix mismatch")
        require(all(q.get("title") and q.get("giver") and q.get("objective_type") for q in hooks), f"{region}: incomplete ecology hook metadata")
        all_hooks.extend(hooks)

    ids = [q["id"] for q in all_hooks]
    require(len(all_hooks) == 160, f"Expected 160 v0.2 ecology hooks, found {len(all_hooks)}")
    require(len(set(ids)) == 160, "Ecology hook IDs must be globally unique")

    signatures = load(SIGNATURES)
    named = signatures["named_signature_equipment"]
    special = signatures["special_village_rewards"]
    catalogs = signatures["regional_unique_reward_catalogs"]
    require(len(named) == 8, f"Expected 8 named signature rewards, found {len(named)}")
    require(len(special) == 8, f"Expected 8 special village rewards, found {len(special)}")
    require(set(catalogs) == {"NORTH", "EAST", "SOUTH", "WEST"}, "Unique reward catalogs must cover all four macro-regions")
    require(all(len(catalogs[region]) == 16 for region in catalogs), "Each regional unique-reward catalog must contain exactly 16 entries")
    require(len(signatures["regional_quality_vocabulary"]) == 4, "Expected four regional quality vocabularies")
    require(len(signatures["quality_policy"]) >= 6, "Expected the full v0.2 unique-reward quality policy")

    named_by_name = {entry["name"]: entry for entry in named}
    require(named_by_name["THE RED RETURN"]["region"] == "SOUTH", "THE RED RETURN must remain a Sunscar signature")
    require("Boomerang" in named_by_name["THE RED RETURN"]["base_item"], "THE RED RETURN must remain the boomerang signature")
    require(named_by_name["GREENWAKE"]["region"] == "EAST", "GREENWAKE must remain a Greenveil signature")
    require("Boomerang" not in named_by_name["GREENWAKE"]["base_item"], "Greenveil must not dilute the Sunscar boomerang identity")
    require(named_by_name["CANOPY GLIDER"]["region"] == "EAST", "CANOPY GLIDER must remain Greenveil's iconic early utility signature")

    native = load(NATIVE_REWARDS)
    rewards = native["rewards"]
    require(native["count"] == 31, "Native Alex's Mobs reward bank must declare 31 entries")
    require(len(rewards) == 31, f"Expected 31 native Alex's Mobs reward integrations, found {len(rewards)}")
    reward_names = [r["reward"] for r in rewards]
    require(len(set(reward_names)) == 31, "Native Alex's Mobs reward names must be unique")
    require("Animal Dictionary" in reward_names, "Animal Dictionary integration is required")
    require("Froststalker Horn / Froststalker Helmet" in reward_names, "Froststalker reward chain is required")
    require("Vine Lasso" in reward_names, "Vine Lasso integration is required")
    require("Roadrunner Boots" in reward_names, "Roadrunner Boots integration is required")
    require("Echolocator" in reward_names, "Echolocator integration is required")

    print("Quest Bible v0.2 semantic import validation passed")
    print(f"  ecology species: {len(species)}")
    print(f"  ecology hooks: {len(all_hooks)} (40/region, 10/tier)")
    print(f"  named signatures: {len(named)}")
    print(f"  native Alex's Mobs rewards: {len(rewards)}")
    print(f"  special village rewards: {len(special)}")
    print("  regional unique rewards: 16 x 4")


if __name__ == "__main__":
    main()
