# CozyCrazyCraft Quest System

Player-facing quest, bounty, map, cartographer, reward, and information-layer design for the CozyCrazyCraft Forge 1.20.1 modpack.

## Start here

### [📜 Browse all 108 current authored quests by region and tier](QUESTS.md)
### [🎁 Browse the regional reward ladders](REWARDS.md)
### [🗺️ See the first working map-system slice](docs/MAP_SYSTEM_VERTICAL_SLICE.md)

Those are the human-readable overviews. `QUESTS.md` shows Shared Core, Frostmarch, Greenveil, Sunscar, and Harvestwood across Hearthlands → Frontier → Wildlands → Dread Reaches. `REWARDS.md` shows what each region is intended to teach/give the player at each tier.

For implementation detail, see [the mechanical quest matrix](design/REGIONAL_QUEST_MATRIX.md).

## Status

The project is intentionally split into two layers:

1. **Safe content/data work** — Bountiful 6.0.4 configuration, proven objective/reward patterns, quest catalogs, story-board metadata, proof-item specifications, reward balancing, registry audits, and test plans.
2. **World/runtime integration work** — regional board assignment, useful-structure locating, same-board redemption, proof placement, and map/story state.

The world side is no longer hypothetical: **CozyCrazyZones 0.3.6 is the current geography contract.** The quest project consumes its region/radial classification rather than duplicating that logic. Version 0.3.6 also establishes a real starter information layer: it reserves the first village roughly 1,000–1,650 blocks from spawn, prepares the starter desk map, grants/links the starter Atlas, and paints a route to that first village.

The core rule remains: **do not invent a complicated event system when Bountiful, Supplementaries, Map Atlases, or vanilla criteria can already do the job reliably.** Anything not proven reliable is marked for world integration, registry verification, reward audit, or deferral rather than silently treated as implemented.

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
- Current authored quest design count: **108** plus a growing repeatable/ecology pool
- Current CI validates Bountiful data, regional quest catalog coverage/schema, regional board-selection policy, story-board metadata, and zoning/world bindings

## Useful documents

- [`QUESTS.md`](QUESTS.md) — easy human-readable authored-quest browser
- [`REWARDS.md`](REWARDS.md) — easy regional/tier reward browser
- [`STRUCTURES.md`](STRUCTURES.md) — quest-relevant structure browser
- [`docs/MAP_SYSTEM_VERTICAL_SLICE.md`](docs/MAP_SYSTEM_VERTICAL_SLICE.md) — starter map + quest/cartographer map implementation path
- [`docs/FIRST_VILLAGE_VERTICAL_SLICE.md`](docs/FIRST_VILLAGE_VERTICAL_SLICE.md) — first playable starter→village→board→local-adventure target
- [`docs/REWARD_SOURCE_AUDIT.md`](docs/REWARD_SOURCE_AUDIT.md) — source-checked reward IDs/mechanics and compatibility warnings
- [`design/REGIONAL_QUEST_MATRIX.md`](design/REGIONAL_QUEST_MATRIX.md) — all authored quests + mechanical objective/status
- [`design/STORY_BOARD_UI.md`](design/STORY_BOARD_UI.md) — story-first Bountiful UI plan
- [`design/story_board_mockup.html`](design/story_board_mockup.html) — visual mockup
- [`design/REWARD_VALIDATION_BACKLOG.md`](design/REWARD_VALIDATION_BACKLOG.md) — broader modded reward audit queue
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

This quest project owns **what local people know about them, why the player might care, and what the player receives for engaging with them**.
