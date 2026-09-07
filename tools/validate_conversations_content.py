#!/usr/bin/env python3
from __future__ import annotations

import json
import re
from collections import defaultdict
from difflib import SequenceMatcher
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA_ROOT = ROOT / "runtime" / "src" / "main" / "resources" / "data" / "cozycrazyquests"
CONVERSATIONS = DATA_ROOT / "conversations"
DIALOGUE_META = DATA_ROOT / "dialogue"
VOICE_MANIFEST = DIALOGUE_META / "dialogue_voice_manifest_v0_2.json"
CONTEXT_BANK = DIALOGUE_META / "context_voice_banks_v0_2.json"

REQUIRED = {
    "cartographer_first_real_map.json",
    "cartographer_quest_active.json",
    "cartographer_quest_turnin.json",
    "guard_local.json",
    "villager_armorer.json",
    "villager_butcher.json",
    "villager_cartographer.json",
    "villager_child.json",
    "villager_cleric.json",
    "villager_farmer.json",
    "villager_fisherman.json",
    "villager_fletcher.json",
    "villager_leatherworker.json",
    "villager_librarian.json",
    "villager_mason.json",
    "villager_nitwit.json",
    "villager_shepherd.json",
    "villager_toolsmith.json",
    "villager_unemployed.json",
    "villager_weaponsmith.json",
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

# The custom CozyCrazyCraft screen is substantially more compact and faster than the stock
# Conversations 1.0.5 screen, but spoken dialogue should still be speech-sized rather than prose.
MAX_DIALOGUE_CHARS = 220
MAX_NORMAL_CHAR_DELAY = 0.75

# Conversation Bible v0.2 voice-pass guardrails. These apply to resources explicitly listed in
# dialogue_voice_manifest_v0_2.json, not every legacy quest conversation in the repository.
MAX_VOICE_WORDS = 35
MAX_REPLY_WORDS = 12
NEAR_DUP_MIN_CHARS = 45
NEAR_DUP_RATIO = 0.92
PHRASE_WORDS = 6
MAX_PHRASE_LINES = 3

REQUIRED_PROFESSIONS = {
    "armorer",
    "butcher",
    "cartographer",
    "cleric",
    "farmer",
    "fisherman",
    "fletcher",
    "leatherworker",
    "librarian",
    "mason",
    "shepherd",
    "toolsmith",
    "weaponsmith",
}
KNOWLEDGE_STATES = ("UNKNOWN", "RUMOR", "LEAD", "KNOWN", "CONFIRMED")
REGIONS = ("SHARED_CORE", "NORTH", "EAST", "SOUTH", "WEST")
TIERS = ("HEARTHLANDS", "FRONTIER", "WILDLANDS", "DREAD_REACHES")
TRUST_STATES = ("STRANGER", "RECOGNIZED", "RELIABLE", "TRUSTED", "PROVEN")
FACT_KINDS = (
    "STRUCTURE_SURVEY",
    "COMMUNITY_INCIDENT",
    "ECOLOGY",
    "ROAD",
    "WATER",
    "CAVE",
    "RECENT_EVENT",
)
PROVENANCE_KINDS = (
    "local_observation",
    "profession_evidence",
    "village_report",
    "map_record",
    "old_record",
    "player_report",
    "world_event",
    "rumor_network",
    "quest_proof",
)
PLAYER_INTENTS = (
    "WHERE_EXACTLY",
    "CAN_YOU_MARK_IT",
    "WHO_SAW_IT",
    "HOW_DANGEROUS",
    "WHO_ELSE_WOULD_KNOW",
    "WHAT_AM_I_LOOKING_FOR",
)

TOKEN_PATTERNS = (
    re.compile(r"\{[A-Za-z][A-Za-z0-9_]*\}"),
    re.compile(r"\$\{[^}]+\}"),
    re.compile(r"\{\{[^}]+\}\}"),
    re.compile(r"%\([^)]+\)[a-zA-Z]"),
)


def fail(message: str) -> None:
    raise SystemExit(f"Conversations validation failed: {message}")


def load_json(path: Path) -> object:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        fail(f"{path}: invalid JSON: {exc}")


def validate_action(action: object, source: Path) -> None:
    if not isinstance(action, dict):
        fail(f"{source}: action must be an object")
    text = action.get("action")
    if not isinstance(text, str) or not text:
        fail(f"{source}: action object needs non-empty 'action' string")
    if not text.startswith(ALLOWED_ACTION_PREFIXES):
        fail(f"{source}: unsupported Conversations 1.0.5 action syntax: {text}")

    # Conversations 1.0.5 parses give.item as count,item_id{nbt}; a comma between the item id and
    # opening NBT brace silently makes the registry id end in a comma. This exact bug caused the
    # first in-game "I'll survey it" button to appear to do nothing.
    if text.startswith("give.item(") and ",{" in text:
        fail(f"{source}: invalid Conversations give.item syntax has comma before NBT: {text}")


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


def validate_timings(option: dict, source: Path) -> None:
    timings = option.get("timings")
    if not isinstance(timings, list) or len(timings) != 4:
        fail(f"{source}: every dialogue option must declare exactly four timing values")
    if not all(isinstance(value, (int, float)) and value >= 0 for value in timings):
        fail(f"{source}: timings must be four non-negative numbers")
    if timings[0] > MAX_NORMAL_CHAR_DELAY:
        fail(
            f"{source}: normal character delay {timings[0]} is too slow for CozyCrazyCraft dialogue "
            f"(max {MAX_NORMAL_CHAR_DELAY})"
        )


def validate_file(path: Path) -> None:
    data = load_json(path)
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
            dialogue = option["dialogue"]
            if len(dialogue) > MAX_DIALOGUE_CHARS:
                fail(
                    f"{path}: dialogue beat is {len(dialogue)} chars; split/shorten it below "
                    f"{MAX_DIALOGUE_CHARS}"
                )
            condition = option.get("condition")
            if not isinstance(condition, str) or not condition:
                fail(f"{path}: every dialogue option must explicitly declare a condition")
            validate_timings(option, path)
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


def resource_path(resource: str) -> Path:
    if not isinstance(resource, str) or ":" not in resource:
        fail(f"voice manifest has invalid resource id: {resource!r}")
    namespace, path = resource.split(":", 1)
    if namespace != "cozycrazyquests":
        fail(f"voice manifest resource must use cozycrazyquests namespace: {resource}")
    if "/" in path:
        return DATA_ROOT / f"{path}.json"
    return CONVERSATIONS / f"{path}.json"


def conversation_text(path: Path) -> tuple[list[str], list[str]]:
    data = load_json(path)
    if not isinstance(data, dict):
        fail(f"{path}: voice-audited conversation root must be an object")
    beats: list[str] = []
    replies: list[str] = []
    for container in data.get("dialogues", []):
        if not isinstance(container, dict):
            continue
        for option in container.get("dialogue_options", []):
            if not isinstance(option, dict):
                continue
            dialogue = option.get("dialogue")
            if isinstance(dialogue, str):
                beats.append(dialogue)
            for reply in option.get("replies", []):
                if isinstance(reply, dict) and isinstance(reply.get("reply"), str):
                    replies.append(reply["reply"])
    return beats, replies


def word_count(text: str) -> int:
    return len(re.findall(r"[A-Za-z0-9]+(?:['’][A-Za-z0-9]+)?", text))


def normalized(text: str) -> str:
    text = text.casefold().replace("’", "'")
    text = re.sub(r"[^a-z0-9'\s]+", " ", text)
    return re.sub(r"\s+", " ", text).strip()


def token_words(text: str) -> list[str]:
    return re.findall(r"[a-z0-9]+(?:'[a-z0-9]+)?", normalized(text))


def validate_no_template_tokens(text: str, source: Path) -> None:
    for pattern in TOKEN_PATTERNS:
        match = pattern.search(text)
        if match:
            fail(
                f"{source}: unsupported/unwired template token {match.group(0)!r} in spoken dialogue; "
                "ConversationBridge currently assigns static resources"
            )


def require_keys(mapping: object, required: tuple[str, ...] | set[str], label: str) -> dict:
    if not isinstance(mapping, dict):
        fail(f"{label} must be an object")
    missing = set(required) - set(mapping)
    if missing:
        fail(f"{label} missing required entries: {sorted(missing)}")
    return mapping


def validate_voice_manifest() -> tuple[dict, set[str]]:
    if not VOICE_MANIFEST.is_file():
        fail(f"missing Conversation Bible v0.2 voice manifest: {VOICE_MANIFEST}")
    manifest = load_json(VOICE_MANIFEST)
    if not isinstance(manifest, dict):
        fail(f"{VOICE_MANIFEST}: root must be an object")
    if manifest.get("schema_version") != 2:
        fail(f"{VOICE_MANIFEST}: schema_version must be 2")
    if manifest.get("authority") != "docs/CozyCrazyCraft_Conversation_Bible_v0.2.md":
        fail(f"{VOICE_MANIFEST}: authority must point to Conversation Bible v0.2")

    profiles = manifest.get("ambient_profiles")
    if not isinstance(profiles, list) or not profiles:
        fail(f"{VOICE_MANIFEST}: ambient_profiles must be a non-empty array")

    audited: set[str] = set()
    profession_slots: dict[str, set[int]] = defaultdict(set)
    profession_personalities: dict[str, set[str]] = defaultdict(set)
    seen_resources: set[str] = set()
    for profile in profiles:
        if not isinstance(profile, dict):
            fail(f"{VOICE_MANIFEST}: each ambient profile must be an object")
        resource = profile.get("resource")
        profession = profile.get("profession")
        personality = profile.get("personality")
        slot = profile.get("profile_slot")
        niche = profile.get("knowledge_niche")
        if not all(isinstance(value, str) and value for value in (resource, profession, personality, niche)):
            fail(f"{VOICE_MANIFEST}: ambient profile needs resource/profession/personality/knowledge_niche strings")
        if resource in seen_resources:
            fail(f"{VOICE_MANIFEST}: duplicate ambient resource {resource}")
        seen_resources.add(resource)
        audited.add(resource)
        path = resource_path(resource)
        if not path.is_file():
            fail(f"{VOICE_MANIFEST}: ambient resource does not exist: {resource} -> {path}")
        if profession in REQUIRED_PROFESSIONS:
            if not isinstance(slot, int) or slot not in (1, 2, 3):
                fail(f"{VOICE_MANIFEST}: {profession} needs integer profile_slot 1/2/3, got {slot!r}")
            profession_slots[profession].add(slot)
            profession_personalities[profession].add(personality)

    for profession in sorted(REQUIRED_PROFESSIONS):
        if profession_slots[profession] != {1, 2, 3}:
            fail(
                f"{VOICE_MANIFEST}: {profession} must expose stable ambient slots 1/2/3; "
                f"found {sorted(profession_slots[profession])}"
            )
        if len(profession_personalities[profession]) < 3:
            fail(f"{VOICE_MANIFEST}: {profession} needs three distinct personality profiles")

    families = require_keys(
        manifest.get("knowledge_state_resources"),
        {"structure", "underground"},
        f"{VOICE_MANIFEST}: knowledge_state_resources",
    )
    for family_name in ("structure", "underground"):
        family = require_keys(
            families[family_name],
            KNOWLEDGE_STATES,
            f"{VOICE_MANIFEST}: {family_name} knowledge states",
        )
        resources = [family[state] for state in KNOWLEDGE_STATES]
        if len(set(resources)) != len(resources):
            fail(f"{VOICE_MANIFEST}: {family_name} states must not collapse onto the same resource")
        for state, resource in zip(KNOWLEDGE_STATES, resources):
            if not isinstance(resource, str):
                fail(f"{VOICE_MANIFEST}: {family_name}.{state} must be a resource id string")
            audited.add(resource)
            path = resource_path(resource)
            if not path.is_file():
                fail(f"{VOICE_MANIFEST}: missing {family_name}.{state} resource: {resource}")

    provenance = manifest.get("provenance_surfaces")
    if not isinstance(provenance, dict) or not provenance:
        fail(f"{VOICE_MANIFEST}: provenance_surfaces must be a non-empty object")
    for kind, resources in provenance.items():
        if not isinstance(kind, str) or not isinstance(resources, list) or not resources:
            fail(f"{VOICE_MANIFEST}: each provenance surface needs a non-empty resource array")
        for resource in resources:
            if not isinstance(resource, str):
                fail(f"{VOICE_MANIFEST}: provenance surface resource must be a string")
            audited.add(resource)
            if not resource_path(resource).is_file():
                fail(f"{VOICE_MANIFEST}: provenance resource does not exist: {resource}")

    if manifest.get("context_bank") != "cozycrazyquests:dialogue/context_voice_banks_v0_2":
        fail(f"{VOICE_MANIFEST}: context_bank must point to v0.2 context voice bank")
    return manifest, audited


def validate_context_bank() -> None:
    if not CONTEXT_BANK.is_file():
        fail(f"missing Conversation Bible v0.2 context bank: {CONTEXT_BANK}")
    bank = load_json(CONTEXT_BANK)
    if not isinstance(bank, dict):
        fail(f"{CONTEXT_BANK}: root must be an object")
    if bank.get("schema_version") != 2:
        fail(f"{CONTEXT_BANK}: schema_version must be 2")
    if bank.get("executable") is not False:
        fail(f"{CONTEXT_BANK}: executable must remain false until selector integration exists")
    dimensions = bank.get("dimensions")
    if not isinstance(dimensions, dict):
        fail(f"{CONTEXT_BANK}: dimensions must be an object")

    region_tier = require_keys(dimensions.get("region_tier"), REGIONS, f"{CONTEXT_BANK}: region_tier")
    for region in REGIONS:
        tiers = require_keys(region_tier[region], TIERS, f"{CONTEXT_BANK}: region_tier.{region}")
        for tier in TIERS:
            lines = tiers[tier]
            if not isinstance(lines, list) or len(lines) < 2 or not all(isinstance(line, str) and line for line in lines):
                fail(f"{CONTEXT_BANK}: region_tier.{region}.{tier} needs at least two non-empty lines")

    trust = require_keys(dimensions.get("trust"), TRUST_STATES, f"{CONTEXT_BANK}: trust")
    for state in TRUST_STATES:
        if not isinstance(trust[state], list) or len(trust[state]) < 2:
            fail(f"{CONTEXT_BANK}: trust.{state} needs at least two lines")

    for section in ("local_fact_open", "completion_acknowledgement"):
        facts = require_keys(dimensions.get(section), FACT_KINDS, f"{CONTEXT_BANK}: {section}")
        for fact in FACT_KINDS:
            if not isinstance(facts[fact], list) or not facts[fact]:
                fail(f"{CONTEXT_BANK}: {section}.{fact} needs at least one line")

    familiarity = require_keys(
        dimensions.get("familiarity"),
        ("FIRST_MEETING", "KNOWN_FACE", "HIGH_TRUST"),
        f"{CONTEXT_BANK}: familiarity",
    )
    for state, lines in familiarity.items():
        if not isinstance(lines, list) or not lines:
            fail(f"{CONTEXT_BANK}: familiarity.{state} needs at least one line")

    referral = require_keys(dimensions.get("referral"), KNOWLEDGE_STATES, f"{CONTEXT_BANK}: referral")
    for state in KNOWLEDGE_STATES:
        if not isinstance(referral[state], list) or not referral[state]:
            fail(f"{CONTEXT_BANK}: referral.{state} needs at least one line")

    provenance = require_keys(dimensions.get("provenance"), PROVENANCE_KINDS, f"{CONTEXT_BANK}: provenance")
    for kind in PROVENANCE_KINDS:
        if not isinstance(provenance[kind], list) or not provenance[kind]:
            fail(f"{CONTEXT_BANK}: provenance.{kind} needs at least one line")

    intents = require_keys(dimensions.get("player_intents"), PLAYER_INTENTS, f"{CONTEXT_BANK}: player_intents")
    for intent in PLAYER_INTENTS:
        if not isinstance(intents[intent], list) or not intents[intent]:
            fail(f"{CONTEXT_BANK}: player_intents.{intent} needs at least one authoring rule")


def validate_known_marking_contract(manifest: dict) -> None:
    families = manifest["knowledge_state_resources"]
    for family_name in ("structure", "underground"):
        known_path = resource_path(families[family_name]["KNOWN"])
        unknown_path = resource_path(families[family_name]["UNKNOWN"])
        rumor_path = resource_path(families[family_name]["RUMOR"])

        known_raw = known_path.read_text(encoding="utf-8")
        if "mark_active_target" not in known_raw:
            fail(f"{known_path}: KNOWN location resource must expose a direct mark_active_target branch")
        for path in (unknown_path, rumor_path):
            raw = path.read_text(encoding="utf-8")
            if "mark_active_target" in raw:
                fail(f"{path}: UNKNOWN/RUMOR must not expose an exact target marker")


def validate_voice_resources(audited_resources: set[str]) -> tuple[int, int]:
    entries: list[tuple[str, Path, str]] = []

    for resource in sorted(audited_resources):
        path = resource_path(resource)
        beats, replies = conversation_text(path)
        if not beats:
            fail(f"{path}: voice-audited resource contains no NPC dialogue beats")
        for beat in beats:
            if word_count(beat) > MAX_VOICE_WORDS:
                fail(
                    f"{path}: v0.2 voice beat has {word_count(beat)} words; split it to <= {MAX_VOICE_WORDS}: "
                    f"{beat!r}"
                )
            validate_no_template_tokens(beat, path)
            entries.append((resource, path, beat))
        for reply in replies:
            if word_count(reply) > MAX_REPLY_WORDS:
                fail(
                    f"{path}: player reply has {word_count(reply)} words; keep typewriter choices to "
                    f"<= {MAX_REPLY_WORDS}: {reply!r}"
                )
            validate_no_template_tokens(reply, path)

    exact: dict[str, list[tuple[str, Path, str]]] = defaultdict(list)
    for entry in entries:
        exact[normalized(entry[2])].append(entry)
    for norm, matches in exact.items():
        if norm and len(matches) > 1:
            sources = ", ".join(str(match[1].name) for match in matches)
            fail(f"exact duplicate NPC beat across voice-audited resources ({sources}): {matches[0][2]!r}")

    long_entries = [entry for entry in entries if len(normalized(entry[2])) >= NEAR_DUP_MIN_CHARS]
    for index, left in enumerate(long_entries):
        left_norm = normalized(left[2])
        for right in long_entries[index + 1:]:
            right_norm = normalized(right[2])
            ratio = SequenceMatcher(None, left_norm, right_norm).ratio()
            if ratio >= NEAR_DUP_RATIO:
                fail(
                    f"near-duplicate NPC beats ({ratio:.3f}) in {left[1].name} and {right[1].name}: "
                    f"{left[2]!r} / {right[2]!r}"
                )

    phrase_lines: dict[tuple[str, ...], set[int]] = defaultdict(set)
    for line_index, (_, _, beat) in enumerate(entries):
        words = token_words(beat)
        seen_in_line: set[tuple[str, ...]] = set()
        for start in range(0, len(words) - PHRASE_WORDS + 1):
            phrase = tuple(words[start:start + PHRASE_WORDS])
            if phrase in seen_in_line:
                continue
            seen_in_line.add(phrase)
            phrase_lines[phrase].add(line_index)

    offenders = [
        (phrase, line_ids)
        for phrase, line_ids in phrase_lines.items()
        if len(line_ids) > MAX_PHRASE_LINES
    ]
    if offenders:
        phrase, line_ids = max(offenders, key=lambda item: len(item[1]))
        examples = ", ".join(entries[index][1].name for index in sorted(line_ids)[:5])
        fail(
            f"six-word phrase is reused in {len(line_ids)} NPC beats ({examples}): "
            f"{' '.join(phrase)!r}; rewrite to preserve speaker voice"
        )

    return len(audited_resources), len(entries)


def main() -> None:
    if not CONVERSATIONS.is_dir():
        fail(f"missing dialogue directory {CONVERSATIONS}")
    present = {path.name for path in CONVERSATIONS.glob("*.json")}
    missing = REQUIRED - present
    if missing:
        fail(f"missing required dialogue files: {sorted(missing)}")

    for path in sorted(CONVERSATIONS.glob("*.json")):
        validate_file(path)

    manifest, audited = validate_voice_manifest()
    validate_context_bank()
    validate_known_marking_contract(manifest)
    audited_count, beat_count = validate_voice_resources(audited)

    print(
        f"Validated {len(present)} CozyCrazyCraft Conversations dialogue files; "
        f"voice-audited {audited_count} v0.2 resources / {beat_count} NPC beats"
    )


if __name__ == "__main__":
    main()
