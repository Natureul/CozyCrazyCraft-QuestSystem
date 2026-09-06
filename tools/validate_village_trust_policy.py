#!/usr/bin/env python3
"""Validate CozyCrazyCraft Village Trust and board cadence policy."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
POLICY = ROOT / "data" / "village_trust_policy.json"
BOUNTIFUL = ROOT / "deployment" / "config" / "bountiful" / "bountiful.json"

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
    config = load(BOUNTIFUL)
    if not isinstance(policy, dict) or not isinstance(config, dict):
        return finish()

    population = policy.get("board_population", {})
    minimum = population.get("minimum_visible_notices")
    preferred = population.get("preferred_visible_notices")
    maximum = population.get("maximum_visible_notices")
    if not all(isinstance(x, int) for x in (minimum, preferred, maximum)):
        errors.append("board population min/preferred/max must be integers")
    elif not (3 <= minimum <= preferred <= maximum <= 10):
        errors.append("board population must stay compact and ordered (3..10)")

    rotation = policy.get("rotation", {})
    seconds = rotation.get("board_update_frequency_seconds")
    if not isinstance(seconds, int) or seconds < 60:
        errors.append("board update cadence must be at least 60 seconds")
    if config.get("boardUpdateFrequency") != seconds:
        errors.append("Bountiful boardUpdateFrequency must match Village Trust policy")
    if rotation.get("accepted_bounties_expire") is not False:
        errors.append("Village Trust policy requires non-expiring accepted bounties")
    if config.get("shouldBountiesHaveTimersAndExpire") is not False:
        errors.append("Bountiful accepted-bounty timers must be disabled")

    stages = policy.get("trust_stages")
    if not isinstance(stages, list) or len(stages) < 4:
        errors.append("trust_stages must define a useful progression")
    else:
        reps = [stage.get("minimum_reputation") for stage in stages if isinstance(stage, dict)]
        if len(reps) != len(stages) or not all(isinstance(x, int) for x in reps):
            errors.append("each trust stage needs integer minimum_reputation")
        elif reps != sorted(set(reps)):
            errors.append("trust stage reputation thresholds must be unique and ascending")
        if reps and reps[0] != 0:
            errors.append("first trust stage must begin at reputation 0")

    anti = policy.get("anti_grind_rules", {})
    ceiling = anti.get("no_main_story_gate_above_reputation")
    if not isinstance(ceiling, int) or ceiling > 5:
        errors.append("main-story trust gates must not exceed reputation 5")
    if anti.get("do_not_require_repetitive_bulk_jobs_to_unlock_story") is not True:
        errors.append("policy must explicitly reject repetitive story grind")

    facts = policy.get("bountiful_6_0_4_facts", {})
    if facts.get("accepted_bounty_timer_policy") != "disabled":
        errors.append("source-audited 6.0.4 timer policy must remain disabled")
    if "repRequired" not in str(facts.get("objective_rep_required_native_bug_or_gap", "")):
        warnings.append("objective repRequired compatibility note is missing")

    return finish()


def finish() -> int:
    for warning in warnings:
        print(f"WARNING: {warning}")
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} error(s), {len(warnings)} warning(s)")
        return 1
    print(f"OK: village trust/cadence policy validation passed ({len(warnings)} warning(s))")
    return 0


if __name__ == "__main__":
    sys.exit(main())
