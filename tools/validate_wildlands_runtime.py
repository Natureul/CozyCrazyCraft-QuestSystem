#!/usr/bin/env python3
"""Guardrails for the first executable Wildlands authored-quest slice.

Wildlands work is allowed to be asymmetric. A quest only goes live when its world hook is authoritative:
real local structure for structure jobs, exact audited entity type for Great Hunts, and no placeholder boss
added merely to fill a regional slot.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "runtime" / "src" / "main" / "java" / "com" / "natureul" / "cozycrazyquests"
DIALOGUES = ROOT / "runtime" / "src" / "main" / "resources" / "data" / "cozycrazyquests" / "conversations"


def text(path: Path) -> str:
    if not path.is_file():
        raise SystemExit(f"ERROR: missing Wildlands runtime file: {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8")


def require(haystack: str, needle: str, label: str) -> None:
    if needle not in haystack:
        raise SystemExit(f"ERROR: Wildlands runtime guard missing: {label}")


catalog = text(JAVA / "WildlandsQuestCatalog.java")
facade = text(JAVA / "VillageQuestCatalog.java")
hunts = text(JAVA / "ExactHuntRegistry.java")
manager = text(JAVA / "VillageConversationQuestManager.java")
contract = text(JAVA / "VillageContractItem.java")

expected = {
    "north_w3_sleeping_mountain": "mowziesmobs:frostmaw_spawn",
    "north_w3_last_warm_camp": "dungeons_enhanced:ice_pit",
    "east_w3_temple_eight_roots": "betterjungletemples:jungle_temple",
    "south_w3_sunbird": "mowziesmobs:umvuthana_grove",
}
for quest_id, structure in expected.items():
    require(catalog, f'"{quest_id}"', f"catalog includes {quest_id}")
    require(catalog, f'"{structure}"', f"{quest_id} has an audited structure target")

require(catalog, '"WILDLANDS", 0', "Wildlands targets stay in the issuing radial tier")
require(facade, "WildlandsQuestCatalog.ALL", "Wildlands bank is executable")

require(hunts, '"north_w3_sleeping_mountain"', "Frostmaw hunt has exact-kill policy")
require(hunts, '"mowziesmobs:frostmaw"', "Frostmaw entity ID is exact")
require(hunts, '"south_w3_sunbird"', "Umvuthi hunt has exact-kill policy")
require(hunts, '"mowziesmobs:umvuthi"', "Umvuthi entity ID is exact")
require(manager, "if (exactHunt != null && !exactHunt.matches(event.getEntity().getType())) continue;",
        "unrelated monsters cannot advance Great Hunts")
require(manager, '"defeat " + exactHunt.targetLabel()', "acceptance text names the actual hunt target")
require(contract, '"Defeat " + exactHunt.targetLabel()', "physical contract names the actual hunt target")
require(manager, "definition.structureCandidates().size() > 1", "mixed structure families retry legal candidates")
require(manager, "nearestLegal", "illegal nearest candidate cannot automatically mask every candidate type")

# Signature item IDs are intentionally explicit here so accidental material/name-order regressions are caught.
for item_id in (
    "spartanweaponry:pike_diamond",
    "spartanweaponry:glaive_diamond",
    "spartanweaponry:lance_diamond",
):
    require(catalog, f'"{item_id}"', f"signature reward uses {item_id}")

# These remain blocked by the registry/world-binding audit and must not be smuggled into this executable bank.
for blocked in (
    "west_w3_headless_road",
    "east_d4_thing_beneath_canopy",
    "north_d4_captain_in_ice",
    "west_d4_old_harvest",
):
    if blocked in catalog:
        raise SystemExit(f"ERROR: blocked/unaudited encounter became executable: {blocked}")

stems = (
    "wildlands_sleeping_mountain",
    "wildlands_last_warm_camp",
    "wildlands_temple_eight_roots",
    "wildlands_sunbird",
)
for stem in stems:
    for phase in ("offer", "active", "turnin"):
        path = DIALOGUES / f"{stem}_{phase}.json"
        if not path.is_file():
            raise SystemExit(f"ERROR: missing Wildlands dialogue: {path.relative_to(ROOT)}")

print("OK: Wildlands runtime validated (4 local structure jobs, 2 exact Great Hunts, 12 dialogues)")
