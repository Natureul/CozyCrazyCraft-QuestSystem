#!/usr/bin/env python3
"""Validate the isolated Moonlight/Supplementaries cartographer-map prototype."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT / "prototype" / "cartographer_datapack"
TRADES = PACK / "data" / "minecraft" / "moonlight" / "villager_trade" / "cartographer"
CONTRACT = ROOT / "data" / "world_bindings" / "cozy_zones_0_3_6.json"
REGIONAL_ADDITIONS = ROOT / "data" / "world_bindings" / "cozy_zones_0_3_6_regional_additions.json"

errors: list[str] = []
warnings: list[str] = []


def load(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"{path.relative_to(ROOT)}: invalid JSON: {exc}")
        return None


def collect_structure_rules(*documents) -> dict[str, dict]:
    rules: dict[str, dict] = {}
    for document in documents:
        if not isinstance(document, dict):
            continue
        for rule in document.get("structure_rules", []):
            if not isinstance(rule, dict) or not isinstance(rule.get("id"), str):
                continue
            rid = rule["id"]
            prior = rules.get(rid)
            if prior is not None and prior != rule:
                errors.append(f"Conflicting checked-in 0.3.6 structure rule for {rid}")
            rules[rid] = rule
    return rules


def main() -> int:
    meta = load(PACK / "pack.mcmeta")
    contract = load(CONTRACT)
    additions = load(REGIONAL_ADDITIONS)
    if isinstance(meta, dict):
        if (meta.get("pack") or {}).get("pack_format") != 15:
            errors.append("cartographer prototype pack.mcmeta must use 1.20.1 pack_format 15")

    for document, label in ((contract, "compact contract"), (additions, "regional additions")):
        if isinstance(document, dict) and (document.get("source") or {}).get("version") != "0.3.6":
            errors.append(f"{label} must identify CozyCrazyZones 0.3.6")

    # The namespace is not cosmetic. Moonlight derives the profession resource
    # id from the directory path; vanilla cartographer requires minecraft:cartographer.
    wrong_namespace_root = PACK / "data" / "cozycrazyquests" / "moonlight" / "villager_trade" / "cartographer"
    if wrong_namespace_root.exists():
        errors.append("Cartographer trade files must be under data/minecraft/... so Moonlight resolves minecraft:cartographer")

    if not TRADES.is_dir():
        errors.append("Missing data/minecraft/moonlight/villager_trade/cartographer prototype directory")
        return finish()

    explicit_rules = collect_structure_rules(contract, additions)

    files = sorted(TRADES.glob("*.json"))
    if not files:
        errors.append("No cartographer prototype trades found")

    for path in files:
        data = load(path)
        if not isinstance(data, dict):
            continue
        label = str(path.relative_to(ROOT))

        if data.get("type") != "supplementaries:structure_map":
            errors.append(f"{label}: type must be supplementaries:structure_map")
        if data.get("item") != "minecraft:emerald":
            warnings.append(f"{label}: unusual primary map currency {data.get('item')!r}")

        target = data.get("structure")
        if not isinstance(target, str) or not target:
            errors.append(f"{label}: missing structure target")
        elif target not in explicit_rules:
            errors.append(
                f"{label}: target {target} is not present in the checked-in audited CozyCrazyZones 0.3.6 structure rules"
            )
        else:
            rule = explicit_rules[target]
            macros = rule.get("macro_regions") or []
            if not macros:
                warnings.append(f"{label}: target {target} is not macro-region restricted; static trade may leak across regions")
            if rule.get("minimum_influence") not in {"CARDINAL_TRANSITION", "ESTABLISHED"}:
                warnings.append(
                    f"{label}: target {target} is not regionally established/transition-gated; prefer runtime selection for strict locality"
                )

        lo = data.get("price_min")
        hi = data.get("price_max")
        if not isinstance(lo, int) or not isinstance(hi, int) or lo < 1 or hi < lo:
            errors.append(f"{label}: invalid price_min/price_max")
        elif hi > 24:
            warnings.append(f"{label}: map price is above the initial design ceiling ({hi})")

        level = data.get("level")
        if not isinstance(level, int) or not 1 <= level <= 5:
            errors.append(f"{label}: villager level must be 1..5")

        max_trades = data.get("max_trades")
        if not isinstance(max_trades, int) or max_trades < 1:
            errors.append(f"{label}: max_trades must be positive")

        # Until the client-side resource layer is packaged, a custom map_name
        # would render as a raw translation key if we forget its lang entry.
        if "map_name" in data:
            warnings.append(f"{label}: custom map_name requires matching client lang resources")

    return finish()


def finish() -> int:
    for warning in warnings:
        print(f"WARNING: {warning}")
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        print(f"\nFAILED: {len(errors)} error(s), {len(warnings)} warning(s)")
        return 1
    print(f"OK: cartographer prototype validation passed ({len(warnings)} warning(s))")
    return 0


if __name__ == "__main__":
    sys.exit(main())
