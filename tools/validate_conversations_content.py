#!/usr/bin/env python3
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONVERSATIONS = ROOT / "runtime" / "src" / "main" / "resources" / "data" / "cozycrazyquests" / "conversations"
REQUIRED = {
    "cartographer_first_real_map.json",
    "cartographer_quest_active.json",
    "cartographer_quest_turnin.json",
}

ALLOWED_ACTION_PREFIXES = (
    "give.item(",
    "dialogue.close",
    "dialogue.goto(",
    "dialogue.replay",
    "villager.trade",
    "villager.awardReputation(",
    "action.global(",
    "debug(",
)


def fail(message: str) -> None:
    raise SystemExit(f"Conversations validation failed: {message}")


def validate_action(action: object, source: Path) -> None:
    if not isinstance(action, dict):
        fail(f"{source}: action must be an object")
    text = action.get("action")
    if not isinstance(text, str) or not text:
        fail(f"{source}: action object needs non-empty 'action' string")
    if not text.startswith(ALLOWED_ACTION_PREFIXES):
        fail(f"{source}: unsupported Conversations 1.0.5 action syntax: {text}")


def validate_reply(reply: object, source: Path) -> None:
    if not isinstance(reply, dict) or not isinstance(reply.get("reply"), str):
        fail(f"{source}: each reply needs a string 'reply'")
    actions = reply.get("action")
    if actions is None:
        return
    if isinstance(actions, dict):
        validate_action(actions, source)
    elif isinstance(actions, list) and actions:
        for action in actions:
            validate_action(action, source)
    else:
        fail(f"{source}: reply 'action' must be an object or non-empty array")


def validate_file(path: Path) -> None:
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        fail(f"{path}: invalid JSON: {exc}")

    if not isinstance(data, dict):
        fail(f"{path}: root must be an object")
    if not isinstance(data.get("starting_index", 0), int):
        fail(f"{path}: starting_index must be an integer")

    dialogues = data.get("dialogues")
    if not isinstance(dialogues, list) or not dialogues:
        fail(f"{path}: Conversations requires a non-empty dialogues array")

    for container in dialogues:
        if not isinstance(container, dict):
            fail(f"{path}: dialogue container must be an object")
        options = container.get("dialogue_options")
        if not isinstance(options, list) or not options:
            fail(f"{path}: dialogue container needs dialogue_options")
        for option in options:
            if not isinstance(option, dict) or not isinstance(option.get("dialogue"), str):
                fail(f"{path}: each dialogue option needs a dialogue string")
            # Conversations 1.0.5 defaults a missing option condition to literal 'null', which is
            # not an unconditional condition. Require explicit conditions so an authored line can
            # never disappear because of that parser quirk.
            condition = option.get("condition")
            if not isinstance(condition, str) or not condition:
                fail(f"{path}: every dialogue option must explicitly declare a condition")
            replies = option.get("replies", [])
            if not isinstance(replies, list):
                fail(f"{path}: replies must be an array")
            for reply in replies:
                validate_reply(reply, path)
            actions = option.get("actions", [])
            if not isinstance(actions, list):
                fail(f"{path}: dialogue actions must be an array")
            for action in actions:
                validate_action(action, path)


def main() -> None:
    if not CONVERSATIONS.is_dir():
        fail(f"missing dialogue directory {CONVERSATIONS}")
    present = {path.name for path in CONVERSATIONS.glob("*.json")}
    missing = REQUIRED - present
    if missing:
        fail(f"missing required dialogue files: {sorted(missing)}")
    for path in sorted(CONVERSATIONS.glob("*.json")):
        validate_file(path)
    print(f"Validated {len(present)} CozyCrazyCraft Conversations dialogue files")


if __name__ == "__main__":
    main()
