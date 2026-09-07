#!/usr/bin/env python3
"""Static guardrails for 0.4.1 jobless-village and regional quest routing."""
from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "runtime" / "src" / "main" / "java" / "com" / "natureul" / "cozycrazyquests"
CONV = ROOT / "runtime" / "src" / "main" / "resources" / "data" / "cozycrazyquests" / "conversations"


def read(path: Path) -> str:
    if not path.is_file():
        raise SystemExit(f"ERROR: missing {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8")


def require(src: str, needle: str, label: str) -> None:
    if needle not in src:
        raise SystemExit(f"ERROR: missing civic guard: {label}")


roles = read(JAVA / "VillageCivicRoleService.java")
social = read(JAVA / "VillageSocialConversationManager.java")
fallback = read(JAVA / "CivicQuestFallbackManager.java")
catalog = read(JAVA / "VillageQuestCatalog.java")
context = read(JAVA / "VillageContext.java")
mod = read(JAVA / "CozyCrazyQuests.java")

for role in ("STEWARD", "ROADWARDEN", "QUARTERMASTER", "WATCH_CONTACT"):
    require(roles, role, f"stable role {role}")
require(roles, "Comparator.comparing(Villager::getUUID)", "deterministic civic assignment")
require(roles, "village.key().equals(theirs.key())", "civic roles cannot cross settlement identity")

for forbidden in ("setVillagerData", "setProfession", "setJobSite", "setVillagerXp", "setOffers"):
    if forbidden in roles or forbidden in fallback:
        raise SystemExit(f"ERROR: civic layer mutates vanilla occupation/trade state via {forbidden}")

require(social, "VillageCivicRoleService.roleFor", "civic roles have distinct ambient Conversations")
require(social, "VillageCivicRoleService.isCivicContact", "useful-person routing falls back to civic contacts")
require(social, 'Component.literal("Ask "', "routing action bar is compact waypoint feedback")

require(catalog, ".filter(definition -> !isRegionScoped(definition))", "legacy profession lookup cannot leak regional banks")
for prefix, macro in (("west_", "WEST"), ("east_", "EAST"), ("north_", "NORTH"), ("south_", "SOUTH")):
    require(catalog, f'id.startsWith("{prefix}")', f"regional prefix {prefix} is recognized")
    require(catalog, f'"{macro}".equals(cell.macro())', f"{prefix} content requires {macro} macro")
require(fallback, "regionalForProfession", "profession-specific regional work uses region-aware lookup")
require(fallback, "civicCandidates", "jobless civic contacts can expose village work")
require(fallback, 'pending.putString("giver_civic_role"', "contracts remember civic issuing role")
require(fallback, 'pending.putInt("target_start_chunk_x"', "civic structure offers preserve exact target instance X")
require(fallback, 'pending.putInt("target_start_chunk_z"', "civic structure offers preserve exact target instance Z")
require(mod, "CivicQuestFallbackManager::onEntityInteract", "civic quest layer is registered")

# Named or unnamed settlement identity must prefer a physical civic anchor before any coordinate fallback.
require(context, '"|meeting|"', "unnamed village identity prefers meeting POI")
require(context, '"|board|"', "unnamed village identity can use village board")
if "Math.floorDiv(anchor.getX(), 128)" in context:
    raise SystemExit("ERROR: legacy 128-block unnamed-village collision bucket returned")

for stem in ("civic_steward", "civic_roadwarden", "civic_quartermaster", "civic_watch_contact"):
    path = CONV / f"{stem}.json"
    data = json.loads(read(path))
    beats = [
        option.get("dialogue", "")
        for page in data.get("dialogues", [])
        for option in page.get("dialogue_options", [])
    ]
    if not beats:
        raise SystemExit(f"ERROR: {stem} has no dialogue beat")
    for beat in beats:
        words = len(re.findall(r"\b[\w’'-]+\b", beat))
        if words > 30:
            raise SystemExit(f"ERROR: {stem} civic beat is {words} words; keep civic routing concise")
    raw = path.read_text(encoding="utf-8")
    require(raw, 'ccc_action:\\"route_help\\"', f"{stem} routes the player to useful work")

print("OK: civic-role routing, jobless-village fallback, and macro-scoped regional issuance validated")
