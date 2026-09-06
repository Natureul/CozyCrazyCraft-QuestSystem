# CozyCrazyCraft Quest System

Player-facing quest, bounty, map, cartographer, reward, and information-layer design for the CozyCrazyCraft Forge 1.20.1 modpack.

## Start here

### [📜 Browse all 108 current quests by region and tier](QUESTS.md)

That page is the human-readable overview. It shows Shared Core, Frostmarch, Greenveil, Sunscar, and Harvestlands across Hearthlands → Frontier → Wildlands → Dread Reaches, with the current reward direction for each tier.

For implementation detail, see [the mechanical quest matrix](design/REGIONAL_QUEST_MATRIX.md).

## Status

The project is intentionally split into two layers:

1. **Safe content/data work** — Bountiful 6.0.4 configuration, proven objective/reward patterns, quest catalogs, story-board metadata, proof-item specifications, reward balancing, registry audits, and test plans.
2. **World/runtime integration work** — regional board assignment, useful-structure locating, same-board redemption, proof placement, and map/story state.

The world side is no longer purely hypothetical: **CozyCrazyZones 0.3.4 is the current geography contract.** The quest project should consume its region/radial classification rather than duplicate that logic. Integration is still staged so we do not build fragile hooks before the corresponding structure/map behavior is proven.

The core rule remains: **do not invent a complicated event system when Bountiful can already do the job reliably.** Anything not proven reliable is marked for world integration, registry verification, reward audit, or deferral rather than silently treated as implemented.

## Current baseline

- Target: Minecraft Forge 1.20.1
- Bountiful: `6.0.4+1.20.1-forge`
- Geography contract: CozyCrazyZones `0.3.4`
- Default Bountiful bounty pools/decrees: excluded in the deployment baseline
- Only CozyCrazyCraft custom pool/decree content should load after the baseline is installed
- Initial smoke-test decree: `Local Notices`
- Current authored quest design count: **108**

## Useful documents

- [`QUESTS.md`](QUESTS.md) — easy human-readable quest browser
- [`design/REGIONAL_QUEST_MATRIX.md`](design/REGIONAL_QUEST_MATRIX.md) — all quests + mechanical objective/status
- [`design/STORY_BOARD_UI.md`](design/STORY_BOARD_UI.md) — story-first Bountiful UI plan
- [`design/story_board_mockup.html`](design/story_board_mockup.html) — visual mockup
- [`design/REWARD_VALIDATION_BACKLOG.md`](design/REWARD_VALIDATION_BACKLOG.md) — modded reward audit queue
- [`docs/BOUNTIFUL_6_0_4_RELIABILITY.md`](docs/BOUNTIFUL_6_0_4_RELIABILITY.md)
- [`docs/QUEST_FRAMEWORK.md`](docs/QUEST_FRAMEWORK.md)
- [`docs/INTEGRATION_BOUNDARY.md`](docs/INTEGRATION_BOUNDARY.md)
- [`docs/TEST_PLAN.md`](docs/TEST_PLAN.md)
- [`deployment/config/bountiful/`](deployment/config/bountiful/) — custom-only Bountiful baseline

## Geographic model

Every board/location ultimately lives in two classifications:

- radial tier: Hearthlands / Frontier / Wildlands / Dread Reaches
- macro-region: North / East / South / West (plus shared core / transition handling)

So a board can be in, for example, **Frostmarch Frontier** or **Harvestlands Wildlands**, and its work/reward pool can reflect both.

The zoning project owns the truth of **where things can exist**.

This quest project owns **what local people know about them, why the player might care, and what the player receives for engaging with them**.
