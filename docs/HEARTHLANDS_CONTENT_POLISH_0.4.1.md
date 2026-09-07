# Hearthlands Content Polish — 0.4.1

Branch: `hearthlands-content-polish-0.4.1`  
Base: `2e413cba282af6a11d5372b177d6c51d9df4503b`

## Scope

This pass changes executable Hearthlands content only. It deliberately does **not** modify `VillageConversationQuestManager`, `QuestHintNetwork`, `NamedPlaceBridge`, `StructureSurveyCompletionBridge`, `RecoveryQuestRuntime`, or `GoreTunnelLead`.

The pass follows the Conversation Bible short-beat/local-knowledge rules, the Quest Bible anti-boredom rules, the Progression Bible's Zone-1 community/exploration/profession lattice, and the Structure Bible's current structure-tier/inhabitant audit.

## Core content decision

The old Hearthlands bank repeatedly reused the same structure families. Stables supported `The Empty Stalls`, `Bring Back the Mail`, `Who Owned the Empty Stalls`, plus a First Real Map candidate. Ice Pit and Jungle Monument each had three Hearthlands definitions even though the current structure authority places Ice Pit in Wildlands and Jungle Monument in Frontier. Desert Tomb and Witch Tower were also reused across survey/combat/recovery work.

This pass removes those authored collisions. Every fixed structure target in the executable Hearthlands + Hearthlands recovery banks now has its own content role. `The First Real Map` uses a separate ambient-landmark candidate bank.

## Amber Rest / Empty Stalls fix

`The Empty Stalls` remains the meaningful first stables visit. It asks for condition, stall/tack/feed marks, and whether the site can be reclaimed. The reward is a `Stableyard Spade` plus food — not a saddle.

`The Paper Trail from the Stalls` replaces the old same-instance stable-ledger repetition. The feed stamp discovered at the stables points to `dungeons_enhanced:hay_storage`. The player recovers the `Weathered Stable Ledger` from the old hay store instead of immediately returning to the same stables. Active dialogue explicitly says: **"Do not go back through the stables. We are following the paper trail now."**

Its reward is a `Stablekeeper's Hoe` plus `Route Keeper's Tag`, making the continuation informational/agricultural rather than another horse-gear payout.

## Quest rewrites

### Community

| Quest | New role | Target | Reward identity |
|---|---|---|---|
| The Empty Stalls | reclaimability + route-mark survey | `dungeons_enhanced:stables` | Stableyard Spade + golden carrots |
| The Outer Pasture | short local pasture protection | local area | Pasture Shears + food |
| Trouble at the Waterline | short water-edge community protection | local water edge | Waterline Rod + cooked fish |

### Exploration / investigation

| Quest | New role | Target | Reward identity |
|---|---|---|---|
| The Cellar Under the Hill | ordinary early dungeon survey | `dungeons_enhanced:dungeon_variant` | Feather Falling boots + torches |
| Stone in the Green | small landmark / masonry survey | `dungeons_enhanced:druid_circle` | Brush-Cutter + scaffolding |
| Stone Under the Sun | first local tomb survey | `dungeons_enhanced:desert_tomb` | Cold Sweat waterskin + compass |
| Smoke Above the Old Road | factual witch-tower survey | `dungeons_enhanced:witch_tower` | Old-Stone Pick + lanterns |
| Lanterns Below | compact chamber clear | `valhelsia_structures:spawner_room` | shield + lanterns |
| What the River Kept | submerged-site survey | `dungeons_enhanced:sunken_shrine` | Depth Strider boots + paper |
| Old Walls, Old Names | local ruin documentation | `dungeons_enhanced:ruined_building` | Surveyor's Pick + bricks |
| The First Real Map | actual cartography of ambient landmarks | dedicated candidate bank | Survey Glass + paper |

`The First Real Map` uses `dungeons_enhanced:mushroom_house`, `valhelsia_structures:big_tree`, `valhelsia_structures:witch_hut`, `born_in_chaos_v1:observation_tower_forest`, and `born_in_chaos_v1:observation_tower_plains`. None is used by another Hearthlands authored quest in this pass.

### Profession work

