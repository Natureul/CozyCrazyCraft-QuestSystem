#!/usr/bin/env python3
"""Validate cosmetic story-board metadata against the current Bountiful smoke-test pool."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
POOL = ROOT / "deployment" / "config" / "bountiful" / "bounty_pools" / "ccc_local_objs.json"
CARDS = ROOT / "data" / "ui" / "story_cards_local.json"
PROFILES = ROOT / "data" / "ui" / "region_profiles.json"

errors: list[str] = []
warnings: list[str] = []


def load(path: Path):
    try:
        with path.open("r", encoding="utf-8") as fh:
            return json.load(fh)
    except Exception as exc:
        errors.append(f"{path.relative_to(ROOT)}: invalid JSON: {exc}")
        return None


def check_short_text(label: str, value, max_len: int, required: bool = True) -> None:
    if not isinstance(value, str) or not value.strip():
        if required:
            errors.append(f"{label}: expected non-empty text")
        return
    if len(value) > max_len:
        warnings.append(f"{label}: may be too long for compact board UI ({len(value)} chars; target <= {max_len})")


def main() -> int:
    pool = load(POOL)
    cards = load(CARDS)
    profiles = load(PROFILES)

    if not all(isinstance(x, dict) for x in (pool, cards, profiles)):
        return finish()

    pool_entries = set((pool.get("content") or {}).keys())
    story_cards = cards.get("cards")
    region_ids = set((profiles.get("regions") or {}).keys())
    notice_classes = set((profiles.get("notice_classes") or {}).keys())

    if cards.get("schema_version") != 1:
        errors.append("story_cards_local.json: schema_version must be 1")

    if not isinstance(story_cards, dict) or not story_cards:
        errors.append("story_cards_local.json: cards must be a non-empty object")
        return finish()

    card_entries = set(story_cards.keys())

    missing = sorted(pool_entries - card_entries)
    extra = sorted(card_entries - pool_entries)
    if missing:
        errors.append("Local Notices entries missing story cards: " + ", ".join(missing))
    if extra:
        errors.append("Story cards reference unknown Local Notices entries: " + ", ".join(extra))

    for entry_id, card in sorted(story_cards.items()):
        label = f"story_cards_local.json::{entry_id}"
        if not isinstance(card, dict):
            errors.append(f"{label}: card must be an object")
            continue

        check_short_text(f"{label}.title", card.get("title"), 48)
        check_short_text(f"{label}.issuer", card.get("issuer"), 36)
        check_short_text(f"{label}.body", card.get("body"), 220)

        notice_class = card.get("notice_class")
        if not isinstance(notice_class, str) or not notice_class.strip():
            errors.append(f"{label}: missing non-empty 'notice_class'")
        elif notice_class not in notice_classes:
            errors.append(f"{label}: unknown notice_class {notice_class!r}")

        regional = card.get("regional_body")
        if regional is not None:
            if not isinstance(regional, dict):
                errors.append(f"{label}.regional_body must be an object when present")
            else:
                unknown = sorted(set(regional) - region_ids)
                if unknown:
                    errors.append(f"{label}.regional_body has unknown region keys: {', '.join(unknown)}")
                missing_regions = sorted(region_ids - set(regional))
                if missing_regions:
                    warnings.append(
                        f"{label}.regional_body does not cover every UI region: {', '.join(missing_regions)}; base body will be fallback"
                    )
                for region, text in sorted(regional.items()):
                    check_short_text(f"{label}.regional_body.{region}", text, 220)

    return finish()


def finish() -> int:
    for warning in warnings:
        print(f"WARNING: {warning}")
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} error(s), {len(warnings)} warning(s)")
        return 1
    print(f"OK: story-board metadata validation passed ({len(warnings)} warning(s))")
    return 0


if __name__ == "__main__":
    sys.exit(main())
