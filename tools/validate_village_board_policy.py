#!/usr/bin/env python3
"""Validate the village-board runtime contract before it becomes Forge code."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
POLICY = ROOT / "data" / "village_board_policy.json"
ASSIGNMENT = ROOT / "data" / "board_decree_assignment.json"

errors: list[str] = []
warnings: list[str] = []


def load(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"{path.relative_to(ROOT)}: invalid JSON: {exc}")
        return None


def main() -> int:
    policy = load(POLICY)
    assignment = load(ASSIGNMENT)
    if not isinstance(policy, dict):
        return finish()

    if policy.get("schema_version") != 1:
        errors.append("village_board_policy.json: schema_version must be 1")

    stock = policy.get("stock_bountiful")
    if not isinstance(stock, dict):
        errors.append("village_board_policy.json: missing stock_bountiful object")
    else:
        freq = stock.get("current_board_gen_frequency")
        if not isinstance(freq, int) or freq < 0:
            errors.append("stock_bountiful.current_board_gen_frequency must be a non-negative integer")
        if stock.get("generation_semantics") != "weighted_jigsaw_house_pool_entry_not_guaranteed":
            errors.append("stock Bountiful generation must be recorded as non-guaranteed weighted jigsaw insertion")
        pools = stock.get("supported_vanilla_pools")
        expected = {"plains", "savanna", "snowy", "taiga", "desert"}
        if not isinstance(pools, list) or set(pools) != expected:
            errors.append("stock_bountiful.supported_vanilla_pools must match the five Bountiful 1.20.1 vanilla pools")
        if stock.get("existing_gazebo_satisfies_guarantee") is not True:
            errors.append("existing Bountiful gazebos must satisfy the one-board guarantee")
        if stock.get("disable_stock_generation_before_replacement_proven") is not False:
            errors.append("do not disable stock board generation before companion replacement is proven")

    petshop = policy.get("domestication_innovation_petshop")
    if not isinstance(petshop, dict):
        errors.append("village_board_policy.json: missing domestication_innovation_petshop object")
    else:
        if petshop.get("source_mod") != "domesticationinnovation":
            errors.append("petshop source_mod must be domesticationinnovation")
        if petshop.get("config_file") != "config/domestication-innovation.toml":
            errors.append("petshop config_file must identify Domestication Innovation's common config")
        if petshop.get("config_section") != "general" or petshop.get("config_key") != "petstore_village_weight":
            errors.append("petshop suppression must use general.petstore_village_weight")
        if petshop.get("desired_shipping_weight") != 0:
            errors.append("shipping petstore_village_weight must be zero once board replacement is proven")
        if petshop.get("avoid_brittle_one_to_one_jigsaw_template_swap") is not True:
            errors.append("petshop replacement should not depend on a brittle one-to-one jigsaw template swap")
        if petshop.get("keep_domestication_innovation_pet_mechanics") is not True:
            errors.append("removing the petshop must not disable Domestication Innovation's pet mechanics")
        if petshop.get("apply_disable_only_when_village_board_guarantee_is_proven") is not True:
            errors.append("petshop suppression must wait until the civic-board guarantee is proven")

    detection = policy.get("village_detection")
    if not isinstance(detection, dict):
        errors.append("village_board_policy.json: missing village_detection object")
    else:
        r = detection.get("existing_board_search_radius")
        if not isinstance(r, int) or not 24 <= r <= 128:
            errors.append("existing_board_search_radius must be a conservative 24..128 blocks")
        if detection.get("reject_lone_bell_without_village_evidence") is not True:
            errors.append("lone bells must not qualify as villages")

    repair = policy.get("repair")
    if not isinstance(repair, dict):
        errors.append("village_board_policy.json: missing repair object")
    else:
        if repair.get("board_block") != "bountiful:bountyboard":
            errors.append("repair.board_block must be bountiful:bountyboard")
        radius = repair.get("placement_search_radius")
        if not isinstance(radius, int) or not 2 <= radius <= 24:
            errors.append("repair.placement_search_radius must stay compact (2..24)")
        checks = repair.get("max_candidate_checks_per_attempt")
        if not isinstance(checks, int) or not 1 <= checks <= 256:
            errors.append("repair.max_candidate_checks_per_attempt must be 1..256")
        if repair.get("never_force_place_if_no_safe_site") is not True:
            errors.append("repair must be allowed to defer when no safe site exists")
        if repair.get("retroactive_for_existing_villages") is not True:
            warnings.append("Existing boardless villages would remain broken if retroactive repair is disabled")

    modification = policy.get("player_modification")
    if not isinstance(modification, dict) or modification.get("immediate_respawn_after_player_break") is not False:
        errors.append("player-broken boards must not immediately respawn")

    regional = policy.get("regional_initialization")
    if not isinstance(regional, dict):
        errors.append("village_board_policy.json: missing regional_initialization object")
    else:
        if "regionalCellAt" not in str(regional.get("geography_api")):
            errors.append("regional initialization must consume CozyZonesApi.regionalCellAt")
        assignment_path = regional.get("assignment_contract")
        if assignment_path != "data/board_decree_assignment.json":
            errors.append("regional initialization must point to data/board_decree_assignment.json")
        if regional.get("random_decree_selection_is_not_production_behavior") is not True:
            errors.append("random decree selection must be explicitly rejected as production behavior")
        fallback = regional.get("fallback_profile")
        if not isinstance(fallback, str) or not fallback:
            errors.append("regional_initialization.fallback_profile must be a non-empty decree/profile id")
        elif isinstance(assignment, dict):
            text = json.dumps(assignment)
            if fallback not in text:
                warnings.append(f"Fallback profile {fallback!r} is not mentioned in board_decree_assignment.json")

    perf = policy.get("performance")
    if not isinstance(perf, dict):
        errors.append("village_board_policy.json: missing performance object")
    else:
        if perf.get("global_world_scans") is not False:
            errors.append("village board runtime must not globally scan the world")
        if perf.get("per_tick_structure_queries") is not False:
            errors.append("village board runtime must not perform per-tick structure queries")
        if perf.get("cache_processed_settlements") is not True:
            errors.append("processed settlements must be cached")

    record = policy.get("saved_record")
    if not isinstance(record, dict):
        errors.append("village_board_policy.json: missing saved_record object")
    else:
        required_statuses = {"FOUND_EXISTING", "PLACED_REPAIR", "DEFERRED_NO_SAFE_SITE", "PLAYER_MODIFIED"}
        statuses = record.get("statuses")
        if not isinstance(statuses, list) or set(statuses) != required_statuses:
            errors.append("saved_record.statuses must define all four lifecycle states")

    return finish()


def finish() -> int:
    for warning in warnings:
        print(f"WARNING: {warning}")
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} error(s), {len(warnings)} warning(s)")
        return 1
    print(f"OK: village-board policy validation passed ({len(warnings)} warning(s))")
    return 0


if __name__ == "__main__":
    sys.exit(main())
