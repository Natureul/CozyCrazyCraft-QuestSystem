#!/usr/bin/env python3
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
POLICY = ROOT / "data" / "progression" / "social_progression_policy_v0_1.json"


def fail(message: str) -> None:
    raise SystemExit(f"Social progression validation failed: {message}")


def main() -> None:
    data = json.loads(POLICY.read_text(encoding="utf-8"))

    principles = data["principles"]
    if principles["exact_npc_uuid_may_be_critical"]:
        fail("critical progression may not depend on one entity UUID")
    if principles["raw_quest_count_is_progression"]:
        fail("raw quest count may not become the chapter progression metric")
    if principles["minimum_critical_router_surfaces"] < 3:
        fail("every critical chapter state needs at least three router surfaces")
    if principles["atlas_role"] != "persistent_geography_not_npc_quest_log":
        fail("Atlas must remain geography, not an NPC quest tracker")

    trust = data["trust"]["ordered"]
    expected_trust = ["STRANGER", "RECOGNIZED", "RELIABLE", "TRUSTED", "PROVEN"]
    if trust != expected_trust:
        fail(f"trust order changed: {trust}")

    knowledge = data["knowledge"]
    if knowledge["ordered"] != ["UNKNOWN", "RUMOR", "LEAD", "KNOWN", "CONFIRMED"]:
        fail("knowledge state machine changed")
    if knowledge["rumor_creates_precise_pin"]:
        fail("RUMOR may not silently create a precise Atlas pin")

    zone1 = data["zone1"]
    gate = zone1["capstone_gate"]
    if set(gate["all_of"]) != {"COMMUNITY", "EXPLORATION"}:
        fail("Zone 1 capstone must require community + exploration breadth")
    if not {"PROFESSION", "DANGER"}.issubset(set(gate["one_of"])):
        fail("Zone 1 capstone needs a practical/danger third branch option")
    if gate["raw_repeatable_count_can_substitute"]:
        fail("repeatable grind may not substitute for semantic chapter breadth")
    if len(zone1["branches"]) < 3:
        fail("Zone 1 lattice needs at least three distinct branches")

    weights = data["quest_pool"]["starting_weights_percent"]
    if sum(weights.values()) != 100:
        fail(f"quest-pool starting weights total {sum(weights.values())}, expected 100")
    guards = data["quest_pool"]["guardrails"]
    if guards["max_same_core_verb_visible"] > 2:
        fail("visible pool may not regress to repetitive core verbs")
    if guards["frontier_plus_named_destination_fraction_min"] < 0.5:
        fail("Frontier+ destination specificity fell below Bible minimum")
    if guards["bosses_in_random_rotation"]:
        fail("boss commissions may not enter random rotation")

    professions = data["profession_knowledge"]
    if professions["cartographer"].get("universal_quest_funnel", True):
        fail("Cartographer may not become the universal quest funnel")
    if professions["nitwit"].get("optimal_farm", True):
        fail("Nitwit may not become the optimal farming profession")

    arcs = data["regional_arcs"]
    if arcs["EAST"]["wildlands_anchor"] is not None:
        fail("East must not gain an invented mandatory Wildlands boss")
    if "boomerang" not in arcs["SOUTH"]["signature_tags"]:
        fail("Sunscar must retain boomerang identity")
    if "canopy_glider" not in arcs["EAST"]["signature_tags"]:
        fail("Greenveil must retain Canopy Glider identity")

    flags = data["content_flags"]
    if flags["JUNGLE_ABOMINATION_ENABLED"]:
        fail("Jungle Abomination final must remain feature-gated until encounter is enabled")
    if flags["ANCIENT_REMNANT_ENABLED"]:
        fail("Ancient Remnant final must remain feature-gated until encounter is enabled")

    print("Validated CozyCrazyCraft semantic social-progression policy")


if __name__ == "__main__":
    main()