| Quest | New role | Target | Reward identity |
|---|---|---|---|
| Hold the Watchline | watch-tower combat commission | `dungeons_enhanced:watch_tower` | Spartan iron pike |
| Keep the Lane Open | local road work | local wooded lane | Spartan iron quarterstaff |
| Test the Long Reach | local weapon commission | local exposed road | Spartan iron spear |
| Clear the Timber Track | local road/work-crew problem | local timber track | Spartan iron battleaxe |
| Field-Test the Mail | equipment field test instead of another stables trip | local rough road | chainmail issued on accept + iron |
| Silk Across the Road | spider source clear | `betterdungeons:spider_dungeon` | Quick Charge crossbow |
| The Road Through the Ruin | road reopening through a distinct ruin | `valhelsia_structures:tower_ruin` | Roadwright's Spade + iron |
| A Patrol Trial | local weapon test | local patrol road | trial blade issued on accept |
| The Cart That Didn't Return | local route protection | local cart trail | Roadside Hatchet + leather |

### Recovery work

| Quest | New role | Target | Reward identity |
|---|---|---|---|
| The Letter in the Rafters | recover old survey correspondence from an empty shelter | `dungeons_enhanced:miners_house` | Cold Sweat thermometer + torches |
| Plans Left Behind | recover field notes without inventing an on-site resident | `valhelsia_structures:player_house` | Foundation Trowel + paper |
| The Mark Beside the Sand | recover an existing rubbing from a small desert ruin | `towns_and_towers:mimic_desert` | Spartan boomerang + brush |
| The Paper Trail from the Stalls | intentional Empty Stalls continuation at a different site | `dungeons_enhanced:hay_storage` | Stablekeeper's Hoe + name tag |

## Tier / suitability corrections

Removed from the Hearthlands executable target pool:

- `dungeons_enhanced:ice_pit` — current Structure Bible places it in Wildlands.
- `dungeons_enhanced:jungle_monument` — current Structure Bible places it in Frontier and treats its one caged jungle villager as a rescue/progression node, not a generic early ruin.

No registry IDs were invented. New structure targets came from the supplied runtime registry/Structure Bible material. New non-vanilla reward IDs were limited to IDs already present in executable project content (`cold_sweat:waterskin`, `cold_sweat:thermometer`, and existing Spartan Weaponry IDs).

## Reward pass

Horse/saddle rewards are removed from both stables jobs. Spartan Weaponry remains the main early combat-variety layer: pike, quarterstaff, spear, battleaxe, boomerang. Cold Sweat is introduced softly through a waterskin and thermometer. Profession rewards now match actual work: shears for pasture work, Depth Strider boots for submerged survey, a shield for a tight chamber, chainmail for an armorer field test, and road tools for road work.

Equipment rewards remain compatible with Quality Equipment; this pass does not hardcode configurable quality names. No unverified Camera Mod, Farmer's Delight, pet-system, or Majrusz registry IDs were added merely to satisfy a checklist.

## Same-location chains after this pass

There are **no authored fixed-structure same-location chains** in the Hearthlands content bank.

One intentional story continuation remains:

1. `The Empty Stalls` -> `dungeons_enhanced:stables`
2. `The Paper Trail from the Stalls` -> `dungeons_enhanced:hay_storage`

The first survey discovers the supplier/feed-mark clue; the second follows records to a different agricultural structure and pays a different reward family.

## INTEGRATION REQUIREMENTS

1. **Recent exact-instance suppression.** Track the last ~3 authored structure target-instance keys per village/player and strongly suppress immediate reuse unless a quest explicitly declares `continuation=true`.
2. **Reward-archetype memory.** Track at least the last 2 primary reward families and suppress back-to-back repeats.
3. **Explicit regional eligibility for local jobs.** The current definition schema has tier and same-macro targeting but no authored issuer-macro field. Add one before making local jobs strongly Frostmarch/Greenveil/Sunscar/Harvestwood-specific.
4. **Noncombat local objective verbs.** Add content-safe runtime support for observe/photograph/inspect/repair/deliver/escort/ecology objectives. The current local objective type is hostile-clear only; adding more local quests now would mostly add more kill counters.
5. **Optional chain prerequisite.** If `The Paper Trail from the Stalls` should be guaranteed only after `The Empty Stalls`, expose a content-level prerequisite flag. The rewrite is safe without it because it targets a different site and does not rely on a false player-history claim.
6. **Verified registry surface for reward authors.** Expose or generate an item/enchantment registry dump from the installed pack so content can safely add Camera Mod, Farmer's Delight, Domestication Innovation, and Majrusz rewards without guessing IDs.

## Validation expectations

Run the repository's full workflow: all Python validators, Conversations validation, recovery semantics, Java 17 Gradle runtime build, and integrated playtest overlay build.
