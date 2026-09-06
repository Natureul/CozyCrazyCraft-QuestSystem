#!/usr/bin/env python3
"""Reference implementation of CozyCrazyCraft quest/map target selection.

This is an executable specification for the future Forge runtime. It intentionally
operates on already-discovered candidate instances; Minecraft structure discovery is
not implemented here.

The Java runtime should match these semantics unless playtesting changes the policy.
"""

from __future__ import annotations

from dataclasses import dataclass
from math import hypot
from typing import Iterable, Optional
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
POLICY_PATH = ROOT / "data" / "targeting_policy.json"

TIER_ORDER = ["HEARTHLANDS", "FRONTIER", "WILDLANDS", "DREAD_REACHES"]
TIER_INDEX = {tier: index for index, tier in enumerate(TIER_ORDER)}


@dataclass(frozen=True)
class Source:
    x: float
    z: float
    spawn_x: float
    spawn_z: float
    dimension: str
    tier: str
    macro_region: str

    @property
    def radial_distance(self) -> float:
        return hypot(self.x - self.spawn_x, self.z - self.spawn_z)


@dataclass(frozen=True)
class Candidate:
    candidate_id: str
    x: float
    z: float
    dimension: str
    tier: str
    macro_region: str
    influence_band: str
    structure_allowed: bool = True
    active_assignment: bool = False
    recently_used: bool = False
    explored: bool = False

    def board_distance(self, source: Source) -> float:
        return hypot(self.x - source.x, self.z - source.z)

    def radial_distance(self, source: Source) -> float:
        return hypot(self.x - source.spawn_x, self.z - source.spawn_z)


@dataclass(frozen=True)
class SelectionRequest:
    scope: str
    required_region: Optional[str] = None
    target_tier: Optional[str] = None


@dataclass(frozen=True)
class ScoredCandidate:
    candidate: Candidate
    distance: float
    score: float
    reasons: tuple[str, ...]


def load_policy() -> dict:
    return json.loads(POLICY_PATH.read_text(encoding="utf-8"))


def expected_target_tier(source_tier: str, request: SelectionRequest) -> str:
    if request.target_tier:
        return request.target_tier
    if request.scope == "OUTWARD_LEAD":
        idx = TIER_INDEX[source_tier]
        if idx >= len(TIER_ORDER) - 1:
            raise ValueError("Dread Reaches cannot have a one-tier outward lead")
        return TIER_ORDER[idx + 1]
    if request.scope == "LEGENDARY_DESTINATION":
        return "DREAD_REACHES"
    return source_tier


def envelope_for(policy: dict, source: Source, request: SelectionRequest) -> dict:
    scope = policy["scopes"][request.scope]
    # Outward leads are indexed by the issuing/source tier; legendary destinations
    # currently use their destination Dread envelope.
    key = "DREAD_REACHES" if request.scope == "LEGENDARY_DESTINATION" else source.tier
    if key not in scope:
        raise ValueError(f"No distance envelope for {request.scope}/{key}")
    return scope[key]


def distance_fit(distance: float, envelope: dict) -> float:
    """0..100 preference curve peaking at ideal and tapering to each hard edge."""
    lo = float(envelope["min"])
    ideal = float(envelope["ideal"])
    soft = float(envelope["soft_max"])
    hard = float(envelope["hard_max"])

    if distance < lo or distance > hard:
        return float("-inf")
    if distance <= ideal:
        width = ideal - lo
        return 100.0 if width <= 0 else 100.0 * (distance - lo) / width
    if distance <= soft:
        width = soft - ideal
        return 100.0 if width <= 0 else 100.0 - 50.0 * (distance - ideal) / width
    width = hard - soft
    return 50.0 if width <= 0 else 50.0 * (hard - distance) / width


