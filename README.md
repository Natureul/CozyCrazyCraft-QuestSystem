# CozyCrazyCraft Quest System

Player-facing quest, bounty, map, cartographer, reward, and information-layer design for the CozyCrazyCraft Forge 1.20.1 modpack.

## Start here

### [📜 Browse the 108 authored quests by region and tier](QUESTS.md)
### [🧭 Browse the 16 regional repeatable field jobs](FIELD_JOBS.md)
### [🎁 Browse the regional reward ladders](REWARDS.md)
### [🗺️ See the first working map-system slice](docs/MAP_SYSTEM_VERTICAL_SLICE.md)
### [📐 See how quest targets are kept sane/local](docs/TARGETING_AND_DISTANCE_POLICY.md)
### [🧑‍🌾 See the cartographer/local-knowledge system](docs/CARTOGRAPHER_SYSTEM.md)

Current designed content: **124 quest concepts** — 108 authored/story-capable notices plus 16 additional regional field jobs. The two layers are intentionally separate: named contracts can carry structure/story significance, while field jobs keep ordinary boards locally flavored between major discoveries.

For implementation detail, see [the mechanical quest matrix](design/REGIONAL_QUEST_MATRIX.md) and the machine-readable catalogs under [`data/quest_catalog/`](data/quest_catalog/).

## Status

The project is intentionally split into two layers:

1. **Safe content/data work** — Bountiful 6.0.4 configuration, proven objective/reward patterns, quest catalogs, story-board metadata, proof-item specifications, reward balancing, registry audits, target-selection policy, cartographer prototypes, and test plans.
2. **World/runtime integration work** — regional board assignment, useful-structure locating, same-board redemption, proof placement, and map/story state.

The world side is no longer hypothetical: **CozyCrazyZones 0.3.6 is the current geography contract.** The quest project consumes its region/radial classification rather than duplicating that logic. Version 0.3.6 also establishes a real starter information layer: it reserves the first village roughly 1,000–1,650 blocks from spawn, prepares the starter desk map, grants/links the starter Atlas, and paints a route to that first village.

The core rule remains: **do not invent a complicated event system when Bountiful, Supplementaries, Map Atlases, Moonlight, or vanilla criteria can already do the job reliably.** Anything not proven reliable is marked for world integration, registry verification, reward audit, or deferral rather than silently treated as implemented.

The target-selection rule is equally important: **if there is no sensible real target, do not issue the world-target quest.** A board should fall back to ordinary local work rather than send the player absurdly far away or into the wrong cardinal region.

## Current baseline

- Target: Minecraft Forge 1.20.1
- Bountiful: `6.0.4+1.20.1-forge`
- Geography contract: CozyCrazyZones `0.3.6`
- Shared/core ecology radius: 0–700 blocks by default
- Cardinal transition: ~700–1,200 blocks by default
- First-village reservation: ~1,000–1,650 blocks, preferring ~1,050–1,250
- Radial tiers: Hearthlands 0–2,500 / Frontier 2,500–5,500 / Wildlands 5,500–9,000 / Dread Reaches 9,000+
- Default Bountiful bounty pools/decrees: excluded in the deployment baseline
- Only CozyCrazyCraft custom pool/decree content should load after the baseline is installed
- Initial smoke-test decree: `Local Notices`
- Current design count: **124** (108 authored + 16 field jobs)
- Four regional field-job playtest decrees are materialized in actual Bountiful JSON
- Four deterministic structure-map playtest decrees use Bountiful command rewards → Supplementaries structure maps
- A separate, non-shipping cartographer datapack prototype tests Moonlight/Supplementaries local structure-map trades
- Machine-readable travel scopes now distinguish Local Site, Regional Expedition, Outward Lead, and Legendary Destination
- Current CI validates Bountiful data, authored catalog coverage/schema, repeatable expansion, readable browsers, regional board policy, target-distance policy, cartographer prototype data, story metadata, and zoning/world bindings

## Useful documents

- [`QUESTS.md`](QUESTS.md) — easy human-readable authored-quest browser
- [`FIELD_JOBS.md`](FIELD_JOBS.md) — easy browser for repeatable regional ecology/community work
- [`REWARDS.md`](REWARDS.md) — easy regional/tier reward browser
- [`STRUCTURES.md`](STRUCTURES.md) — quest-relevant structure browser
- [`docs/TARGETING_AND_DISTANCE_POLICY.md`](docs/TARGETING_AND_DISTANCE_POLICY.md) — how local/expedition/outward targets are selected without absurd distances
- [`docs/CARTOGRAPHER_SYSTEM.md`](docs/CARTOGRAPHER_SYSTEM.md) — cartographer roles, map progression, regional map inventory, and runtime upgrade path
- [`docs/MAP_SYSTEM_VERTICAL_SLICE.md`](docs/MAP_SYSTEM_VERTICAL_SLICE.md) — starter map + quest/cartographer map implementation path
- [`docs/FIRST_VILLAGE_VERTICAL_SLICE.md`](docs/FIRST_VILLAGE_VERTICAL_SLICE.md) — first playable starter→village→board→local-adventure target
- [`docs/REWARD_SOURCE_AUDIT.md`](docs/REWARD_SOURCE_AUDIT.md) — source-checked reward IDs/mechanics and compatibility warnings
- [`design/REGIONAL_QUEST_MATRIX.md`](design/REGIONAL_QUEST_MATRIX.md) — all authored quests + mechanical objective/status
- [`design/STORY_BOARD_UI.md`](design/STORY_BOARD_UI.md) — story-first Bountiful UI plan
- [`design/story_board_mockup.html`](design/story_board_mockup.html) — visual mockup
- [`design/REWARD_VALIDATION_BACKLOG.md`](design/REWARD_VALIDATION_BACKLOG.md) — broader modded reward audit queue
- [`data/targeting_policy.json`](data/targeting_policy.json) — machine-readable target-distance/selection contract
- [`prototype/cartographer_datapack/`](prototype/cartographer_datapack/) — isolated static cartographer trade experiment; not included in normal playtest overlay
- [`docs/BOUNTIFUL_6_0_4_RELIABILITY.md`](docs/BOUNTIFUL_6_0_4_RELIABILITY.md)
- [`docs/QUEST_FRAMEWORK.md`](docs/QUEST_FRAMEWORK.md)
- [`docs/INTEGRATION_BOUNDARY.md`](docs/INTEGRATION_BOUNDARY.md)
- [`docs/TEST_PLAN.md`](docs/TEST_PLAN.md)
- [`deployment/config/bountiful/`](deployment/config/bountiful/) — custom-only Bountiful baseline

## Geographic model

Every board/location ultimately lives in two classifications:

- radial tier: Hearthlands / Frontier / Wildlands / Dread Reaches
- macro-region: North / East / South / West (plus shared core / transition handling)

Current macro-region display names are:

- North — **Frostmarch**
- East — **Greenveil**
- South — **Sunscar**
- West — **Harvestwood**

So a board can be in, for example, **Frostmarch Frontier** or **Harvestwood Wildlands**, and its work/reward pool can reflect both.

The zoning project owns the truth of **where things can exist**.

This quest project owns **what local people know about them, why the player might care, how far a request is reasonably allowed to send them, and what they receive for engaging with it**.
