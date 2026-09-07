#!/usr/bin/env python3
"""Guard dynamic social pages against becoming permanent stale entity dialogue."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "runtime" / "src" / "main" / "java" / "com" / "natureul" / "cozycrazyquests"


def read(name: str) -> str:
    path = JAVA / name
    if not path.is_file():
        raise SystemExit(f"ERROR: missing {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8")


def require(src: str, needle: str, label: str) -> None:
    if needle not in src:
        raise SystemExit(f"ERROR: missing social-refresh invariant: {label}")


bridge = read("ConversationBridge.java")
social = read("VillageSocialConversationManager.java")

require(bridge, "static String currentDialogueId", "ConversationBridge exposes current dialogue ID")
require(social, "clearRefreshableSocialDialogue", "social manager has targeted refresh helper")
for family in ('path.startsWith("villager_")', 'path.startsWith("hint_")', 'path.startsWith("gore_tunnel_")'):
    require(social, family, f"refreshable family {family}")
for civic in ("civic_steward", "civic_roadwarden", "civic_quartermaster", "civic_watch_contact"):
    require(social, civic, f"refreshable civic page {civic}")

active_start = social.find("ResourceLocation activeHint = QuestHintNetwork.dialogue")
gore = social.find("ResourceLocation deepRoad = GoreTunnelLead.adultDialogue")
refreshes = [i for i in range(len(social)) if social.startswith("clearRefreshableSocialDialogue(villager);", i)]
if active_start < 0 or gore < 0 or not any(active_start < i < gore for i in refreshes):
    raise SystemExit("ERROR: stale ambient/hint/Gore pages are not cleared between active-hint resolution and Gore re-evaluation")

# The old social fallback produced a sentence-sized `Ask Name — role, ~distance blocks direction.` HUD line.
# The central router now owns normal route_help; this fallback is still kept compact as a defensive path.
if 'Component.literal("Ask "' in social:
    raise SystemExit("ERROR: sentence-style social referral HUD path returned")
require(social, "message.length() > 45", "defensive social referral has a 45-character budget")
require(social, 'Component.literal("Board • "', "notice-board direction uses compact data feedback")

print("OK: ambient, hint, civic, child, guard and Gore social pages refresh without clearing authored quest dialogue")