def evaluate_candidate(
    policy: dict,
    source: Source,
    request: SelectionRequest,
    candidate: Candidate,
) -> tuple[bool, str | ScoredCandidate]:
    hard = policy["hard_rules"]
    envelope = envelope_for(policy, source, request)
    required_tier = expected_target_tier(source.tier, request)
    required_region = request.required_region

    if hard.get("same_dimension", True) and candidate.dimension != source.dimension:
        return False, "wrong_dimension"
    if hard.get("require_cozy_zones_structure_allowed", True) and not candidate.structure_allowed:
        return False, "cozy_zones_rejected"
    if candidate.active_assignment and hard.get("do_not_reuse_active_assignment", True):
        return False, "already_active"
    if hard.get("respect_target_radial_tier", True) and candidate.tier != required_tier:
        return False, f"wrong_tier:{candidate.tier}!=${required_tier}".replace("$", "")
    if required_region and hard.get("respect_target_macro_region_when_regional", True):
        if candidate.macro_region != required_region:
            return False, f"wrong_region:{candidate.macro_region}!={required_region}"

    distance = candidate.board_distance(source)
    fit = distance_fit(distance, envelope)
    if fit == float("-inf"):
        return False, f"outside_distance_envelope:{distance:.1f}"

    reasons: list[str] = [f"distance_fit={fit:.2f}"]
    score = fit

    influence_bonus = {
        "SHARED_CORE": 0.0,
        "CARDINAL_TRANSITION": 4.0,
        "ESTABLISHED": 10.0,
    }.get(candidate.influence_band, 0.0)
    score += influence_bonus
    reasons.append(f"influence={influence_bonus:+.1f}")

    if not candidate.explored:
        score += 8.0
        reasons.append("unexplored=+8")

    if candidate.recently_used:
        score -= 25.0
        reasons.append("recent_duplicate=-25")

    if request.scope == "OUTWARD_LEAD":
        radial_gain = candidate.radial_distance(source) - source.radial_distance
        minimum_gain = float(hard["outward_leads_min_radial_gain_blocks"])
        if hard.get("outward_leads_require_positive_radial_gain", True) and radial_gain < minimum_gain:
            return False, f"insufficient_outward_gain:{radial_gain:.1f}<{minimum_gain:.1f}"
        outward_bonus = min(12.0, max(0.0, (radial_gain - minimum_gain) / 150.0))
        score += outward_bonus
        reasons.append(f"outward_gain={radial_gain:.1f};bonus={outward_bonus:+.1f}")

    return True, ScoredCandidate(candidate, distance, score, tuple(reasons))


def rank_candidates(
    source: Source,
    request: SelectionRequest,
    candidates: Iterable[Candidate],
    policy: Optional[dict] = None,
) -> tuple[list[ScoredCandidate], dict[str, str]]:
    policy = policy or load_policy()
    accepted: list[ScoredCandidate] = []
    rejected: dict[str, str] = {}

    for candidate in candidates:
        ok, result = evaluate_candidate(policy, source, request, candidate)
        if ok:
            accepted.append(result)  # type: ignore[arg-type]
        else:
            rejected[candidate.candidate_id] = str(result)

    # Highest score wins. Shorter distance is a deterministic secondary tie-break,
    # then stable candidate ID for reproducibility.
    accepted.sort(key=lambda row: (-row.score, row.distance, row.candidate.candidate_id))
    return accepted, rejected


def select_target(
    source: Source,
    request: SelectionRequest,
    candidates: Iterable[Candidate],
    policy: Optional[dict] = None,
) -> Optional[ScoredCandidate]:
    ranked, _ = rank_candidates(source, request, candidates, policy)
    return ranked[0] if ranked else None


if __name__ == "__main__":
    # Small manual demo; CI exercises the richer fixture file.
    board = Source(1200, 0, 0, 0, "minecraft:overworld", "HEARTHLANDS", "WEST")
    request = SelectionRequest("LOCAL_SITE", required_region="WEST")
    examples = [
        Candidate("near", 1610, 0, "minecraft:overworld", "HEARTHLANDS", "WEST", "ESTABLISHED"),
        Candidate("ideal", 2020, 0, "minecraft:overworld", "HEARTHLANDS", "WEST", "ESTABLISHED"),
        Candidate("wrong-region", 1950, 0, "minecraft:overworld", "HEARTHLANDS", "NORTH", "ESTABLISHED"),
    ]
    ranked, rejected = rank_candidates(board, request, examples)
    for row in ranked:
        print(f"{row.candidate.candidate_id}: {row.distance:.1f} blocks, score={row.score:.2f}, {', '.join(row.reasons)}")
    for cid, reason in rejected.items():
        print(f"REJECT {cid}: {reason}")
