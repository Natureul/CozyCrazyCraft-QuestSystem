#!/usr/bin/env python3
"""Validate the first regional repeatable expansion.

This is intentionally stricter than prose design: each current region/tier cell must
have exactly one additional repeatable concept and every objective must use a
Bountiful-safe item, item_tag, or entity primitive.
"""

from __future__ import annotations

import json
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PATH = ROOT / "data" / "quest_catalog" / "repeatable_expansion_01.json"
REGIONS = {"NORTH", "EAST", "SOUTH", "WEST"}
TIERS = {"HEARTHLANDS", "FRONTIER", "WILDLANDS", "DREAD_REACHES"}
SAFE_TYPES = {"item", "item_tag", "entity"}

errors: list[str] = []


def main() -> int:
    try:
        data = json.loads(PATH.read_text(encoding="utf-8"))
    except Exception as exc:
        print(f"ERROR: cannot parse {PATH.relative_to(ROOT)}: {exc}")
        return 1

    quests = data.get("quests")
    if not isinstance(quests, list):
        print("ERROR: quests must be a list")
        return 1

    seen: set[str] = set()
    cells = Counter()
    for q in quests:
        qid = q.get("id")
        if not isinstance(qid, str) or not qid:
            errors.append("quest missing id")
            continue
        if qid in seen:
            errors.append(f"duplicate id: {qid}")
        seen.add(qid)

        region = q.get("region")
        tier = q.get("tier")
        if region not in REGIONS:
            errors.append(f"{qid}: bad region {region!r}")
        if tier not in TIERS:
            errors.append(f"{qid}: bad tier {tier!r}")
        if region in REGIONS and tier in TIERS:
            cells[(region, tier)] += 1

        for key in ("title", "issuer", "body", "notice_class", "status"):
            if not isinstance(q.get(key), str) or not q[key].strip():
                errors.append(f"{qid}: missing {key}")

        objective = q.get("objective")
        if not isinstance(objective, dict):
            errors.append(f"{qid}: missing objective")
        else:
            if objective.get("type") not in SAFE_TYPES:
                errors.append(f"{qid}: unsafe objective type {objective.get('type')!r}")
            if not isinstance(objective.get("content"), str) or not objective["content"]:
                errors.append(f"{qid}: objective content missing")
            lo, hi = objective.get("min"), objective.get("max")
            if not isinstance(lo, int) or not isinstance(hi, int) or lo < 1 or hi < lo:
                errors.append(f"{qid}: invalid objective amount")

        reward = q.get("reward")
        if not isinstance(reward, dict) or reward.get("type") != "item":
            errors.append(f"{qid}: current expansion rewards must be simple item rewards")

    if len(quests) != 16:
        errors.append(f"expected 16 repeatable quests, found {len(quests)}")

    for region in sorted(REGIONS):
        for tier in sorted(TIERS):
            count = cells[(region, tier)]
            if count != 1:
                errors.append(f"{region}/{tier}: expected exactly 1 expansion quest, found {count}")

    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} error(s)")
        return 1

    print("OK: repeatable expansion validation passed (16 quests; one per region/tier cell)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
