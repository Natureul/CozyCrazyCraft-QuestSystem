#!/usr/bin/env python3
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BO = ROOT / "deployment" / "config" / "bountiful"
STORIES = ROOT / "runtime" / "src" / "main" / "resources" / "assets" / "cozycrazyquests" / "story" / "live_notice_stories.json"


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def main():
    errors = []
    config = load(BO / "bountiful.json")
    if config.get("maxNumRewards") != 1:
        errors.append("Ordinary CozyCrazyCraft notices must use exactly one Bountiful reward.")

    hearth = {
        "north": "ccc_hearth_north_objs",
        "east": "ccc_hearth_east_objs",
        "south": "ccc_hearth_south_objs",
        "west": "ccc_hearth_west_objs",
    }
    live_ids = set(load(BO / "bounty_pools" / "ccc_local_objs.json")["content"])

    for region, pool in hearth.items():
        decree = load(BO / "bounty_decrees" / f"ccc_hearth_{region}.json")
        if decree.get("rewards") != ["ccc_local_rews"]:
            errors.append(f"{region} Hearthlands decree must use only generic payment rewards; signature rewards need deterministic contracts.")
        data = load(BO / "bounty_pools" / f"{pool}.json")["content"]
        live_ids.update(data)
        for qid, entry in data.items():
            amount = entry.get("amount", {})
            if "recover_" in qid:
                if amount != {"min": 1, "max": 1}:
                    errors.append(f"Recovery contract {qid} must request exactly one proof item.")
            elif entry.get("type") == "entity":
                if amount.get("min", 0) < 2:
                    errors.append(f"Hunt notice {qid} is too trivial; min kill count must be >= 2.")
            else:
                if amount.get("min", 0) < 8:
                    errors.append(f"Supply notice {qid} is too trivial; min item count must be >= 8.")

    local = load(BO / "bounty_pools" / "ccc_local_objs.json")["content"]
    for qid, entry in local.items():
        amount = entry.get("amount", {})
        if entry.get("type") == "entity" and amount.get("min", 0) < 3:
            errors.append(f"Local hunt {qid} must request at least 3 kills.")
        if entry.get("type") != "entity" and amount.get("min", 0) < 8:
            errors.append(f"Local supply notice {qid} must request at least 8 items.")

    if "ccc_south_h1_flowers_savanna" in load(BO / "bounty_pools" / "ccc_hearth_south_objs.json")["content"]:
        errors.append("Flowers of the Savanna must not be in the random Sunscar pool; its Acacia Blossom reward is signature/deterministic.")

    stories = load(STORIES)
    missing = sorted(live_ids - set(stories))
    if missing:
        errors.append("Live notice objectives missing story cards: " + ", ".join(missing))

    if errors:
        print("Notice coherence validation FAILED:")
        for error in errors:
            print(" -", error)
        raise SystemExit(1)

    print(f"Notice coherence validation passed: {len(live_ids)} live objective cards, one objective/payment policy enforced.")


if __name__ == "__main__":
    main()
