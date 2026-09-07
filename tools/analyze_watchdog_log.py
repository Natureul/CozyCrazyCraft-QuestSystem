#!/usr/bin/env python3
"""Classify Minecraft/ModernFix watchdog incidents from the actual Server thread stack.

Usage:
  python tools/analyze_watchdog_log.py latest.log
  python tools/analyze_watchdog_log.py latest.log --json
  python tools/analyze_watchdog_log.py --self-test

This deliberately does not infer cause from lines merely near the watchdog timestamp.
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import asdict, dataclass
from pathlib import Path

WATCHDOG_RE = re.compile(r"^\[([^\]]+)\].*A single server tick has taken (\d+), more than (\d+) milliseconds")
THREAD_HEADER = '"Server thread"'


@dataclass(frozen=True)
class Incident:
    timestamp: str
    elapsed_ms: int
    threshold_ms: int
    classification: str
    quest_frames: tuple[str, ...]
    evidence_frames: tuple[str, ...]


def _server_thread_block(lines: list[str], watchdog_index: int) -> list[str]:
    header = None
    for i in range(watchdog_index + 1, min(len(lines), watchdog_index + 1600)):
        if lines[i].startswith(THREAD_HEADER):
            header = i
            break
    if header is None:
        return []
    block = [lines[header]]
    for i in range(header + 1, min(len(lines), header + 700)):
        if lines[i].startswith('"'):
            break
        block.append(lines[i])
    return block


def classify_server_stack(block: list[str]) -> tuple[str, tuple[str, ...], tuple[str, ...]]:
    text = "\n".join(block)
    quest_frames = tuple(line.strip() for line in block if "cozycrazyquests" in line.lower())

    if "NearbyStructureResolver.findNearest" in text and (
        "findNearestMapStructure" in text
        or "GridStructurePlacement.findNearest" in text
        or "locateGelPlacement" in text
    ):
        cls = "QUEST_STRUCTURE_LOCATE"
        evidence_terms = (
            "NearbyStructureResolver",
            "findNearestMapStructure",
            "GridStructurePlacement",
            "locateGelPlacement",
            "VillageConversationQuestManager.onEntityInteract",
        )
    elif "VillageConversationQuestManager.onEntityInteract" in text and (
        "StructureCheck" in text or "ChunkGenerator" in text
    ):
        # Later watchdog snapshots can catch a lower frame while the same synchronous target locate
        # is inside structure generation/check code. The interaction frame proves the call owner.
        cls = "QUEST_STRUCTURE_LOCATE"
        evidence_terms = (
            "VillageConversationQuestManager.onEntityInteract",
            "StructureCheck",
            "ChunkGenerator",
        )
    elif "TeleportCommand" in text and "ServerChunkCache.getChunkBlocking" in text:
        cls = "TELEPORT_CHUNK_LOAD"
        evidence_terms = ("TeleportCommand", "ServerChunkCache.getChunkBlocking")
    elif "ServerChunkCache.getChunkBlocking" in text:
        cls = "CHUNK_LOAD_BLOCK"
        evidence_terms = ("ServerChunkCache.getChunkBlocking",)
    elif "NoiseBasedChunkGenerator" in text or "StructureCheck" in text or "ChunkGenerator" in text:
        cls = "WORLDGEN_OR_STRUCTURE_CHECK"
        evidence_terms = ("NoiseBasedChunkGenerator", "StructureCheck", "ChunkGenerator")
    elif block:
        cls = "OTHER_SERVER_THREAD"
        evidence_terms = ()
    else:
        cls = "NO_SERVER_THREAD_STACK"
        evidence_terms = ()

    evidence = tuple(
        line.strip() for line in block
        if any(term in line for term in evidence_terms)
    )
    return cls, quest_frames, evidence[:12]


def parse(text: str) -> list[Incident]:
    lines = text.splitlines()
    incidents: list[Incident] = []
    for i, line in enumerate(lines):
        match = WATCHDOG_RE.match(line)
        if not match:
            continue
        block = _server_thread_block(lines, i)
        classification, quest_frames, evidence = classify_server_stack(block)
        incidents.append(Incident(
            timestamp=match.group(1),
            elapsed_ms=int(match.group(2)),
            threshold_ms=int(match.group(3)),
            classification=classification,
            quest_frames=quest_frames,
            evidence_frames=evidence,
        ))
    return incidents


def summarize(incidents: list[Incident]) -> str:
    if not incidents:
        return "No ModernFix watchdog incidents found."
    counts: dict[str, int] = {}
    max_by_class: dict[str, int] = {}
    for incident in incidents:
        counts[incident.classification] = counts.get(incident.classification, 0) + 1
        max_by_class[incident.classification] = max(
            max_by_class.get(incident.classification, 0), incident.elapsed_ms
        )
    lines = [f"Watchdog reports: {len(incidents)}"]
    for cls in sorted(counts):
        lines.append(f"- {cls}: {counts[cls]} report(s), max elapsed {max_by_class[cls] / 1000:.3f}s")
    lines.append("")
    for incident in incidents:
        evidence = incident.evidence_frames[0] if incident.evidence_frames else "(no decisive frame captured)"
        lines.append(
            f"{incident.timestamp}: {incident.elapsed_ms / 1000:.3f}s -> "
            f"{incident.classification} | {evidence}"
        )
    return "\n".join(lines)


def self_test() -> int:
    quest = '''[11:24:10] [ModernFix integrated server watchdog/ERROR]: A single server tick has taken 40029, more than 40000 milliseconds
"Server thread" prio=4 Id=133 RUNNABLE
    at TRANSFORMER/structure_gel@2.16.2/com.legacy.structure_gel.api.structure.GridStructurePlacement.findNearest(GridStructurePlacement.java:316)
    at TRANSFORMER/minecraft@1.20.1/net.minecraft.world.level.chunk.ChunkGenerator.handler$bdo000$locateGelPlacement(ChunkGenerator.java:5675)
    at TRANSFORMER/cozycrazyquests@0.4.0/com.natureul.cozycrazyquests.NearbyStructureResolver.findNearest(NearbyStructureResolver.java:48)
    at TRANSFORMER/cozycrazyquests@0.4.0/com.natureul.cozycrazyquests.VillageConversationQuestManager.onEntityInteract(VillageConversationQuestManager.java:80)
"Render thread" prio=4 Id=1 RUNNABLE
'''
    teleport = '''[11:55:20] [ModernFix integrated server watchdog/ERROR]: A single server tick has taken 40001, more than 40000 milliseconds
"Server thread" prio=4 Id=133 TIMED_WAITING
    at TRANSFORMER/minecraft@1.20.1/net.minecraft.server.level.ServerChunkCache.getChunkBlocking(ServerChunkCache.java:3268)
    at TRANSFORMER/minecraft@1.20.1/net.minecraft.server.commands.TeleportCommand.m_139014_(TeleportCommand.java:144)
"Render thread" prio=4 Id=1 RUNNABLE
'''
    no_stack = '''[12:00:00] [ModernFix integrated server watchdog/ERROR]: A single server tick has taken 40002, more than 40000 milliseconds
[12:00:01] [Render thread/INFO]: still alive
'''
    checks = [
        (quest, "QUEST_STRUCTURE_LOCATE"),
        (teleport, "TELEPORT_CHUNK_LOAD"),
        (no_stack, "NO_SERVER_THREAD_STACK"),
    ]
    for fixture, expected in checks:
        incidents = parse(fixture)
        if len(incidents) != 1 or incidents[0].classification != expected:
            print(f"SELF-TEST FAILED: expected {expected}, got {incidents}")
            return 1
    print("OK: watchdog classifier self-test passed")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("log", nargs="?")
    parser.add_argument("--json", action="store_true", dest="as_json")
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()

    if args.self_test:
        return self_test()
    if not args.log:
        parser.error("log path is required unless --self-test is used")

    path = Path(args.log)
    incidents = parse(path.read_text(encoding="utf-8", errors="replace"))
    if args.as_json:
        print(json.dumps([asdict(i) for i in incidents], indent=2))
    else:
        print(summarize(incidents))
    return 0


if __name__ == "__main__":
    sys.exit(main())
