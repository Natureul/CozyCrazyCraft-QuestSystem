#!/usr/bin/env python3
"""Ensure every machine-readable quest remains discoverable in the root QUESTS.md browser."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CATALOG = ROOT / "data" / "quest_catalog"
BROWSER = ROOT / "QUESTS.md"

errors: list[str] = []


def main() -> int:
    try:
        browser = BROWSER.read_text(encoding="utf-8")
    except Exception as exc:
        print(f"ERROR: cannot read QUESTS.md: {exc}")
        return 1

    quest_ids: set[str] = set()
    quest_titles: list[tuple[str, str, str]] = []

    for path in sorted(CATALOG.glob("*.json")):
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except Exception as exc:
            errors.append(f"{path.relative_to(ROOT)}: invalid JSON: {exc}")
            continue

        region = str(data.get("display_region") or data.get("region") or path.stem)
        for quest in data.get("quests", []):
            if not isinstance(quest, dict):
                continue
            qid = quest.get("id")
            title = quest.get("title")
            if isinstance(qid, str):
                quest_ids.add(qid)
            if isinstance(qid, str) and isinstance(title, str):
                quest_titles.append((qid, title, region))

    missing = [(qid, title, region) for qid, title, region in quest_titles if title not in browser]
    if missing:
        for qid, title, region in missing:
            errors.append(f"QUESTS.md missing {region} quest {qid}: {title}")

    # The headline count should not quietly become stale.
    expected_count = len(quest_ids)
    count_markers = [
        f"**{expected_count} quest concepts**",
        f"all {expected_count} current quests",
        f"**{expected_count}**",
    ]
    if not any(marker in browser for marker in count_markers):
        errors.append(f"QUESTS.md does not visibly reflect the current catalog count ({expected_count})")

    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} quest-browser synchronization error(s)")
        return 1

    print(f"OK: QUESTS.md exposes all {expected_count} catalogued quests")
    return 0


if __name__ == "__main__":
    sys.exit(main())
