#!/usr/bin/env python3
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
POLICY = ROOT / "data" / "structure_recovery_contracts.json"
POOLS = ROOT / "deployment" / "config" / "bountiful" / "bounty_pools"
RUNTIME = ROOT / "runtime" / "src" / "main"


def load(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def fail(message: str) -> None:
    raise SystemExit(f"ERROR: {message}")


def main() -> None:
    data = load(POLICY)
    contracts = data.get("contracts", [])
    if len(contracts) < 4:
        fail("expected at least four recovery contracts")

    proof_source = (RUNTIME / "java/com/natureul/cozycrazyquests/ProofLootInjector.java").read_text(encoding="utf-8")
    item_source = (RUNTIME / "java/com/natureul/cozycrazyquests/ModItems.java").read_text(encoding="utf-8")
    lang_path = RUNTIME / "resources/assets/cozycrazyquests/lang/en_us.json"
    lang = load(lang_path)

    seen_items: set[str] = set()
    seen_objectives: set[str] = set()

    for contract in contracts:
        cid = contract["id"]
        proof = contract["proof_item"]
        loot = contract["loot_table"]
        pool_id = contract["objective_pool"]
        objective_id = contract["objective_id"]
        min_rep = contract["minimum_village_trust_rep"]

        if proof in seen_items:
            fail(f"{cid}: proof item reused: {proof}")
        if objective_id in seen_objectives:
            fail(f"{cid}: objective id reused: {objective_id}")
        seen_items.add(proof)
        seen_objectives.add(objective_id)

        namespace, item_path = proof.split(":", 1)
        if namespace != "cozycrazyquests":
            fail(f"{cid}: proof item must be CozyCrazyQuests-owned")

        pool_path = POOLS / f"{pool_id}.json"
        if not pool_path.is_file():
            fail(f"{cid}: objective pool missing: {pool_path}")
        entries = load(pool_path).get("content", {})
        if objective_id not in entries:
            fail(f"{cid}: objective {objective_id} missing from {pool_id}")
        entry = entries[objective_id]
        if entry.get("type") != "item":
            fail(f"{cid}: recovery objective must use Bountiful item type")
        if entry.get("content") != proof:
            fail(f"{cid}: objective content does not match proof item")
        amount = entry.get("amount", {})
        if amount.get("min") != 1 or amount.get("max") != 1:
            fail(f"{cid}: recovery proof amount must be exactly one")
        if float(entry.get("repRequired", 0)) < float(min_rep):
            fail(f"{cid}: objective unlocks before its documented Village Trust requirement")

        loot_namespace, loot_path = loot.split(":", 1)
        if f'"{loot_namespace}"' not in proof_source or f'"{loot_path}"' not in proof_source:
            fail(f"{cid}: loot table is not wired in ProofLootInjector")
        if f'proof("{item_path}")' not in item_source:
            fail(f"{cid}: proof item is not registered in ModItems")

        model = RUNTIME / f"resources/assets/cozycrazyquests/models/item/{item_path}.json"
        if not model.is_file():
            fail(f"{cid}: missing item model {model}")
        if f"item.cozycrazyquests.{item_path}" not in lang:
            fail(f"{cid}: missing display-name translation")
        if f"item.cozycrazyquests.{item_path}.desc" not in lang:
            fail(f"{cid}: missing tooltip translation")

    print(f"OK: structure recovery validation passed ({len(contracts)} contracts)")


if __name__ == "__main__":
    main()
