#!/usr/bin/env python3
"""Ensure every regional repeatable expansion concept is visible in FIELD_JOBS.md."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CATALOG = ROOT / "data" / "quest_catalog" / "repeatable_expansion_01.json"
BROWSER = ROOT / "FIELD_JOBS.md"

errors: list[str] = []


def main() -> int:
    try:
        data = json.loads(CATALOG.read_text(encoding="utf-8"))
        browser = BROWSER.read_text(encoding="utf-8")
    except Exception as exc:
        print(f"ERROR: unable to read field-job catalog/browser: {exc}")
        return 1

    quests = data.get("quests")
    if not isinstance(quests, list):
        print("ERROR: repeatable expansion quests must be a list")
        return 1

    ids: set[str] = set()
    for quest in quests:
        if not isinstance(quest, dict):
            continue
        qid = quest.get("id")
        title = quest.get("title")
        if isinstance(qid, str):
            ids.add(qid)
        if isinstance(qid, str) and isinstance(title, str) and title not in browser:
            errors.append(f"FIELD_JOBS.md missing {qid}: {title}")

    count = len(ids)
    if f"**{count} field-job concepts" not in browser and f"**{count}**" not in browser:
        errors.append(f"FIELD_JOBS.md does not visibly reflect field-job count ({count})")

    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} field-job browser synchronization error(s)")
        return 1

    print(f"OK: FIELD_JOBS.md exposes all {count} regional field jobs")
    return 0


if __name__ == "__main__":
    sys.exit(main())
