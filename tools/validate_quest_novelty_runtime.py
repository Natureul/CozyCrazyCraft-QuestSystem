#!/usr/bin/env python3
"""Guard authored quest target/reward recency and the Amber Rest regression fix."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "runtime" / "src" / "main" / "java" / "com" / "natureul" / "cozycrazyquests"

errors: list[str] = []


def read(name: str) -> str:
    path = JAVA / name
    try:
        return path.read_text(encoding="utf-8")
    except Exception as exc:
        errors.append(f"{name}: cannot read: {exc}")
        return ""


def require(text: str, token: str, label: str) -> None:
    if token not in text:
        errors.append(f"{label}: missing {token!r}")


def forbid(text: str, token: str, label: str) -> None:
    if token in text:
        errors.append(f"{label}: forbidden regression token {token!r}")


def main() -> int:
    state = read("VillageQuestState.java")
    policy = read("QuestNoveltyPolicy.java")
    manager = read("VillageConversationQuestManager.java")
    civic = read("CivicQuestFallbackManager.java")
    recovery = read("HearthlandsRecoveryQuestCatalog.java")

    require(state, 'RECENT_COMPLETIONS = "recent_completion_by_village"', "recent completion state")
    require(state, "static CompoundTag recentCompletion", "recent completion state")
    require(state, "static void noteRecentCompletion", "recent completion state")
    require(state, 'entry.putString("target_key"', "recent completion target identity")
    require(state, 'entry.putString("reward_family"', "recent completion reward identity")

    require(policy, "SAME_REWARD_FAMILY_PENALTY = 10", "reward repetition penalty")
    require(policy, "SAME_TARGET_INSTANCE_PENALTY = 100", "same-instance strong penalty")
    require(policy, "primaryRewardFamily", "reward-family classifier")
    require(policy, "targetKey.equals(previousTarget)", "exact recent target comparison")
    require(policy, "family.equals(previousFamily)", "recent reward-family comparison")

    require(manager, "QuestNoveltyPolicy.penalty(root, village.key(), definition, target.targetKey())",
            "profession-first offer ranking")
    require(manager, "fallbackPenalty = Integer.MAX_VALUE", "profession-first fallback ranking")
    require(manager, "VillageQuestState.noteRecentCompletion(", "turn-in records local history")
    require(manager, "QuestNoveltyPolicy.primaryRewardFamily(definition)", "turn-in records reward family")

    require(civic, "QuestNoveltyPolicy.penalty(root, village.key(), definition, target.targetKey())",
            "civic/regional offer ranking")
    require(civic, "betterOffer(regional, civicOffer)", "civic role compares available novelty")

    # The exact 0.4.0 failure must remain authored out even if generic ranking falls back in a sparse world.
    stable_start = recovery.find('"hearthlands_recovery_stable_ledger"')
    stable_end = recovery.find("static final List<Definition> ALL", stable_start)
    stable = recovery[stable_start:stable_end] if stable_start >= 0 and stable_end > stable_start else ""
    if not stable:
        errors.append("stable recovery definition could not be isolated")
    else:
        require(stable, 'structures("dungeons_enhanced:hay_storage")', "stable paper-trail continuation uses a new site")
        forbid(stable, 'structures("dungeons_enhanced:stables")', "stable recovery must not immediately reuse the stables")
        forbid(stable, 'reward("minecraft:saddle"', "stable recovery must not repeat the saddle reward")

    if errors:
        for error in errors:
            print("ERROR:", error)
        print(f"FAILED: {len(errors)} authored novelty regression(s)")
        return 1
    print("OK: recent target/reward ranking and Amber Rest anti-repeat invariants validated")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
