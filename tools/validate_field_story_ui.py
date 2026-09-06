#!/usr/bin/env python3
"""Validate story-card metadata for the four regional field-job Bountiful pools."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
POOLS = ROOT / "deployment" / "config" / "bountiful" / "bounty_pools"
CARDS = ROOT / "data" / "ui" / "story_cards_field.json"
PROFILES = ROOT / "data" / "ui" / "region_profiles.json"
POOL_FILES = {
    "NORTH": "ccc_field_north_objs.json",
    "EAST": "ccc_field_east_objs.json",
    "SOUTH": "ccc_field_south_objs.json",
    "WEST": "ccc_field_west_objs.json",
}
TIERS = {"HEARTHLANDS", "FRONTIER", "WILDLANDS", "DREAD_REACHES"}

errors: list[str] = []


def load(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"{path.relative_to(ROOT)}: invalid JSON: {exc}")
        return None


def main() -> int:
    cards_doc = load(CARDS)
    profiles = load(PROFILES)
    if not isinstance(cards_doc, dict) or not isinstance(profiles, dict):
        return finish()

    cards = cards_doc.get("cards")
    if not isinstance(cards, dict):
        errors.append("story_cards_field.json: cards must be an object")
        return finish()

    notice_classes = set((profiles.get("notice_classes") or {}).keys())
    pool_region: dict[str, str] = {}

    for region, filename in POOL_FILES.items():
        doc = load(POOLS / filename)
        if not isinstance(doc, dict):
            continue
        content = doc.get("content")
        if not isinstance(content, dict):
            errors.append(f"{filename}: content must be an object")
            continue
        for entry_id in content:
            if entry_id in pool_region:
                errors.append(f"duplicate field objective id {entry_id}")
            pool_region[entry_id] = region

    missing = sorted(set(pool_region) - set(cards))
    extra = sorted(set(cards) - set(pool_region))
    if missing:
        errors.append("field objectives missing story cards: " + ", ".join(missing))
    if extra:
        errors.append("field story cards reference unknown objectives: " + ", ".join(extra))

    for entry_id, card in cards.items():
        if not isinstance(card, dict):
            errors.append(f"{entry_id}: card must be an object")
            continue
        for key in ("region", "tier", "title", "issuer", "notice_class", "body"):
            if not isinstance(card.get(key), str) or not card[key].strip():
                errors.append(f"{entry_id}: missing {key}")
        if card.get("region") != pool_region.get(entry_id):
            errors.append(f"{entry_id}: card region does not match its pool")
        if card.get("tier") not in TIERS:
            errors.append(f"{entry_id}: invalid tier {card.get('tier')!r}")
        if card.get("notice_class") not in notice_classes:
            errors.append(f"{entry_id}: unknown notice class {card.get('notice_class')!r}")
        if isinstance(card.get("body"), str) and len(card["body"]) > 240:
            errors.append(f"{entry_id}: body is too long for compact board UI")

    if len(cards) != 16:
        errors.append(f"expected 16 field story cards, found {len(cards)}")

    return finish()


def finish() -> int:
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} error(s)")
        return 1
    print("OK: regional field-job story metadata validation passed (16 cards)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
